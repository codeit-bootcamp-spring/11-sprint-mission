package com.sprint.mission.discodeit.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super(authenticationManager);
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported");
        }
        String username = "";
        String password = "";
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> body = objectMapper.readValue(request.getInputStream(), Map.class);
                username = body.getOrDefault("username", "");
                password = body.getOrDefault("password", "");
            } catch (IOException ignored) {
            }
        } else {
            username = obtainUsername(request) != null ? obtainUsername(request) : "";
            password = obtainPassword(request) != null ? obtainPassword(request) : "";
        }
        UsernamePasswordAuthenticationToken authRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(username.trim(), password);
        setDetails(request, authRequest);
        return getAuthenticationManager().authenticate(authRequest);
    }
}
