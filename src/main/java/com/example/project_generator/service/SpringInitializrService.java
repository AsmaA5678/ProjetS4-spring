package com.example.project_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.version.VersionReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SpringInitializrService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MavenCoordinatesService coordinatesService;
    /**
     * Récupère et formate les groupes de dépendances à afficher dans l'interface CLI
     */
    public List<Map<String, Object>> fetchDependencyGroups() {
        String url = "https://start.spring.io/metadata/client?format=dependencies";

        try {
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode groupsNode = root.path("dependencies").path("values");

            List<Map<String, Object>> groups = new ArrayList<>();
            for (JsonNode groupNode : groupsNode) {
                String name = groupNode.path("name").asText();
                List<Map<String, String>> deps = new ArrayList<>();
                for (JsonNode depNode : groupNode.path("values")) {
                    Map<String, String> dep = new LinkedHashMap<>();
                    dep.put("id", depNode.path("id").asText());
                    dep.put("name", depNode.path("name").asText());
                    deps.add(dep);
                }
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("name", name);
                group.put("items", deps);
                groups.add(group);
            }
            return groups;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des dépendances dynamiques : " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Résout les IDs de dépendances sélectionnées vers des objets Dependency prêts pour Freemarker
     */
    public Map<String, Dependency> resolveDependencies(Set<String> ids) {
        Map<String, Dependency> result = new LinkedHashMap<>();
        String url = "https://start.spring.io/metadata/client";
        try {
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode allDeps = root.path("dependencies").path("values");

            for (JsonNode group : allDeps) {
                for (JsonNode depNode : group.path("values")) {
                    String id = depNode.path("id").asText();
                    if (ids.contains(id)) {
                        String groupId = depNode.path("groupId").asText(null);
                        String artifactId = depNode.path("artifactId").asText(null);
                        String version = depNode.has("version") ? depNode.path("version").asText(null) : null;

                        if (groupId == null || artifactId == null || groupId.isBlank() || artifactId.isBlank()) {
                            System.err.println("⚠️ Dépendance incomplète : ID=" + id);

                            // 🧠 GPT fallback
                            MavenCoordinatesService.Coordinates coords = coordinatesService.resolve(id);
                            if (coords != null) {
                                Dependency.Builder builder = Dependency.withCoordinates(coords.groupId, coords.artifactId)
                                        .scope(DependencyScope.COMPILE);
                                if (coords.version != null && !coords.version.isBlank()) {
                                    builder = builder.version(VersionReference.ofValue(coords.version));
                                }
                                Dependency resolved = builder.build();
                                result.put(id, resolved);
                                System.out.println("✅ Complété par GPT : " + coords.groupId + ":" + coords.artifactId + ":" + coords.version);
                            } else {
                                System.out.println("❌ GPT n'a pas pu résoudre : " + id);
                            }
                            continue; // important pour ne pas traiter 2 fois
                        }


                        Dependency.Builder builder = Dependency.withCoordinates(groupId, artifactId)
                                .scope(DependencyScope.COMPILE);

                        if (version != null && !version.isEmpty()) {
                            builder = builder.version(VersionReference.ofValue(version));
                        }

                        Dependency dep = builder.build();
                        result.put(id, dep);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la résolution des dépendances : " + e.getMessage());
        }
        return result;
    }

}
