package com.sos.lms.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary  // Add this annotation
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {  // Rename method
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/**").permitAll();

        return http.build();
    }
}