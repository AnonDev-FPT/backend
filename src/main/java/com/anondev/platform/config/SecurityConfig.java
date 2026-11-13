package com.anondev.platform.config;

import com.anondev.platform.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/**",
                                "/webjars/**"
                        ).permitAll()
                        // Candidate-only Technical Test & Attempt endpoints (must be before public jobs matchers)
                        .requestMatchers("/api/v1/jobs/*/test", "/api/v1/jobs/*/test/**").hasAnyRole("CANDIDATE", "ADMIN")
                        // Employer-only Job creation and management endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/jobs").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers("/api/v1/jobs/my-jobs").hasAnyRole("EMPLOYER", "ADMIN")
                        // Public Job listing and details
                        .requestMatchers(HttpMethod.GET, "/api/v1/jobs", "/api/v1/jobs/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/billing/plans").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tournaments", "/api/v1/tournaments/*", "/api/v1/tournaments/*/leaderboard").permitAll()
                        // Admin endpoints (including KYB tax verification)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/companies/*/verify-tax").hasRole("ADMIN")
                        // Candidate endpoints
                        .requestMatchers("/api/v1/candidates/**").hasAnyRole("CANDIDATE", "ADMIN")
                        // Employer endpoints
                        .requestMatchers("/api/v1/companies/**", "/api/v1/billing/**").hasAnyRole("EMPLOYER", "ADMIN")
                        // Any other request authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .map(u -> new org.springframework.security.core.userdetails.User(
                        u.getEmail(),
                        u.getPasswordHash(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
