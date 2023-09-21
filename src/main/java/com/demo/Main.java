package com.demo;

import com.demo.service.UserService;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        UserService user = new UserService();
        user.pay(1, 100, "2023-09-22");

        YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
        PropertiesPropertySourceLoader propertiesLoader = new PropertiesPropertySourceLoader();
        FileSystemResource fileSystemResource = new FileSystemResource("application.yml");
        ClassPathResource propertiesClassPathResource = new ClassPathResource("application.properties");
        ClassPathResource yamlClassPathResource = new ClassPathResource("application.yml");

        List<PropertySource<?>> loadedYaml = yamlLoader.load("application", yamlClassPathResource);
        List<PropertySource<?>> loadedProperties = propertiesLoader.load("application", propertiesClassPathResource);
        System.out.println(loadedYaml.size());
        for (PropertySource<?> propertySource : loadedYaml) {
            String value = (String) propertySource.getProperty("mb.type-handler-pkg");
            System.out.println(propertySource.getProperty(value));
        }
    }
}
