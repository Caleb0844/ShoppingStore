package com.caleb.Creator_store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Registers the shared password hashing strategy used by account services.
// Reuse the PasswordEncoder bean; never hash passwords manually or store raw credentials.
@Configuration
public class PasswordConfig {

    // Provides one BCrypt encoder for registration and future login password checks.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
