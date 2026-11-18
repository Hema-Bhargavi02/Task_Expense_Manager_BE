package com.example.taskandexpensemanager.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class SecurityErrorHandlersTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    @Test
    void missingOrInvalidTokenReturnsJson401() throws Exception {
        MockHttpServletRequest request
                = new MockHttpServletRequest("GET", "/expense/period");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAuthEntryPoint(jsonMapper).commence(
                request,
                response,
                new BadCredentialsException("missing token")
        );

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        JsonNode body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals("UNAUTHORIZED", body.get("code").asString());
        assertEquals("/expense/period", body.get("path").asString());
        assertTrue(body.get("message").asString().contains("log in again"));
    }

    @Test
    void accessDeniedReturnsJson403() throws Exception {
        MockHttpServletRequest request
                = new MockHttpServletRequest("GET", "/expense/period");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAccessDeniedHandler(jsonMapper).handle(
                request,
                response,
                new AccessDeniedException("forbidden")
        );

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        JsonNode body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals("FORBIDDEN", body.get("code").asString());
        assertEquals("/expense/period", body.get("path").asString());
    }
}
