package com.subnotway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.subnotway", "controlador", "servicio", "dao", "config"})
public class SubnotwayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubnotwayApplication.class, args);
    }
}
