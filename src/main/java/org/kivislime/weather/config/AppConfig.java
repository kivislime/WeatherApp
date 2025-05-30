package org.kivislime.weather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:application.properties")
@PropertySource("classpath:secrets.properties")
@ComponentScan(basePackages = {
        "org.kivislime.weather.service",
        "org.kivislime.weather.repository",
        "org.kivislime.weather.security",
        "org.kivislime.weather.mapper",
        "org.kivislime.weather.client"
})
public class AppConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
