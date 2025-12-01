package io.github.easylog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Peter Szrnka
 */
@Slf4j
@SpringBootApplication
public class EasyLogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyLogServerApplication.class, args);
    }
}
