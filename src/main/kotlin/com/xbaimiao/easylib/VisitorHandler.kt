package com.xbaimiao.easylib

import com.xbaimiao.easylib.bridge.PlaceholderExpansion
import com.xbaimiao.easylib.command.registerCommand
import com.xbaimiao.easylib.loader.DependencyLoader
import com.xbaimiao.easylib.util.*
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.objectweb.asm.*
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.jar.JarFile

object VisitorHandler {

    val lifeCycleMethodList = ArrayList<LifeCycleMethod>()

    /**
     * 获取 URL 下的所有类
     */
    fun URL.visitor() {
        val srcFile = try {
            File(toURI())
        } catch (ex: IllegalArgumentException) {
            File((openConnection() as JarURLConnection).jarFileURL.toURI())
        } catch (ex: URISyntaxException) {
            File(path)
        }
        val jarFile = JarFile(srcFile)
        jarFile.stream().filter { it.name.endsWith(".class") }.forEach { entry ->
            if (entry.name.contains("shadow")) return@forEach
            val classReader = ClassReader(jarFile.getInputStream(entry))

            if (!runCatching { classReader.hasAnnotation() }.getOrElse { false }) {
                return@forEach
            }
            val clazz =
                Class.forName(classReader.className.replace("/", "."), false, VisitorHandler::class.java.classLoader)
            val instance = runCatching { clazz.getDeclaredField("INSTANCE") }.getOrNull()?.get(clazz) ?: return@forEach

            if (clazz.isAnnotationPresent(EPlaceholderExpansion::class.java)) {
                (instance as? PlaceholderExpansion)?.register()
                debug("${clazz.name} 通过 EPlaceholderExpansion 注册占位符成功")
            }

            if (clazz.isAnnotationPresent(EConfig::class.java)) {
                loadConfig(instance)
                debug("${clazz.name} 通过 EConfig 注册配置文件成功")
            }
            if (clazz.isAnnotationPresent(ECommandHeader::class.java)) {
                registerCommand(instance)
                debug("${clazz.name} 通过 ECommandHeader 注册命令成功")
            }
            if (clazz.isAnnotationPresent(Dependency::class.java)) {
                handleDependency(clazz.getAnnotation(Dependency::class.java))
            }
            if (clazz.isAnnotationPresent(DependencyList::class.java)) {
                clazz.getAnnotation(DependencyList::class.java).depends.forEach {
                    handleDependency(it)
                }
            }
            if (clazz.isAnnotationPresent(AwakeClass::class.java)) {
                clazz.declaredMethods.filter { it.isAnnotationPresent(Awake::class.java) }.forEach { method ->
                    val awake = method.getAnnotation(Awake::class.java)
                    LifeCycleMethod(awake.lifeCycle, method, instance).also {
                        lifeCycleMethodList.add(it)
                        debug("${clazz.name} 通过 Awake 注册生命周期方法 ${method.name} 成功")
                    }
                }
            }
            if (clazz.isAnnotationPresent(EListener::class.java)) {
                val eListener = clazz.getAnnotation(EListener::class.java)
                if (clazz.interfaces.contains(Listener::class.java)) {
                    if (eListener.depend.isNotEmpty()) {
                        if (eListener.depend.all { Bukkit.getPluginManager().getPlugin(it) != null }) {
                            registerListener(instance as Listener)
                            debug("${clazz.name} 通过 EListener 注册监听器成功")
                        } else {
                            debug("${clazz.name} 依赖 ${eListener.depend} 不满足 不注册监听器")
                        }
                    } else {
                        registerListener(instance as Listener)
                        debug("${clazz.name} 通过 EListener 注册监听器成功")
                    }
                } else {
                    warn("${clazz.name} not is Listener, but it annotation EListener!")
                }
            }
        }
    }

    private val annotations by lazy {
        listOf(
            EConfig::class.java,
            ECommandHeader::class.java,
            EListener::class.java,
            EPlaceholderExpansion::class.java,
            Dependency::class.java,
            DependencyList::class.java,
            AwakeClass::class.java
        ).map { it.name }
    }

    private fun ClassReader.hasAnnotation(): Boolean {
        var hasAnnotation = false

        val classVisitor = object : ClassVisitor(Opcodes.ASM6, ClassWriter(this, ClassWriter.COMPUTE_MAXS)) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
                val className = descriptor.substring(1, descriptor.length - 1).replace("/", ".")
                if (annotations.any { className in annotations }) {
                    hasAnnotation = true
                }
                return super.visitAnnotation(descriptor, visible)
            }

        }

        this.accept(classVisitor, 0)
        return hasAnnotation
    }

    private fun handleDependency(dependency: Dependency) {
        debug("处理依赖 ${dependency.url}")
        if (kotlin.runCatching { Class.forName(dependency.clazz) }.getOrNull() == null) {
            val url = if (dependency.format) {
                val processed = dependency.url.dependencyToUrl(dependency.repoUrl)
                debug("解析依赖地址 $processed")
                processed
            } else dependency.url
            DependencyLoader.DEPENDENCIES.add(DependencyLoader.Dependency(url, dependency.repoUrl))
            debug("添加依赖 ${dependency.url}")
        }
    }

    private fun String.dependencyToUrl(repoUrl: String): String {
        var repoBaseUrl = repoUrl
        if (!repoUrl.endsWith("/")) repoBaseUrl = "$repoUrl/"

        val parts = this.split(':')
        if (parts.size !in 3..4) {
            throw IllegalArgumentException("Format not correct")
        }
        val group = parts[0]
        val name = parts[1]
        val version = parts[2]
        val classifier = if (parts.size == 4) parts[3] else ""
        val groupPath = group.replace('.', '/')
        val artifact = if (classifier.isNotEmpty()) "$name-$version-$classifier.jar" else "$name-$version.jar"

        return "${repoBaseUrl}$groupPath/$name/$version/$artifact"
    }

    @JvmStatic
    fun loadConfig(configObj: Any) {
        val configClass = configObj::class.java
        val configFileAnnotation = configClass.getAnnotation(EConfig::class.java)
            ?: error("${configObj::class.java.simpleName} must have @Config annotation")

        val file = File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, configFileAnnotation.file)

        if (!file.exists()) {
            plugin.saveResource(configFileAnnotation.file, false)
        }

        val configFields = configClass.declaredFields.filter {
            it.isAnnotationPresent(ConfigNode::class.java)
        }

        val configuration = YamlConfiguration.loadConfiguration(file)

        var isChange = false

        for (field in configFields) {
            field.isAccessible = true
            val annotation = field.getAnnotation(ConfigNode::class.java)
            val yamlValue = configuration.get(annotation.node)
            if (yamlValue == null) {
                debug("${configFileAnnotation.file} not found ${annotation.node}. auto create")
                configuration.set(annotation.node, field.get(configObj))
                if (!isChange) {
                    isChange = true
                }
                continue
            }
            debug("${configFileAnnotation.file} found ${annotation.node}. value: $yamlValue")
            field.set(configObj, yamlValue)
        }
        if (isChange) {
            configuration.save(file)
        }

    }

}
