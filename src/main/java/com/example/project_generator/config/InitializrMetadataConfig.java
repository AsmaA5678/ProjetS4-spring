package com.example.project_generator.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(InitializrProperties.class)
public class InitializrMetadataConfig {

    @Bean
    public InitializrMetadataUpdateStrategy updateStrategy(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        RestTemplate restTemplate = builder.build();
        return current -> {
            String url = "https://start.spring.io/metadata/client";
            try {
                String json = restTemplate.getForObject(url, String.class);

                // Lecture JSON et nettoyage en profondeur
                JsonNode root = objectMapper.readTree(json);

                // Patch tous les "type" mal formatés
                replaceEnumTypesRecursively(root);

                // Converti l'arbre JSON corrigé en string
                String cleanedJson = objectMapper.writeValueAsString(root);

                // Lecture vers InitializrMetadata
                InitializrMetadata updated = objectMapper.readValue(cleanedJson, InitializrMetadata.class);
                updated.validate();
                return updated;

            } catch (Exception e) {
                System.err.println(" Échec de mise à jour des métadonnées : " + e.getMessage());
                return current;
            }
        };
    }

    private void replaceEnumTypesRecursively(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                if ("type".equals(entry.getKey()) && entry.getValue().isTextual()) {
                    String original = entry.getValue().asText();
                    String uppercased = original.toUpperCase().replace("-", "_");
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("type", uppercased);
                } else {
                    replaceEnumTypesRecursively(entry.getValue());
                }
            });
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                replaceEnumTypesRecursively(item);
            }
        }
    }

    @Bean
    public InitializrMetadataProvider initializrMetadataProvider(
            InitializrProperties props,
            InitializrMetadataUpdateStrategy updateStrategy
    ) {
        InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(props).build();
        return new DefaultInitializrMetadataProvider(metadata, updateStrategy);
    }
}
