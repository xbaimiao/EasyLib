package com.xbaimiao.easylib.loader;

import com.xbaimiao.easylib.EasyPlugin;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyLoader {

    public static final List<Dependency> DEPENDENCIES = new ArrayList<>();

    public static void loader(EasyPlugin plugin) {
        for (Dependency dependency : DEPENDENCIES) {
            if (!dependency.getFile().exists()) {
                if (!dependency.getFile().getParentFile().exists() && dependency.getFile().getParentFile().mkdirs()) {
                    plugin.getLogger().info("Download " + dependency.getFile().getName());
                    if (download(dependency.getUrl(), dependency.getFile())) {
                        plugin.getLogger().info("Download " + dependency.getFile().getName() + " Success");
                    }
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

                    plugin.getLogger().info("Relocated " + dependency.getFile().getName());

                    finalFile = tempFile;
                    shouldDeleteOnExit = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!Loader.addPath(finalFile)) {
                plugin.getLogger().warning("Load " + dependency.getFile().getName() + " Fail");
            }
            if (shouldDeleteOnExit) finalFile.deleteOnExit();
        }
    }

    private static boolean download(String in, File file) {
        try {
            InputStream inputStream = new URL(in).openStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            byte[] buf = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buf)) > 0) {
                bufferedOutputStream.write(buf, 0, len);
            }
            bufferedOutputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class Dependency {

        private final File file;
        private final String url;
        private final Map<String, String> relocateRules;

        public Dependency(String url, String baseUrl, Map<String, String> relocateRules) {
            this.url = url;
            this.relocateRules = relocateRules;
            String filePath = url.replace(baseUrl, "")
                    .replace("/", File.separator);
            this.file = new File("libraries", filePath);
        }

        public Dependency(String url) {
            this(url, "https://maven.aliyun.com/repository/public/", new HashMap<>());
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
    }

}