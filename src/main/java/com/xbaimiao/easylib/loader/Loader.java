package com.xbaimiao.easylib.loader;

import com.xbaimiao.easylib.EasyPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class Loader {

    public static final String ALIYUN_REPO_URL = "https://maven.aliyun.com/repository/public/";

    public static void loaderKotlin(String version, EasyPlugin plugin, Map<String, String> relocateMap, String repoUrl) {
        List<String> kotlinLibrary = new ArrayList<>();
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-stdlib:" + version);
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + version);
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-stdlib-jdk7:" + version);
        kotlinLibrary.add("org.jetbrains.kotlin:kotlin-reflect:" + version);
        kotlinLibrary.add("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3");
        kotlinLibrary.add("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3");

        for (String library : kotlinLibrary) {
            Map.Entry<String, Map.Entry<String, String>> url = Loader.dependencyToUrl(library, repoUrl);
            DependencyLoader.Dependency dependency = Loader.toDependenency(url, repoUrl, relocateMap);
            DependencyLoader.load(plugin, dependency);
        }
    }

    public static String replace(String s) {
        return s.replace("!", "");
    }

    public static Map.Entry<String, Map.Entry<String, String>> dependencyToUrl(String dependency, String repoUrl) {
        String repoBaseUrl = repoUrl;
        if (!repoUrl.endsWith("/")) repoBaseUrl = repoUrl + "/";

        String[] extensionParts = dependency.split("@");
        String[] parts = extensionParts[0].split(":");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException("Format not correct: " + dependency);
        }
        String group = parts[0];
        String name = parts[1];
        String version = parts[2];
        String classifier = parts.length == 4 ? parts[3] : "";
        String extension = extensionParts.length == 2 ? extensionParts[1] : "jar";
        String groupPath = group.replace('.', '/');
        String artifact = !classifier.isEmpty() ? String.format("%s-%s-%s.%s", name, version, classifier, extension) : String.format("%s-%s.%s", name, version, extension);

        Map.Entry<String, String> innerPair = new HashMap.SimpleEntry<>(group + ":" + name, version);
        return new HashMap.SimpleEntry<>(repoBaseUrl + groupPath + "/" + name + "/" + version + "/" + artifact, innerPair);
    }

    public static DependencyLoader.Dependency toDependenency(Map.Entry<String, Map.Entry<String, String>> entry, String repoUrl, Map<String, String> relocateMap) {
        return new DependencyLoader.Dependency(entry.getKey(), repoUrl, relocateMap, toNumericVersion(entry.getValue().getValue()), entry.getValue().getKey());
    }

    public static DependencyLoader.Dependency toDependenency(String dependency, String repoUrl) {
        Map.Entry<String, Map.Entry<String, String>> url = dependencyToUrl(dependency, repoUrl);
        return toDependenency(url, repoUrl, new HashMap<>());
    }

    public static DependencyLoader.Dependency toDependenency(String dependency) {
        Map.Entry<String, Map.Entry<String, String>> url = dependencyToUrl(dependency, ALIYUN_REPO_URL);
        return toDependenency(url, ALIYUN_REPO_URL, new HashMap<>());
    }

    public static int toNumericVersion(String version) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(version);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        return Integer.parseInt(sb.toString());
    }

    public static List<DependencyLoader.Dependency> cleanDependencies(List<DependencyLoader.Dependency> list) {
        Map<String, DependencyLoader.Dependency> map = new HashMap<>();

        for (DependencyLoader.Dependency dependency : list) {
            if ("ignore:ignore".equals(dependency.getIdentify())) {
                map.put(dependency.getUrl(), dependency);
            } else {
                if (map.containsKey(dependency.getIdentify())) {
                    DependencyLoader.Dependency oldDependency = map.get(dependency.getIdentify());
                    if (dependency.getNumericVersion() > oldDependency.getNumericVersion()) {
                        map.put(dependency.getIdentify(), dependency);
                    }
                } else {
                    map.put(dependency.getIdentify(), dependency);
                }
            }
        }

        List<DependencyLoader.Dependency> cleanedList = new ArrayList<>();
        map.forEach((key, value) -> cleanedList.add(value));
        return cleanedList;
    }
}
