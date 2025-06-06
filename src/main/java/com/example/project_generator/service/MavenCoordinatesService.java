package com.example.project_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MavenCoordinatesService {

    @Value("${github.model.token}")
    private String githubToken;

    @Value("${github.model.endpoint}")
    private String endpoint;

    @Value("${github.model.name}")
    private String modelName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Coordinates resolve(String dependencyName) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = Map.of(
                    "model", modelName,
                    "messages", List.of(
                            Map.of("role", "system",
                                    "content", "You are a helpful assistant that gives Maven dependency info."),
                            Map.of("role", "user",
                                    "content", "Return ONLY a valid JSON object with the Maven coordinates for the Spring dependency '" + dependencyName + "'. Format:\n" +
                                            "{ \"groupId\": \"...\", \"artifactId\": \"...\", \"version\": \"...\" }\n" +
                                            "⚠️ Do not include any explanation, comment, or text.")
                    ),
                    "temperature", 0.0
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(githubToken);
            headers.setAccept(List.of(MediaType.valueOf("application/vnd.github+json")));
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String result = root.path("choices").get(0).path("message").path("content").asText();
                JsonNode json = objectMapper.readTree(result);

                return new Coordinates(
                        json.path("groupId").asText(),
                        json.path("artifactId").asText(),
                        json.path("version").asText()
                );
            }
        } catch (Exception e) {
            System.err.println(" GPT resolution failed: " + e.getMessage());
        }

        return null;
    }

    public static class Coordinates {
        public String groupId, artifactId, version;

        public Coordinates(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }
}
