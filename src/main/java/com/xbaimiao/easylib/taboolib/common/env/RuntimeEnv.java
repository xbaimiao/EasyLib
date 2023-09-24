package com.xbaimiao.easylib.taboolib.common.env;

import com.xbaimiao.easylib.nms.RuntimeResource;
import com.xbaimiao.easylib.nms.RuntimeResources;
import com.xbaimiao.easylib.util.BukkitKt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipFile;

/**
 * a
 *
 * @author xbaimiao
 * @since 2023/9/13 22:38
 */
public class RuntimeEnv {

    private static String defaultAssets = "assets";

    public static void loadAssets(@NotNull Class<?> clazz) {
        RuntimeResource[] resources = null;
        if (clazz.isAnnotationPresent(RuntimeResource.class)) {
            resources = clazz.getAnnotationsByType(RuntimeResource.class);
        } else {
            RuntimeResources annotation = clazz.getAnnotation(RuntimeResources.class);
            if (annotation != null) {
                resources = annotation.value();
            }
        }
        if (resources == null) {
            return;
        }
        for (RuntimeResource resource : resources) {
            loadAssets(resource.name(), resource.hash(), resource.value(), resource.zip());
        }
    }

    public static void loadAssets(String name, String hash, String url, boolean zip) {
        File file;
        if (name.isEmpty()) {
            file = new File(defaultAssets, hash.substring(0, 2) + "/" + hash);
        } else {
            file = new File(defaultAssets, name);
        }
        if (file.exists() && IO.getHash(file).equals(hash)) {
            return;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        BukkitKt.info("Downloading assets " + url.substring(url.lastIndexOf('/') + 1));
        try {
            if (zip) {
                File cacheFile = new File(file.getParentFile(), file.getName() + ".zip");
                IO.downloadFile(new URL(url + ".zip"), cacheFile);
                try (ZipFile zipFile = new ZipFile(cacheFile)) {
                    InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(url.substring(url.lastIndexOf('/') + 1)));
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        fileOutputStream.write(IO.readFully(inputStream));
                    }
                } finally {
                    cacheFile.delete();
                }
            } else {
                IO.downloadFile(new URL(url), file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
