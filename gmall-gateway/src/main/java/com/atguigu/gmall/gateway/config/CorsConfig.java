package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
       corsConfiguration.addAllowedHeader("*");
       corsConfiguration.addAllowedOrigin("http://manager.gmall.com");
       corsConfiguration.addAllowedOrigin("http://localhost:1000");
       corsConfiguration.addAllowedOrigin("http://www.gmall.com");
       corsConfiguration.addAllowedOrigin("http://gmall.com");
       corsConfiguration.addAllowedMethod("*");
       corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);

        return new CorsWebFilter(configurationSource);
    }
}
