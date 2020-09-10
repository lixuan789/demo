package com.example.demo;

import com.example.demo.ui.Web;
import com.example.demo.utils.viewDemo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Random;
import java.util.Scanner;

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class DemoApplication {

    public static String port;

    public static void main(String[] args) {
        Random random = new Random();
        port=String.valueOf(random.nextInt(Math.abs(65536 - 1024)) + 1024);
        new SpringApplicationBuilder(DemoApplication.class).properties("server.port=" + port).run(args);
//        SpringApplication.run(DemoApplication.class, args);
        viewDemo.main(args);
    }

}
