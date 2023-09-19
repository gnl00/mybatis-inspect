package com.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("com.demo.mapper")
@SpringBootApplication

public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(Main.class, args);
    }
}