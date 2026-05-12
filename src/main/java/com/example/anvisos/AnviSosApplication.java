package com.example.anvisos;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableAsync
public class AnviSosApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AnviSosApplication.class);
        app.addInitializers(new EnvFileInitializer());
        app.run(args);
    }

    static class EnvFileInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();

            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(".")
                        .ignoreIfMissing()
                        .load();

                Map<String, Object> dotenvMap = new HashMap<>();
                dotenv.entries().forEach(entry -> dotenvMap.put(entry.getKey(), entry.getValue()));

                MapPropertySource propertySource = new MapPropertySource("dotenv", dotenvMap);
                environment.getPropertySources().addFirst(propertySource);
            } catch (Exception e) {
                System.err.println("Error loading .env file: " + e.getMessage());
            }
        }
    }

}
