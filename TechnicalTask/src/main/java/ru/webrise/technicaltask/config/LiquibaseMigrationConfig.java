package ru.webrise.technicaltask.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("liquibaseMigration")
@PropertySource("classpath:application-liquibase-migration.properties")
public class LiquibaseMigrationConfig {
}
