package com.laklu.pos.auth.config;

import com.laklu.pos.auth.RestAccessDeniedHandler;
import com.laklu.pos.auth.RestAuthenticationEntryPoint;
import com.laklu.pos.filters.JwtFilter;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${web.cors.allowed-origins}")
    private String webCorsOrigin;
    @Value("${app-setup.alreadySetup}")
    private Boolean alreadySetup;
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HttpSession httpSession) throws Exception {
        var whiteListRoutes = new ArrayList<String>();

        whiteListRoutes.add("/api/v1/auth/login");
        whiteListRoutes.add("/error");
        whiteListRoutes.add("/swagger-ui/**");
        whiteListRoutes.add("/v3/api-docs/**");
        whiteListRoutes.add("/swagger-resources/**");
        whiteListRoutes.add("/webjars/**");
        whiteListRoutes.add("/api/v1/payments/webhook/sepay");
        whiteListRoutes.add("/api/v1/attachments/**");


        if(!alreadySetup) {
            whiteListRoutes.add("/api/v1/setup/permissions");
            whiteListRoutes.add("/api/v1/setup/super-admin");
        }

        http.cors(withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests((authorizeRequests) ->
            authorizeRequests
                .requestMatchers(whiteListRoutes.toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
        );
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling((exceptionHandling) ->
            exceptionHandling
                .accessDeniedHandler(new RestAccessDeniedHandler())
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
        );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configurationSource = new CorsConfiguration();
        configurationSource.addAllowedOrigin("*");
        configurationSource.addAllowedHeader("*");
        configurationSource.addAllowedMethod(HttpMethod.GET);
        configurationSource.addAllowedMethod(HttpMethod.POST);
        configurationSource.addAllowedMethod(HttpMethod.PUT);
        configurationSource.addAllowedMethod(HttpMethod.DELETE);
        configurationSource.addAllowedMethod(HttpMethod.OPTIONS);
        configurationSource.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configurationSource);
        return source;
    }

}
