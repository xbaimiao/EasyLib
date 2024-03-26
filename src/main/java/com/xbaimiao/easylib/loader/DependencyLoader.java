package com.xbaimiao.easylib.loader;

import com.xbaimiao.easylib.EasyPlugin;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
            if (!Loader.addPath(dependency.getFile())) {
                plugin.getLogger().warning("Load " + dependency.getFile().getName() + " Fail");
            }
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

        public Dependency(String url, String baseUrl) {
            this.url = url;
            String filePath = url.replace(baseUrl, "")
                    .replace("/", File.separator);
            this.file = new File("libraries",filePath);
        }

        public Dependency(String url) {
            this(url, "https://maven.aliyun.com/repository/public/");
        }

        public File getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }
    }

}