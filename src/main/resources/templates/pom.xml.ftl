<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>${description.springBootVersion!'3.4.4'}</version>
        <relativePath/>
    </parent>
    
    <groupId>${description.groupId!'com.example'}</groupId>
    <artifactId>${description.artifactId!'my-project'}</artifactId>
    <version>${description.version!'0.0.1-SNAPSHOT'}</version>
    <name>${description.name!description.artifactId}</name>
    
    <properties>
        <java.version>${(description.javaVersion!'17')?replace("^([0-9]+).*", "$1", "r")}</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- Dépendances dynamiques -->
        <#if dependencies??>
            <#list dependencies as dep>
                <#if dep.groupId?? && dep.artifactId?? && dep.groupId?has_content && dep.artifactId?has_content>
                    <dependency>
                        <groupId>${dep.groupId}</groupId>
                        <artifactId>${dep.artifactId}</artifactId>
                        <#if dep.version?has_content>
                            <version>${dep.version}</version>
                        </#if>
                    </dependency>
                </#if>
            </#list>
        </#if>


              
        <#-- Déterminer la version de Lombok en fonction de la version Java -->
<#assign javaMajor = (description.javaVersion!'17')?replace("^([0-9]+).*", "$1", "r")?number>
<#assign useLombokVersion = 
    (javaMajor >= 21)?then("1.18.30", 
        (javaMajor >= 17)?then("1.18.28", "1.18.24")
    )
>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${useLombokVersion}</version>
    <scope>provided</scope>
</dependency>



        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>