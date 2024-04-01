package com.xbaimiao.easylib.loader;

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
    private static final Map<String, String> goalRelocate = new HashMap<>();

    public static void loader(EasyPlugin plugin) {
        List<Dependency> dependencies = Loader.cleanDependencies(DEPENDENCIES);
        for (Dependency dependency : dependencies) {
            load(plugin, dependency);
        }
    }

    public static void load(EasyPlugin plugin, Dependency dependency) {
        if (!dependency.getFile().exists()) {
            if (!dependency.getFile().getParentFile().exists()) {
                dependency.getFile().getParentFile().mkdirs();
            }
            plugin.getLogger().info("Download " + dependency.getFile().getName());
            if (download(dependency.getUrl(), dependency.getFile(), plugin)) {
                plugin.getLogger().info("Download " + dependency.getFile().getName() + " Success");
            }
        }
        File finalFile = dependency.getFile();
        Map<String, String> rules = dependency.getRelocateRules();
        rules.putAll(goalRelocate);
        if (!rules.isEmpty()) {
            File tempFile = null;
            try {
                List<Relocation> relocationRules = new ArrayList<>();
                for (Map.Entry<String, String> entry : rules.entrySet()) {
                    relocationRules.add(new Relocation(entry.getKey(), entry.getValue()));
                }
                String md5 = generateMD5FromMap(rules);
                debug(plugin, "Relocation Rules: " + rules + "MD5: " + md5);

                int fileNameIndex = dependency.getFile().getName().lastIndexOf('.');
                String fileName = dependency.getFile().getName().substring(0, fileNameIndex);
                String fileExtension = dependency.getFile().getName().substring(fileNameIndex);
                tempFile = new File(dependency.getFile().getParentFile(), fileName + "-" + md5 + fileExtension);

                debug(plugin, "File Path: " + tempFile.getAbsolutePath());

                if (!tempFile.exists()) {
                    debug(plugin, "Relocating " + dependency.getFile().getName());
                    JarRelocator jarRelocator = new JarRelocator(dependency.getFile(), tempFile, relocationRules);
                    jarRelocator.run();
                    debug(plugin, "Relocated " + dependency.getFile().getName());
                }

//                plugin.getLogger().info("Relocated " + dependency.getFile().getName());

                finalFile = tempFile;
            } catch (IOException e) {
                if (tempFile != null) {
                    tempFile.delete();
                }
                Bukkit.getLogger().log(Level.SEVERE, "Failed to relocate " + dependency.getFile().getName(), e);
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
                goalRelocate.put(source, fresh);
                debug(plugin, "Relocating " + source + " to " + fresh);
            }
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
        private final Map<String, String> relocateRules;
        private final int numericVersion;
        private final String identify;

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

        public File getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getRelocateRules() {
            return relocateRules;
        }

        public int getNumericVersion() {
            return numericVersion;
        }

        public String getIdentify() {
            return identify;
        }
    }

}
