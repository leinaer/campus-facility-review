package com.example.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.campus.repository")
public class CampusEvaluationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusEvaluationApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("----------------------------------------------------------");
            System.out.println("🎉 校园评价系统启动成功！访问地址：http://localhost:8080");
            System.out.println("----------------------------------------------------------");
        };
    }

}
