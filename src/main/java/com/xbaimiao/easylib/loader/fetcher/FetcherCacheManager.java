package com.xbaimiao.easylib.loader.fetcher;

import com.xbaimiao.easylib.loader.DependencyLoader;
import com.xbaimiao.easylib.util.GsonKt;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FetcherCacheManager {

    private static final List<FetcherCache> FETCHER_CACHE_LIST = new ArrayList<>();
    private static final File CACHE_DIR = new File("libraries/com/xbaimiao/easylib/cache".replace("/", File.separator));

    static {
        if (!CACHE_DIR.exists()) {
            CACHE_DIR.mkdirs();
        }
        File[] files = CACHE_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        StringBuilder json = new StringBuilder();
                        for (String line : Files.readAllLines(file.toPath())) {
                            json.append(line);
                        }
                        FetcherCache fetcherCache = FetcherCache.fromJson(GsonKt.parseJson(json.toString()).getAsJsonObject());
                        FETCHER_CACHE_LIST.add(fetcherCache);
                    } catch (IOException e) {
                        Bukkit.getLogger().warning("Failed to load cache file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static void addCache(@NotNull FetcherCache fetcherCache) {
        FETCHER_CACHE_LIST.add(fetcherCache);
        File file = new File(CACHE_DIR, fetcherCache.getDependency().replace(":", "_") + ".json");
        if (!file.exists()) {
            writeCache(file, fetcherCache);
        }
    }

    @Nullable
    public static List<DependencyLoader.Dependency> getCache(String dependency, String repoUrl, Map<String, String> relocationRules) {
        for (FetcherCache fetcherCache : FETCHER_CACHE_LIST) {
            if (fetcherCache.getDependency().equals(dependency) && fetcherCache.getRepoUrl().equals(repoUrl)) {
                List<DependencyLoader.Dependency> dependencies = new ArrayList<>();
                for (DependencyLoader.Dependency cacheDependency : fetcherCache.getDependencies()) {
                    dependencies.add(cacheDependency.copy(relocationRules));
                }
                return dependencies;
            }
        }
        return null;
    }

    private static void writeCache(File file, FetcherCache fetcherCache) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create cache file: " + file.getAbsolutePath(), e);
            }
        }

        String content = fetcherCache.toJson().toString();
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write cache file: " + file.getAbsolutePath(), e);
        }
    }

}
