package org.kivislime.weather.config;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.kivislime.weather.repository")
public class JpaConfig {

    @Bean
    public DataSource dataSource(@Value("${spring.datasource.driver-class-name}") String driverName,
                                 @Value("${spring.datasource.url}") String datasourceUrl,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverName);
        ds.setUrl(datasourceUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       @Value("${spring.jpa.hibernate.ddl-auto}") String ddlAuto) {
        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("org.kivislime.weather.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProps = new Properties();
        jpaProps.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        factory.setJpaProperties(jpaProps);
        return factory;
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource,
                                     @Value("${spring.liquibase.change-log}") String changeLog) {
        SpringLiquibase liq = new SpringLiquibase();
        liq.setDataSource(dataSource);
        liq.setChangeLog(changeLog);
        liq.setShouldRun(true);
        return liq;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}