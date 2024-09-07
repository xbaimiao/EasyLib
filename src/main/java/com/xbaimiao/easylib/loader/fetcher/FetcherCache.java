package com.xbaimiao.easylib.loader.fetcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xbaimiao.easylib.loader.DependencyLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FetcherCache {

    private final String dependency;
    private final String repoUrl;
    private final List<DependencyLoader.Dependency> dependencies;

    public FetcherCache(@NotNull String dependency, @NotNull String repoUrl, List<DependencyLoader.Dependency> dependencies) {
        this.dependency = dependency;
        this.repoUrl = repoUrl;
        this.dependencies = dependencies;
    }

    public static FetcherCache fromJson(JsonObject jsonObject) {
        String dependency = jsonObject.get("dependency").getAsString();
        String repoUrl = jsonObject.get("repoUrl").getAsString();
        List<DependencyLoader.Dependency> dependencies = DependencyLoader.Dependency.fromJsonArray(
                jsonObject.get("dependencies").getAsJsonArray()
        );
        return new FetcherCache(dependency, repoUrl, dependencies);
    }

    @NotNull
    public String getDependency() {
        return dependency;
    }

    @NotNull
    public String getRepoUrl() {
        return repoUrl;
    }

    public List<DependencyLoader.Dependency> getDependencies() {
        return dependencies;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dependency", dependency);
        jsonObject.addProperty("repoUrl", repoUrl);

        JsonArray jsonArray = new JsonArray();
        for (DependencyLoader.Dependency dependency : dependencies) {
            jsonArray.add(dependency.toJson());
        }
        jsonObject.add("dependencies", jsonArray);
        return jsonObject;
    }

}
