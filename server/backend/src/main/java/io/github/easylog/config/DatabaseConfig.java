package io.github.easylog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Peter Szrnka
 */
@Configuration
@Profile("file-db")
@EnableJpaRepositories(basePackages = "io.github.easylog.data")
public class DatabaseConfig {
}
