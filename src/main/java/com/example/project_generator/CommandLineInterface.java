package com.example.project_generator;

import com.example.project_generator.controller.ProjectGeneratorController;
import com.example.project_generator.model.CustomProjectRequest;
import com.example.project_generator.model.FieldDefinition;
import com.example.project_generator.util.MavenVersionResolver;
import com.example.project_generator.ia.DeepSeekIAService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommandLineInterface implements CommandLineRunner {

    private final ProjectGeneratorController projectGeneratorController;

    @Autowired
    private com.example.project_generator.service.SpringInitializrService springInitializrService;

    @Autowired
    public CommandLineInterface(ProjectGeneratorController projectGeneratorController) {
        this.projectGeneratorController = projectGeneratorController;
    }

    @Autowired
    private DeepSeekIAService deepSeekIAService;


    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        CustomProjectRequest request = new CustomProjectRequest();

        System.out.println("=== Spring Project Generator ===");
        
        
        System.out.print("Nom du projet: ");
        request.setName(scanner.nextLine());

        System.out.print("Group  (défaut: com.example): ");
        String groupId = scanner.nextLine();
        request.setGroupId(groupId.isEmpty() ? "com.example" : groupId);
        
        System.out.print("Artifact : ");
        String artifactId = scanner.nextLine().trim(); 
        if (artifactId.isEmpty()) {
           System.err.println("Erreur : L'Artifact ID ne peut pas être vide");
           return;
        }
        request.setArtifactId(artifactId);
        
        System.out.print("Version Java (défaut: 17): ");
        String javaVersion = scanner.nextLine();
        request.setJavaVersion(javaVersion.isEmpty() ? "17" : javaVersion);
        request.setMavenVersion(MavenVersionResolver.resolve(request.getJavaVersion()));

        
       System.out.print("Version Spring Boot (défaut: 3.4.4): ");
       String springBootVersion = scanner.nextLine();
       request.setSpringBootVersion(springBootVersion.isEmpty() ? "3.4.4" : springBootVersion);

        System.out.print("Build tool (1. Maven, 2. Gradle Groovy, 3. Gradle Kotlin) - défaut 1: ");
        String buildToolChoice = scanner.nextLine();
        request.setBuildTool(
            switch (buildToolChoice) {
             case "2" -> "gradle-groovy";
             case "3" -> "gradle-kotlin";
             default -> "maven";
            });

        
        System.out.print("Port (défaut: 8080): ");
        String portInput = scanner.nextLine();
        request.setPort(portInput.isEmpty() ? 8080 : Integer.parseInt(portInput));
        
        System.out.print("Profile (défaut: dev): ");
        String profile = scanner.nextLine();
        request.setProfile(profile.isEmpty() ? "dev" : profile);

     
        System.out.print("Générer Docker ? (y/n): ");
        request.setGenerateDocker(scanner.nextLine().equalsIgnoreCase("y"));
        
        if (request.isGenerateDocker()) {
            System.out.print("Docker repository (défaut: your-default-repo): ");
            String dockerRepo = scanner.nextLine();
            request.setDockerRepository(dockerRepo.isEmpty() ? "your-default-repo" : dockerRepo);
        }
        
        System.out.print("Générer Kubernetes ? (y/n): ");
        request.setGenerateKubernetes(scanner.nextLine().equalsIgnoreCase("y"));
        
        System.out.print("Générer CI/CD (GitLab CI) ? (y/n): ");
        request.setGenerateCLCG(scanner.nextLine().equalsIgnoreCase("y"));

        System.out.println("Type d'architecture (choisissez un numéro):");
        System.out.println("1. Hexagonale");
        System.out.println("2. En couches");
        System.out.print("Votre choix: ");
        int archChoice = Integer.parseInt(scanner.nextLine());
        request.setArchitectureType(switch(archChoice) {
            case 1 -> "hexagonale";
            case 2 -> "en-couches";
            default -> "standard";
        });


        // Dépendances dynamiques depuis start.spring.io
        List<Map<String, Object>> groups = springInitializrService.fetchDependencyGroups();
        System.out.println("➡️ Nombre de groupes de dépendances : " + groups.size());

        List<String> allDepIds = new ArrayList<>();
        int index = 1;

        System.out.println("\n*** Dépendances disponibles par catégorie ***");
        for (Map<String, Object> group : groups) {
            String groupName = (String) group.get("name");
            System.out.println("[" + groupName + "]");

            List<Map<String, String>> items = (List<Map<String, String>>) group.get("items");
            for (Map<String, String> dep : items) {
                System.out.printf("  %d. %s (ID: %s)%n", index, dep.get("name"), dep.get("id"));
                allDepIds.add(dep.get("id"));
                index++;
            }
        }

        System.out.print("\nEntrez les numéros ou les IDs des dépendances à ajouter (séparés par des virgules ou des espaces) : ");
        String depsinput = scanner.nextLine().trim();
        Set<String> selectedIds = new LinkedHashSet<>();

        String[] tokens = depsinput.split("[,\\s]+");
        for (String token : tokens) {
            if (token.matches("\\d+")) {
                int idx = Integer.parseInt(token);
                if (idx >= 1 && idx <= allDepIds.size()) {
                    selectedIds.add(allDepIds.get(idx - 1));
                }
            } else {
                selectedIds.add(token);
            }
        }
        request.setDependencies(selectedIds);

        
        List<String> entities = new ArrayList<>();
        System.out.println("Entrez les noms des entités (une par ligne, vide pour terminer):");
        while (true) {
            System.out.print("Entité: ");
            String entity = scanner.nextLine();
            if (entity.isEmpty()) break;
            entities.add(entity);
        }
        request.setEntities(entities);
        Map<String, List<FieldDefinition>> entityFields = new HashMap<>();

for (String entity : entities) {
    System.out.println("\n➡️  Définir les champs pour l'entité : " + entity);
    List<FieldDefinition> fields = new ArrayList<>();

    while (true) {
        System.out.print("Nom du champ (laisser vide pour terminer): ");
        String fieldName = scanner.nextLine();
        if (fieldName.isEmpty()) break;

        System.out.print("Type du champ (String, Long, Integer, Boolean, LocalDate, etc.): ");
        String fieldType = scanner.nextLine().trim();
        if (fieldType.isEmpty()) fieldType = "String";

        System.out.print("Est-ce la clé primaire ? (y/n): ");
        boolean isPrimary = scanner.nextLine().equalsIgnoreCase("y");

        System.out.print("Not null ? (y/n): ");
        boolean notNull = scanner.nextLine().equalsIgnoreCase("y");

        FieldDefinition field = new FieldDefinition(fieldName, fieldType, isPrimary, notNull);
        fields.add(field);
    }

    entityFields.put(entity, fields);
}

request.setEntityFields(entityFields);


        Map<String, Boolean> restEndpointChoices = new HashMap<>();
        for (String entity : entities) {
           System.out.print("Ajouter des endpoints REST pour " + entity + " ? (y/n): ");
           String input = scanner.nextLine();
           restEndpointChoices.put(entity, input.equalsIgnoreCase("y"));
        }
        request.setRestEndpoints(restEndpointChoices);

        System.out.print("Générer les classes de test ? (y/n, défaut y): ");
        request.setGenerateTests(!scanner.nextLine().equalsIgnoreCase("n"));


        System.out.println("\n🤖 Recommandations IA (DeepSeek) :");

        String summary = String.format("""
        Projet: %s
        Dépendances: %s
        Docker: %s
        Kubernetes: %s
        Architecture: %s
        """, 
        request.getArtifactId(),
        request.getDependencies(),
        request.isGenerateDocker(),
        request.isGenerateKubernetes(),
        request.getArchitectureType());

        System.out.println(deepSeekIAService.getSecurityAdvice(summary));

        System.out.println("\nGénération du projet en cours...");
        try {
            byte[] zipBytes = projectGeneratorController.generateProject(request).getBody();
            String fileName = request.getArtifactId() + ".zip";
            java.nio.file.Files.write(java.nio.file.Path.of(fileName), zipBytes);
            System.out.println("Projet généré avec succès dans le fichier: " + fileName);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du projet: " + e.getMessage());
        }
        
        scanner.close();
    }
}