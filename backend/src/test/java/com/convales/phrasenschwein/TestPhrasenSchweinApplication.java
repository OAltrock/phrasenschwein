package com.convales.phrasenschwein;

import org.springframework.boot.SpringApplication;

public class TestPhrasenSchweinApplication {

    public static void main(String[] args) {
        SpringApplication.from(PhrasenSchweinApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
