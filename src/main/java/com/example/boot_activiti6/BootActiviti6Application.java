package com.example.boot_activiti6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.activiti.spring.boot.SecurityAutoConfiguration.class})
public class BootActiviti6Application {

    public static void main(String[] args) {
        SpringApplication.run(BootActiviti6Application.class, args);
    }

}
