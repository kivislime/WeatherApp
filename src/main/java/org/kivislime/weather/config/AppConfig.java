package org.kivislime.weather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySource({"classpath:application.properties", "classpath:secrets.properties"})
@PropertySource(value = "classpath:application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
@ComponentScan(basePackages = {
        "org.kivislime.weather.service",
        "org.kivislime.weather.repository",
        "org.kivislime.weather.security",
        "org.kivislime.weather.mapper",
        "org.kivislime.weather.client",
        "org.kivislime.weather.exception"
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

    @Bean
    public ExecutorService weatherExecutor(@Value("${app.location.max-threads}") int maxThreads) {
        return Executors.newFixedThreadPool(maxThreads);
    }
}