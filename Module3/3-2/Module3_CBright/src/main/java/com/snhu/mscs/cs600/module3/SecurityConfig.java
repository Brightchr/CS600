package com.snhu.mscs.cs600.module3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig -
 * Version              Author                  Date            Description
 * ***********************************************************************
 *     1                 Christopher Bright      07-Oct-2026     Initial Creation
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides the password encoder used by Spring Security.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates the in-memory user used for authentication in this activity.
     */
    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder
    ) {

        UserDetails user = User.builder()
                .username("namedUser")
                .password(passwordEncoder.encode("names.123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Configures access rules for the application's API endpoints.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/version")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}