package ru.hokan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("ru.hokan")
public class AppmasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(new Class<?>[]{AppmasterApplication.class, YarnController.class}, args);
    }
}
