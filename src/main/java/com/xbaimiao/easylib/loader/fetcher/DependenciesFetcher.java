package com.xbaimiao.easylib.loader.fetcher;

import com.xbaimiao.easylib.loader.DependencyLoader;
import com.xbaimiao.easylib.loader.Loader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependenciesFetcher {
    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //private static EasyPlugin easyPlugin = null;

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        List<DependencyLoader.Dependency> dependencies =
                fetchDependencies("net.kyori:adventure-platform-bukkit:4.3.2",
                        Loader.ALIYUN_REPO_URL,
                        new HashMap<String,String>(){{
                            put("nms;l","dsadsa");
                        }});
        dependencies.forEach(System.out::println);
        System.out.println((System.currentTimeMillis() - time) + "ms");
    }

    public static List<DependencyLoader.Dependency> fetchDependencies(String dependency, String repoUrl, Map<String, String> relocationRules) {
        List<DependencyLoader.Dependency> cacheDependency = FetcherCacheManager.getCache(dependency, repoUrl, relocationRules);
        if (cacheDependency != null) {
            return cacheDependency;
        }

        List<DependencyLoader.Dependency> dependencies = fetchDependencies(dependency, repoUrl, relocationRules, 0);
        FetcherCache fetcherCache = new FetcherCache(dependency, repoUrl, dependencies);
        FetcherCacheManager.addCache(fetcherCache);
        return dependencies;
    }

    private static List<DependencyLoader.Dependency> fetchDependencies(String dependency, String repoUrl, Map<String, String> relocationRules, int deep) {
        List<DependencyLoader.Dependency> list = new ArrayList<>();
        if (dependency.contains("@")) {
            if (dependency.endsWith("@jar")) {
                list.add(Loader.toDependenency(Loader.dependencyToUrl(dependency, repoUrl), repoUrl, relocationRules));
            }
            return list;
        }
        Map<String, String> versionVariables = new HashMap<>();
        String pomDependency = dependency + "@pom";
        String url = Loader.dependencyToUrl(pomDependency, repoUrl).getKey();
        Document document = fetchUrlContent(url);
        if (document == null) return list;
        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        handleDependencyManagement(nodeList, repoUrl, versionVariables);
        List<Artifact> artifacts = rootNodeListToDependencies(nodeList);
        for (Artifact artifact : artifacts) {
            if (artifact.version.isEmpty()) {
                String identify = artifact.group + ":" + artifact.module;
                if (versionVariables.containsKey(identify)) {
                    artifact.setVersion(versionVariables.get(identify));
                } else continue;
            }
            if (artifact.scope.equalsIgnoreCase("compile")) {
                if (artifact.extension.equalsIgnoreCase("jar")) {
                    list.add(Loader.toDependenency(Loader.dependencyToUrl(artifact.toString(), repoUrl), repoUrl, relocationRules));
                }
                if (deep < 3) {
                    List<DependencyLoader.Dependency> subDependencies = fetchDependencies(artifact.toString(), repoUrl, relocationRules, deep + 1);
                    list.addAll(subDependencies);
                }
            }
        }
        list.add(Loader.toDependenency(Loader.dependencyToUrl(dependency, repoUrl), repoUrl, relocationRules));
        return Loader.cleanDependencies(list);
    }

    private static void processBom(String bomDependency, String repoUrl, Map<String, String> versions) {
        String url = Loader.dependencyToUrl(bomDependency, repoUrl).getKey();
        Document document = fetchUrlContent(url);
        if (document == null) return;
        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        handleDependencyManagement(nodeList, repoUrl, versions);
    }

    private static void handleDependencyManagement(NodeList nodeList, String repoUrl, Map<String, String> versions) {
        List<Artifact> managedDependencies = dependencyManagementToArtifacts(nodeList);
        for (Artifact artifact : managedDependencies) {
            if (artifact.extension.equalsIgnoreCase("pom") && artifact.scope.equalsIgnoreCase("import")) {
                processBom(artifact.toString(), repoUrl, versions);
            } else {
                String identify = artifact.group + ":" + artifact.module;
                if (versions.containsKey(identify)) {
                    int numericVersion = Loader.toNumericVersion(versions.get(identify));
                    int currentVersion = Loader.toNumericVersion(artifact.version);
                    if (currentVersion > numericVersion) {
                        versions.put(identify, artifact.version);
                    }
                } else {
                    versions.put(identify, artifact.version);
                }
            }
        }
    }

    private static List<Artifact> dependencyManagementToArtifacts(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            if (node.getNodeName().equalsIgnoreCase("dependencyManagement")) {
                NodeList managementNodes = node.getChildNodes();
                return rootNodeListToDependencies(managementNodes);
            }
        }
        return new ArrayList<>();
    }

    private static List<Artifact> rootNodeListToDependencies(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node dependenciesNode = nodeList.item(i);
            if (dependenciesNode.getNodeType() != Node.ELEMENT_NODE) continue;
            if (dependenciesNode.getNodeName().equalsIgnoreCase("dependencies")) {
                return dependenciesToArtifacts(dependenciesNode);
            }
        }
        return new ArrayList<>();
    }

    private static Artifact nodeToArtifact(Node node) {
        String group = "";
        String module = "";
        String version = "";
        String classifier = "";
        String extension = "jar";
        String scope = "";
        NodeList variableList = node.getChildNodes();
        for (int i = 0; i < variableList.getLength(); i++) {
            Node variableNode = variableList.item(i);
            if (variableNode.getNodeType() != Node.ELEMENT_NODE) continue;
            String value = variableNode.getFirstChild().getNodeValue();
            switch (variableNode.getNodeName()) {
                case "groupId": {
                    group = value;
                    break;
                }
                case "artifactId": {
                    module = value;
                    break;
                }
                case "version": {
                    version = value;
                    break;
                }
                case "type": {
                    extension = value;
                    break;
                }
                case "scope": {
                    scope = value;
                    break;
                }
                case "classifier": {
                    classifier = value;
                    break;
                }
            }
        }
        return new Artifact(group, module, version, classifier, scope, extension);
    }

    private static List<Artifact> dependenciesToArtifacts(Node node) {
        List<Artifact> artifacts = new ArrayList<>();
        NodeList dependencyNodeList = node.getChildNodes();
        for (int dependenciesIndex = 0; dependenciesIndex < dependencyNodeList.getLength(); dependenciesIndex++) {
            Node dependencyNode = dependencyNodeList.item(dependenciesIndex);
            if (dependencyNode.getNodeType() != Node.ELEMENT_NODE) continue;
            if (dependencyNode.getNodeName().equalsIgnoreCase("dependency")) {
                Artifact artifact = nodeToArtifact(dependencyNode);
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

    private static Document fetchUrlContent(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(5000);
            InputStream inputStream = connection.getInputStream();

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            //if (easyPlugin.getDebug()) e.printStackTrace(System.err);
            return null;
        }
    }

    public static class Artifact {
        private final String group;
        private final String module;
        private final String classifier;
        private final String scope;
        private final String extension;
        private String version;

        public Artifact(String group, String module, String version, String classifier, String scope, String extension) {
            this.group = group;
            this.module = module;
            this.version = version;
            this.classifier = classifier;
            this.scope = scope;
            this.extension = extension;
        }

        public Artifact(String group, String module, String version, String classifier, String scope) {
            this(group, module, version, classifier, scope, "jar");
        }

        public Artifact(String group, String module, String version, String scope) {
            this(group, module, version, "", scope);
        }

        public String getGroup() {
            return group;
        }

        public String getModule() {
            return module;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getScope() {
            return scope;
        }

        public String getClassifier() {
            return classifier;
        }

        public String getExtension() {
            return extension;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(group);
            builder.append(":").append(module);
            builder.append(":").append(version);
            if (!classifier.isEmpty()) {
                builder.append(":").append(classifier);
            }
            if (!extension.isEmpty() && !extension.equalsIgnoreCase("jar")) {
                builder.append("@").append(extension);
            }
            return builder.toString();
        }
    }
}
