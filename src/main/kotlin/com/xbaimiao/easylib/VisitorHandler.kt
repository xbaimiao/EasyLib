package com.xbaimiao.easylib

import com.xbaimiao.easylib.command.registerCommand
import com.xbaimiao.easylib.util.*
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.objectweb.asm.*
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.jar.JarFile

object VisitorHandler {

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

            if (!classReader.hasAnnotation()) {
                return@forEach
            }
            val clazz = Class.forName(classReader.className.replace("/", "."), false, VisitorHandler::class.java.classLoader)
            val instance = runCatching { clazz.getDeclaredField("INSTANCE") }.getOrNull()?.get(clazz) ?: return@forEach

            if (clazz.isAnnotationPresent(EConfig::class.java)) {
                loadConfig(instance)
                debug("${clazz.name} 通过 EConfig 注册配置文件成功")
            }
            if (clazz.isAnnotationPresent(ECommandHeader::class.java)) {
                registerCommand(instance)
                debug("${clazz.name} 通过 ECommandHeader 注册命令成功")
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
        listOf(EConfig::class.java, ECommandHeader::class.java, EListener::class.java).map { it.name }
    }

    private fun ClassReader.hasAnnotation(): Boolean {
        var hasAnnotation = false

        val classVisitor = object : ClassVisitor(Opcodes.ASM6, ClassWriter(this, ClassWriter.COMPUTE_MAXS)) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
                val className = descriptor.substring(1, descriptor.length - 1).replace("/", ".")

                if (!descriptor.contains("EConfig")) {
                    return super.visitAnnotation(descriptor, visible)
                }
                debug(annotations)
                debug(className)
                if (annotations.any { className in annotations }) {
                    hasAnnotation = true
                }
                return super.visitAnnotation(descriptor, visible)
            }

        }

        this.accept(classVisitor, 0)
        return hasAnnotation
    }

}