package com.myapp.rh.auth.config;

import com.myapp.rh.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth


                        // FRONT-END
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // TIMECLOCK
                        .requestMatchers(HttpMethod.POST, "/api/timeclock/clock-in").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/timeclock/clock-out").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/timeclock/me").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/timeclock/adjustment/me").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/timeclock/adjustment").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/timeclock/adjustment/pending").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/timeclock/adjustment/*/approve").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/timeclock/adjustment/*/reject").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/timeclock/*").hasRole("HR_MANAGER")

                        // EMPLOYEES
                        .requestMatchers(HttpMethod.GET, "/api/employees/me").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/employees/**").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/employees/**").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("HR_MANAGER")

                        // VACATIONS
                        .requestMatchers(HttpMethod.POST, "/api/vacations/request").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/vacations/me").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/vacations/all").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/vacations/pending").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/vacations/expiring").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/vacations/*/approve").hasRole("HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/vacations/*/reject").hasRole("HR_MANAGER")

                        // OVERTIME
                        .requestMatchers(HttpMethod.GET, "/api/overtime/me").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/overtime/compensate").hasAnyRole("EMPLOYEE", "HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/overtime/*").hasRole("HR_MANAGER")

                        // NOTIFICATIONS
                        .requestMatchers("/api/notifications/**").hasAnyRole("EMPLOYEE", "HR_MANAGER")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}