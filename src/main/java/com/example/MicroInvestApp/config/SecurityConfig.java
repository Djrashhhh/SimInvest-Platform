package com.example.MicroInvestApp.config;

import com.example.MicroInvestApp.impl.usermgmt.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration with JWT Authentication
 * Configures Spring Security for JWT-based stateless authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable method-level security with @PreAuthorize annotations
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * CORS Configuration Bean - This will be used by Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Use allowedOriginPatterns instead of allowedOrigins with credentials
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "https://localhost:3000",
                "https://localhost:5173"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        // Add explicit exposed headers if needed
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Cache-Control",
                "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure HTTP Security with JWT authentication
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Use our custom CORS configuration instead of defaults
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF for stateless API
                .csrf(csrf -> csrf.disable())

                // Configure session management for stateless API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // ✅ Allow OPTIONS requests for CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // FOR FRONTEND ACCESS:
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico",
                                "/static/**", "/assets/**",
                                "/*.js", "/*.css", "/*.map", "/*.ico", "/*.png", "/*.svg",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()


                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/test/public").permitAll()
                        .requestMatchers("/api/v1/securities/test").permitAll()
                        .requestMatchers("/api/v1/securities/test-cors").permitAll()

                        // Market data endpoints
                        .requestMatchers("/api/v1/market-data/market-status").permitAll()
                        .requestMatchers("/api/v1/market-data/health").permitAll()

                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Health check endpoints
                        .requestMatchers("/actuator/health").permitAll()

                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Admin endpoints (require ADMIN role)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Order and Transaction endpoints
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        .requestMatchers("/api/v1/transactions/**").authenticated()

                        //News endpoints
                        .requestMatchers("/api/v1/news/**").authenticated()

                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()

                        // Web pages authentication
                        .anyRequest().authenticated()
                );

        // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Manager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * DAO Authentication Provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}