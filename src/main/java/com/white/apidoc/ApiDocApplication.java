package com.white.apidoc;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
@RequiredArgsConstructor
public class ApiDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiDocApplication.class, args);
    }


}
