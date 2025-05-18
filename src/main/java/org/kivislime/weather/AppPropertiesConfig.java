package org.kivislime.weather;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

//TODO: Поддержка полей аннотаций типа @Value. Чтобы не пришлось добавлять в WebAppInitializer потому что AppPropertiesConfig нашли другие конфиги. Перенести по папкам исправить пути org.kivislime.weather
@Configuration
@ComponentScan(basePackages = "org.kivislime.weather")
@PropertySource("classpath:application.properties")
@PropertySource("classpath:secrets.properties")
public class AppPropertiesConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
