package io.github.easylog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Peter Szrnka
 */
@SpringBootTest
@AutoConfigureMockMvc
class EasyLogServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

	@Test
	void contextLoads() {
        assertNotNull(context);
	}

    @ParameterizedTest
    @ValueSource(strings = {"/index.html", "/missing.html"})
    void testStaticContentExists(String input) throws Exception {
        mockMvc.perform(get(input)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
