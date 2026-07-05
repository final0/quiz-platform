package com.quiz.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.quiz.platform.mapper")
public class QuizPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuizPlatformApplication.class, args);
    }
}
