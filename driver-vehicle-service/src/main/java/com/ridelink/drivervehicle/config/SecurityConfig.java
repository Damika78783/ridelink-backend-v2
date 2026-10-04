package com.ridelink.drivervehicle.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${ridelink.security.jwt.enabled:false}")
    private boolean jwtSecurityEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Always permit Swagger / OpenAPI documentation
                    auth.requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/actuator/**"
                    ).permitAll();

                    // Always permit inter-service endpoint for Ride Management Service
                    auth.requestMatchers(HttpMethod.GET, "/api/drivers/available").permitAll();

                    if (!jwtSecurityEnabled) {
                        // Development / Demonstration mode: allow open access to endpoints
                        auth.anyRequest().permitAll();
                    } else {
                        // Strict Production mode: require authentication for other endpoints
                        auth.requestMatchers(HttpMethod.GET, "/api/drivers/**", "/api/vehicles/**").permitAll();
                        auth.anyRequest().authenticated();
                    }
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
