package com.xbaimiao.easylib.loader;

import com.xbaimiao.easylib.EasyPlugin;
import com.xbaimiao.easylib.VisitorHandler;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class DependencyLoader {

    public static final List<Dependency> DEPENDENCIES = new ArrayList<>();

    public static void loader(EasyPlugin plugin) {
        List<Dependency> dependencies = VisitorHandler.cleanDependencies(DEPENDENCIES);
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
        boolean shouldDeleteOnExit = false;
        if (!rules.isEmpty()) {
            try {
                List<Relocation> relocationRules = new ArrayList<>();
                for (Map.Entry<String, String> entry : rules.entrySet()) {
                    relocationRules.add(new Relocation(entry.getKey(), entry.getValue()));
                }
                File tempFile = File.createTempFile("EasyLib_Relocate_", ".jar");
                JarRelocator jarRelocator = new JarRelocator(dependency.getFile(), tempFile, relocationRules);
                jarRelocator.run();

//                plugin.getLogger().info("Relocated " + dependency.getFile().getName());

                finalFile = tempFile;
                shouldDeleteOnExit = true;
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to relocate " + dependency.getFile().getName(), e);
            }
        }
        if (!Loader.addPath(finalFile)) {
            plugin.getLogger().warning("Load " + dependency.getFile().getName() + " Fail");
        }
        if (shouldDeleteOnExit) finalFile.deleteOnExit();
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
            String filePath = url.replace(baseUrl, "")
                    .replace("/", File.separator);
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
