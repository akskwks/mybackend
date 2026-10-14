package com.app.mybackend.config;

import com.app.mybackend.user.entity.User;
import com.app.mybackend.user.repository.UserRepository;
import com.app.mybackend.user.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner createInitialAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.existsByUserId("admin")) return;
            User admin = new User();
            admin.setUserId("admin");
            admin.setPassword(encoder.encode("admin"));
            admin.setUserName("관리자");
            admin.setRole("ADMIN");
            admin.setUseYn("Y");
            users.save(admin);
        };
    }
}
