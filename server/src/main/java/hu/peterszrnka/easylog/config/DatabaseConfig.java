package hu.peterszrnka.easylog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("file-db")
@EnableJpaRepositories(basePackages = "hu.peterszrnka.easylog.log.data")
public class DatabaseConfig {
}
