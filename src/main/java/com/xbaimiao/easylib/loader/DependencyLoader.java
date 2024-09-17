package com.xbaimiao.easylib.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xbaimiao.easylib.EasyPlugin;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DependencyLoader {

    public static final List<Dependency> DEPENDENCIES = new ArrayList<>();
    public static final Map<String, String> GOAL_RELOCATE = new HashMap<>();
    protected static final List<Dependency> DEPENDENCIES_FOR_PLUGIN = new ArrayList<>();
    protected static String kotlinVersion = "1.9.20";

    public static void loader(EasyPlugin plugin) {
        List<Dependency> dependencies = Loader.cleanDependencies(DEPENDENCIES);
        for (Dependency dependency : dependencies) {
            load(plugin, dependency);
        }
    }

    public static void load(EasyPlugin plugin, String dependency) {
        load(plugin, Loader.toDependenency(dependency));
    }

    public static void load(EasyPlugin plugin, String dependency, String repo) {
        load(plugin, Loader.toDependenency(dependency, repo));
    }

    public static void load(EasyPlugin plugin, Dependency dependency) {
        if (!dependency.getFile().exists()) {
            if (!dependency.getFile().getAbsoluteFile().getParentFile().exists()) {
                dependency.getFile().getAbsoluteFile().getParentFile().mkdirs();
            }
            debug(plugin, "Download " + dependency.getFile().getName());
            if (download(dependency.getUrl(), dependency.getFile(), plugin)) {
                debug(plugin, "Download " + dependency.getFile().getName() + " Success");
            }
        }
        File finalFile = dependency.getFile();
        if (dependency.isRelocate()) {
            Map<String, String> rules = dependency.getRelocateRules();
            rules.putAll(GOAL_RELOCATE);
            // 如果规则不为空
            if (!rules.isEmpty()) {
                File tempFile = null;
                try {
                    // 创建重定位规则列表
                    List<Relocation> relocationRules = new ArrayList<>();
                    // 遍历规则集合，将规则添加到重定位规则列表中
                    for (Map.Entry<String, String> entry : rules.entrySet()) {
                        relocationRules.add(new Relocation(entry.getKey(), entry.getValue()));
                    }
                    // 生成规则的MD5值
                    String md5 = generateMD5FromMap(rules);
                    // 调试日志输出重定位规则和MD5值
                    debug(plugin, "Relocation Rules: " + rules + "MD5: " + md5);

                    // 获取文件名和文件扩展名
                    int fileNameIndex = dependency.getFile().getName().lastIndexOf('.');
                    String fileName = dependency.getFile().getName().substring(0, fileNameIndex);
                    String fileExtension = dependency.getFile().getName().substring(fileNameIndex);

                    File folder = new File(dependency.getFile().getAbsoluteFile().getParentFile(), plugin.getName());
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    File[] files = folder.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            debug(plugin, file.getName() + " MD5: " + md5);
                            if (!file.getName().contains(md5)) {
                                file.delete();
                            }
                        }
                    }
                    // 创建临时文件
                    tempFile = new File(folder, fileName + "-" + md5 + fileExtension);

                    // 调试日志输出文件路径
                    debug(plugin, "File Path: " + tempFile.getAbsolutePath());

                    // 如果临时文件不存在
                    if (!tempFile.exists()) {
                        // 调试日志输出正在重定位的文件名
                        debug(plugin, "Relocating " + dependency.getFile().getName());
                        // 创建Jar重定位器并执行重定位
                        JarRelocator jarRelocator = new JarRelocator(dependency.getFile(), tempFile, relocationRules);
                        jarRelocator.run();
                        // 调试日志输出已重定位的文件名
                        debug(plugin, "Relocated " + dependency.getFile().getName());
                    }

                    // 设置最终文件为临时文件
                    finalFile = tempFile;
                } catch (Throwable e) {
                    // 如果发生异常且临时文件不为空，删除临时文件
                    if (tempFile != null) {
                        tempFile.delete();
                    }
                    // 记录日志输出失败重定位的文件名和异常信息
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to relocate " + dependency.getFile().getName(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        plugin.getClassPathAppender().addJarToClasspath(finalFile.toPath());
    }

    private static boolean download(String in, File file, Plugin plugin) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(in).openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                plugin.getLogger().warning("Failed to download file " + in + ", server return code: " + connection.getResponseCode());
                return false;
            }
            BufferedOutputStream bufferedOutputStream = getBufferedOutputStream(file, connection);
            bufferedOutputStream.flush();
            return true;
        } catch (Exception e) {
            file.delete();
            e.printStackTrace();
            return false;
        }
    }

    @NotNull
    private static BufferedOutputStream getBufferedOutputStream(File file, HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        byte[] buf = new byte[1024];
        int len;
        while ((len = bufferedInputStream.read(buf)) > 0) {
            bufferedOutputStream.write(buf, 0, len);
        }
        return bufferedOutputStream;
    }

    private static String generateMD5FromMap(Map<String, String> map) {

        // 将map转换成String
        StringBuilder mapAsString = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            mapAsString.append(entry.getKey() + "=" + entry.getValue() + ",");
        }

        String result = "";

        try {
            // 获取一个MD5消息摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算并更新摘要
            md.update(mapAsString.toString().getBytes());

            // 生成摘要
            byte[] digest = md.digest();

            // 使用BigInteger将bytes转换为Hex
            BigInteger no = new BigInteger(1, digest);

            // 转换为16进制字符串
            result = no.toString(16);
            while (result.length() < 32) {
                result = "0" + result;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }

    public static void injectDebug(EasyPlugin plugin) {
        InputStream inputStream = plugin.getResource("config.yml");
        if (inputStream == null) {
            return;
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(inputStream)));
        if (configuration.getBoolean("debug")) {
            plugin.setDebug(true);
        }
    }

    public static void init(EasyPlugin plugin) {
        InputStream inputStream = plugin.getResource("plugin.yml");
        assert inputStream != null;
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(inputStream)));
        List<String> relocateList = configuration.getStringList("relocate");
        if (!relocateList.isEmpty()) {
            for (String s : relocateList) {
                String[] args = s.split("!");
                String source = args[0];
                String fresh = args[1];
                GOAL_RELOCATE.put(source, fresh);
                debug(plugin, "Relocating " + source + " to " + fresh);
            }
        }

        String overrideKotlinVersion = configuration.getString("kotlin-version");
        if (overrideKotlinVersion != null) {
            kotlinVersion = overrideKotlinVersion;
        }
        debug(plugin, "Kotlin Version: " + kotlinVersion);

        for (String s : configuration.getStringList("depend-list")) {
            String depend = s;
            String repo = Loader.ALIYUN_REPO_URL;
            if (s.contains("<<repo>>")) {
                repo = s.split("<<repo>>")[1];
                depend = s.split("<<repo>>")[0];
            }
            DEPENDENCIES_FOR_PLUGIN.add(Loader.toDependenency(depend, repo));
        }

    }

    public static void debug(EasyPlugin plugin, String content) {
        if (plugin.getDebug()) {
            plugin.getLogger().info(content);
        }
    }

    public static class Dependency {

        private final File file;
        private final String url;
        private final int numericVersion;
        private final String identify;
        private Map<String, String> relocateRules;
        private boolean relocate = true;

        /**
         * @param url            下载链接
         * @param baseUrl        仓库地址
         * @param relocateRules  重定向规则
         * @param numericVersion 版本号
         * @param identify       唯一标识
         */
        public Dependency(String url, String baseUrl, Map<String, String> relocateRules, int numericVersion, String identify) {
            this.url = url;
            this.relocateRules = relocateRules;
            String filePath = url.replace(baseUrl, "").replace("/", File.separator);
            this.file = new File("libraries", filePath);
            this.numericVersion = numericVersion;
            this.identify = identify;
        }

        /**
         * @param url            下载链接
         * @param file           文件地址
         * @param numericVersion 版本号
         * @param identify       唯一标识
         */
        public Dependency(String url, File file, int numericVersion, String identify) {
            this.url = url;
            this.relocateRules = new HashMap<>();
            this.file = file;
            this.numericVersion = numericVersion;
            this.identify = identify;
        }

        public static List<Dependency> fromJsonArray(JsonArray dependencies) {
            List<Dependency> result = new ArrayList<>();
            for (JsonElement element : dependencies) {
                JsonObject jsonObject = element.getAsJsonObject();
                String url = jsonObject.get("url").getAsString();
                int numericVersion = jsonObject.get("numericVersion").getAsInt();
                String identify = jsonObject.get("identify").getAsString();
                String filePath = jsonObject.get("file").getAsString();
                File file = new File(filePath);
                result.add(new Dependency(url, file, numericVersion, identify));
            }
            return result;
        }

        public File getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getRelocateRules() {
            return relocateRules;
        }

        public void setRelocateRules(Map<String, String> relocateRules) {
            this.relocateRules = relocateRules;
        }

        public int getNumericVersion() {
            return numericVersion;
        }

        public String getIdentify() {
            return identify;
        }

        public boolean isRelocate() {
            return relocate;
        }

        public void setRelocate(boolean relocate) {
            this.relocate = relocate;
        }

        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("url", url);
            jsonObject.addProperty("numericVersion", numericVersion);
            jsonObject.addProperty("identify", identify);
            jsonObject.addProperty("file", file.getPath());
            return jsonObject;
        }

        public Dependency copy(Map<String, String> relocateRules) {
            Dependency dependency = new Dependency(url, file, numericVersion, identify);
            dependency.setRelocateRules(relocateRules);
            return dependency;
        }

        @Override
        public String toString() {
            return "Dependency{" + "file=" + file + ", url='" + url + '\'' + ", relocateRules=" + relocateRules + ", numericVersion=" + numericVersion + ", identify='" + identify + '\'' + '}';
        }

    }

}
