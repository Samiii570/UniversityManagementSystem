package com.kuet.ums.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/favicon.ico", "/api/auth/login", "/error").permitAll()
                .requestMatchers("/api/admins/**").hasRole("ADMIN")
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, authentication) -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("message", "Login successful");
                    body.put("username", authentication.getName());
                    body.put("roles", authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).toList());
                    writeJson(res, HttpStatus.OK, body);
                })
                .failureHandler((req, res, ex) ->
                    writeJson(res, HttpStatus.UNAUTHORIZED, error(HttpStatus.UNAUTHORIZED,
                            "Invalid username or password", req.getRequestURI())))
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, authentication) ->
                    writeJson(res, HttpStatus.OK, Map.of("message", "Logout successful")))
            )

            .httpBasic(Customizer.withDefaults())

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    writeJson(res, HttpStatus.UNAUTHORIZED, error(HttpStatus.UNAUTHORIZED,
                            "Authentication required", req.getRequestURI())))
                .accessDeniedHandler((req, res, e) ->
                    writeJson(res, HttpStatus.FORBIDDEN, error(HttpStatus.FORBIDDEN,
                            "You do not have permission to access this resource", req.getRequestURI())))
            );

        return http.build();
    }

    private Map<String, Object> error(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    private void writeJson(HttpServletResponse res, HttpStatus status, Object body) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getOutputStream(), body);
    }
}
