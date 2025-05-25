package ru.webrise.technicaltask.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("postgres")
@PropertySource("classpath:application-postgres.properties")
public class PostgresConfig {
}
