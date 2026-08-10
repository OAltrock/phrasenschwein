package com.convales.phrasenschwein;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.convales.phrasenschwein",
        "controller",
        "service",
        "security",
        "exceptions"
})
@EntityScan("models")
@EnableJpaRepositories("repository")
public class PhrasenSchweinApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhrasenSchweinApplication.class, args);
    }

}
