package io.github.easylog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Peter Szrnka
 */
@SpringBootTest
@ActiveProfiles("dev")
class DevProfileIntegrationTest {

    @Autowired
    private ApplicationContext context;

	@Test
	void contextLoads() {
        assertNotNull(context);
	}

}
