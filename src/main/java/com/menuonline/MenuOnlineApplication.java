package com.menuonline;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.menuonline.service.StripeService;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
@ConfigurationPropertiesScan
public class MenuOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuOnlineApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(StripeService stripeService) {
        return a -> {
            // stripeService.generateChangePaymentMethodUrl("cus_UAlUQyea60F4Vl", "sub_1TCPxbRsjQxNujCX2uOhgTJK");
        };
    }

}
