package com.tkg.gscm.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

public class CorsCustomizer implements Customizer<CorsConfigurer<HttpSecurity>> {

    @Override
    public void customize(CorsConfigurer<HttpSecurity> corsConfigurer) {
        corsConfigurer
                .configurationSource(customCorsConfigurationSource());
    }

    private CorsConfigurationSource customCorsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration corsConfiguration = new CorsConfiguration();
//                corsConfiguration.addAllowedOrigin("http://localhost:4200");
                corsConfiguration.addAllowedOrigin("*");
                corsConfiguration.addAllowedMethod("*");
                corsConfiguration.addAllowedHeader("*");
                return corsConfiguration;
            }
        };
    }
}
