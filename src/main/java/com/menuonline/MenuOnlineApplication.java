package com.menuonline;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import lombok.extern.slf4j.Slf4j;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
@Slf4j
public class MenuOnlineApplication {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public static void main(String[] args) {
        SpringApplication.run(MenuOnlineApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner() {
        return (args) -> {
            System.out.println("#####################");
            log.info("main - dbUrl: {}", dbUrl);
        };
    }

}
