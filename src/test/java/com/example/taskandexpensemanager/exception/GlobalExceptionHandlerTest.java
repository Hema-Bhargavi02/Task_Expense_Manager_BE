package com.example.taskandexpensemanager.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class GlobalExceptionHandlerTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void apiExceptionBecomesJsonWithStatusAndMessage() throws Exception {
        MvcResult result = mockMvc
                .perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andReturn();

        assertTrue(result.getResponse().getContentType()
                .startsWith("application/json"));

        JsonNode body = jsonMapper.readTree(
                result.getResponse().getContentAsString()
        );

        assertEquals(409, body.get("status").asInt());
        assertEquals("USER_ALREADY_EXISTS", body.get("code").asString());
        assertEquals(
                "That username or email is already registered.",
                body.get("message").asString()
        );
        assertEquals("/conflict", body.get("path").asString());
    }

    @Test
    void unexpectedExceptionIsLoggedButNotExposed() throws Exception {
        MvcResult result = mockMvc
                .perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String rawBody = result.getResponse().getContentAsString();

        assertFalse(rawBody.contains("secret internal detail"));

        JsonNode body = jsonMapper.readTree(rawBody);

        assertEquals(500, body.get("status").asInt());
        assertEquals("INTERNAL_ERROR", body.get("code").asString());
        assertEquals(
                "Something went wrong on our side. Please try again.",
                body.get("message").asString()
        );
    }

    @RestController
    static class TestController {

        @GetMapping("/conflict")
        String conflict() {
            throw new ConflictException(
                    "USER_ALREADY_EXISTS",
                    "That username or email is already registered."
            );
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("secret internal detail");
        }
    }
}
