package com.xbaimiao.easylib.loader;

import org.bukkit.Bukkit;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class Loader extends URLClassLoader {

    static Lookup lookup;
    static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable ignored) {
        }

    }

    public Loader(URL[] urls) {
        super(urls);
    }

    public static boolean addPath(File file) {
        try {
            ClassLoader loader = Bukkit.class.getClassLoader();
            if (loader.getClass().getSimpleName().equals("LaunchClassLoader")) {
                MethodHandle methodHandle = lookup.findVirtual(loader.getClass(), "addURL", MethodType.methodType(Void.TYPE, URL.class));
                methodHandle.invoke(loader, file.toURI().toURL());
            } else {
                Field ucpField;
                try {
                    ucpField = loader.getClass().getDeclaredField("ucp");
                } catch (NoSuchFieldException | NoSuchFieldError var7) {
                    ucpField = loader.getClass().getSuperclass().getDeclaredField("ucp");
                }

                long ucpOffset = unsafe.objectFieldOffset(ucpField);
                Object ucp = unsafe.getObject(loader, ucpOffset);
                MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(Void.TYPE, URL.class));
                methodHandle.invoke(ucp, file.toURI().toURL());
            }

            return true;
        } catch (Throwable var8) {
            var8.printStackTrace();
            return false;
        }
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
