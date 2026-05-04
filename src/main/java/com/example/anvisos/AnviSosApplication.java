package com.example.anvisos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AnviSosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnviSosApplication.class, args);
    }

}
