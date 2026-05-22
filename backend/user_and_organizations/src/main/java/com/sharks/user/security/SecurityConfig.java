package com.sharks.user.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT via {@link JwtAuthenticationFilter} + {@link JwtUtils} (OIDC discovery / JWKS), matching events and ticketing.
 *
 * <p>CORS is intentionally NOT configured here. All browser traffic enters via the API gateway,
 * which owns CORS. Adding CORS at this layer too produces duplicated
 * {@code Access-Control-Allow-Origin} headers (gateway + service), which browsers reject with
 * a "Failed to fetch" error even on 200 responses.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtUtils jwtUtils;

    public SecurityConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizers/*").permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .requestMatchers("/api/v1/attendees/**").authenticated()
                        .requestMatchers("/api/v1/organizers/**").authenticated()
                        .requestMatchers("/api/v1/admins/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        log.info("[jwt:security-chain] layer=SecurityFilterChain, step=registered, jwtFilter=JwtAuthenticationFilter");
        return http.build();
    }

    /**
     * Logs 401 decisions (authorization layer) before returning HTTP 401.
     */
    private static AuthenticationEntryPoint loggingUnauthorizedEntryPoint() {
        HttpStatusEntryPoint delegate = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            log.warn("[jwt:entry-point] layer=AuthenticationEntryPoint, step=commence, method={}, path={}, "
                            + "exceptionType={}, message={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    authException != null ? authException.getClass().getSimpleName() : "null",
                    authException != null ? authException.getMessage() : "");
            delegate.commence(request, response, authException);
        };
    }
}
