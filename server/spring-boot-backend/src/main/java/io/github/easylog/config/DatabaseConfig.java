package io.github.easylog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Peter Szrnka
 */
@Configuration
@EnableJpaRepositories(basePackages = "io.github.easylog")
public class DatabaseConfig {
}
