package com.gateway.payment.security.config;

import com.gateway.payment.omnicanal.canal.CanalApiKeyFilter;
import com.gateway.payment.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CanalApiKeyFilter canalApiKeyFilter;
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CanalApiKeyFilter canalApiKeyFilter,
            SecurityProperties securityProperties,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.canalApiKeyFilter = canalApiKeyFilter;
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/ping",
                                "/api/v1/i18n/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/webhooks/**").permitAll()
                        // Canal Omnicanal (auth via X-Api-Key — CanalApiKeyFilter)
                        .requestMatchers(
                                "/api/v1/boundaryByTypeMethode",
                                "/api/boundaryByTypeMethode",
                                "/api/v1/boundary",
                                "/api/boundary",
                                "/api/v1/omnicanalPresentation",
                                "/api/omnicanalPresentation",
                                "/api/v1/canal/**"
                        ).permitAll()
                        // Catalogue publié : lu par les canaux (mobile, portails) avec le token de l'utilisateur final
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/admin/catalog-services/published",
                                "/api/v1/admin/catalog-services/published/**"
                        ).authenticated()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "CONFIG_MANAGER", "SUPPORT", "AUDITOR", "OPERATOR")
                        // RapidFrame admin (meta / designer / views) — pas le runtime /api/v1/payments*
                        .requestMatchers(
                                "/api/meta/**",
                                "/api/designer/**",
                                "/api/forms/**",
                                "/api/grids/**",
                                "/api/menus/**",
                                "/api/modules",
                                "/api/modules/**",
                                "/api/application",
                                "/api/application/**",
                                "/api/action-views/**",
                                "/api/relations/**",
                                "/api/features/**",
                                "/api/action-validates/**",
                                "/api/customs/**",
                                "/api/charts/**",
                                "/api/dashboards/**",
                                "/api/searches/**",
                                "/api/themes/**",
                                "/api/templates/**",
                                "/api/cards/**",
                                "/api/trees/**",
                                "/api/quick-menus/**",
                                "/api/method-actions/**",
                                "/api/record-actions/**",
                                "/api/condition-actions/**",
                                "/api/group-actions/**",
                                "/api/import-actions/**",
                                "/api/export-actions/**",
                                "/api/bpm/**",
                                "/api/dmn/**"
                        ).hasAnyRole("ADMIN", "CONFIG_MANAGER", "SUPPORT", "AUDITOR", "OPERATOR")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Authentification requise"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN, "ACCESS_DENIED", "Accès refusé"))
                )
                .addFilterBefore(canalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(securityProperties.cors().allowedOrigins().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Trace-Id", "Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = new ApiError(
                Instant.now(),
                status,
                code,
                message,
                null,
                MDC.get("traceId"),
                null
        );
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
