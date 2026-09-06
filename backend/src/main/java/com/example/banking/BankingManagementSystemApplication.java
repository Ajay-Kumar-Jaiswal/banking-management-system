package com.example.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Banking Management System.
 *
 * This is a portfolio/educational project that demonstrates a realistic,
 * layered Spring Boot backend: Controller -> Service -> Repository -> MySQL,
 * secured with JWT, and consumed by a separate React frontend.
 */
@SpringBootApplication
public class BankingManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingManagementSystemApplication.class, args);
    }
}
