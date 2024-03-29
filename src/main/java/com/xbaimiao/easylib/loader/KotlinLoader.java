package com.xbaimiao.easylib.loader;

import com.xbaimiao.easylib.EasyPlugin;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KotlinLoader {

    public static void loader(String version, EasyPlugin plugin, Map<String, String> relocateMap, String repoUrl) {
        List<String> kotlinLibrary = new ArrayList<>();
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-stdlib:" + version);
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + version);
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-stdlib-jdk7:" + version);
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-reflect:" + version);
        kotlinLibrary.add("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3");

        for (String library : kotlinLibrary) {
            try {
                Map.Entry<String, Map.Entry<String, String>> url = Loader.dependencyToUrl(library, repoUrl);
                DependencyLoader.Dependency dependency = new DependencyLoader.Dependency(url.getKey(), repoUrl, relocateMap, 1, url.getValue().getKey());
                DependencyLoader.load(plugin, dependency);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String replace(String s) {
        return s.replace("!", "");
    }


}
