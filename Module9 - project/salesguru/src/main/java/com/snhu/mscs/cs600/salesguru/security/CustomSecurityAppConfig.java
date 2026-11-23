
package com.snhu.mscs.cs600.salesguru.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * CustomSecurityAppConfig -
 *
 * Purpose:
 *      Configures authentication and authorization for Sales Guru+.
 *      User credentials are retrieved from the existing PostgreSQL
 *      database instead of being stored in memory.
 *
 *      REST endpoints use HTTP Basic authentication, while the
 *      graphical interface uses form-based login.
 *
 * Version              Author          Date            Description
 * ***********************************************************************
 *     1                 fjm            6-Oct-2025      Initial Creation
 *     2                 Chris Bright   17-Nov-2026     Database authentication
 *                                                     and UI security
 */
@Configuration
@EnableWebSecurity
public class CustomSecurityAppConfig {

    /**
     * Verifies BCrypt password hashes stored in PostgreSQL.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Uses the application's existing datasource for authentication.
     */
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    /**
     * Secures REST API endpoints with HTTP Basic authentication.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http)
        throws Exception {

        return http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/version").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable())
            .build();
    }

    /**
     * Requires form-based login for graphical interface pages.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain browserSecurity(HttpSecurity http)
        throws Exception {

        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .build();
    }
}
