package com.xbaimiao.easylib.loader;

import com.xbaimiao.easylib.EasyPlugin;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class KotlinLoader {

    private static final String stdlib = "https://maven.aliyun.com/repository/public/org/jetbrains/kotlin/kotlin-stdlib/1.9.0/kotlin-stdlib-1.9.0.jar";
    private static final String reflect = "https://maven.aliyun.com/repository/public/org/jetbrains/kotlin/kotlin-reflect/1.9.0/kotlin-reflect-1.9.0.jar";
    private static final String jdk8 = "https://maven.aliyun.com/repository/public/org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.9.0/kotlin-stdlib-jdk8-1.9.0.jar";
    private static final String jdk7 = "https://maven.aliyun.com/repository/public/org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.9.0/kotlin-stdlib-jdk7-1.9.0.jar";

    private static final List<Lib> libs = new ArrayList<>();

    static {
        libs.add(new Lib(stdlib));
        libs.add(new Lib(reflect));
        libs.add(new Lib(jdk8));
        libs.add(new Lib(jdk7));
    }

    public static void loader(EasyPlugin plugin) {
        for (Lib lib : libs) {
            if (!lib.getFile().exists()) {
                if (!lib.getFile().getParentFile().exists() && lib.getFile().getParentFile().mkdirs()) {
                    plugin.getLogger().info("Download " + lib.getFile().getName());
                    if (download(lib.getUrl(), lib.getFile())) {
                        plugin.getLogger().info("Download " + lib.getFile().getName() + " Success");
                    }
                }
            }
            if (!Loader.addPath(lib.getFile())) {
                plugin.getLogger().warning("Load " + lib.getFile().getName() + " Fail");
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

    static class Lib {

        private final File file;
        private final String url;

        private Lib(String url) {
            this.url = url;
            String filePath = url.replace("https://maven.aliyun.com/repository/public/", "")
                    .replace("/", File.separator);
            this.file = new File(filePath);
        }

        public File getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }
    }

}