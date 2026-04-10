package com.example.silverpear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SilverPearApplication {

    public static void main(String[] args) {
        SpringApplication.run(SilverPearApplication.class, args);
    }

}
