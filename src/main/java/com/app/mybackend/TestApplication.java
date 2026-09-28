package com.app.mybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
//        SpringApplication.run(MybackendApplication.class, args);

        String a = "My name is";
        String b = " Who are you";
        String c = "123";

        char d = a.charAt(9);
        byte[] e = c.getBytes(StandardCharsets.UTF_8);
        int f = Integer.parseInt(c);


        System.out.println(f);
    }
}
