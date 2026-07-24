package com.hasfatempire.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EdusuiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(EdusuiteApplication.class, args);
    }
}
