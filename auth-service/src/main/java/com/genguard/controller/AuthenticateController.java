package com.genguard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authenticate")
public class AuthenticateController {

    private final RestTemplate restTemplate;
    private final String keycloakTokenUrl = "http://localhost:8080/realms/{realm}/protocol/openid-connect/token";
    private final String clientId = "client-id";
    private final String clientSecret = "client-secret";


    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody AuthRequestDto request) {
        try {
            // Set up the HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Set up the body parameters
            String body = "grant_type=password"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&username=" + request.getUsername()
                    + "&password=" + request.getPassword();

            // Create an HTTP entity with headers and body
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Make a POST request to Keycloak's token endpoint
            ResponseEntity<String> response = restTemplate.exchange(keycloakTokenUrl, HttpMethod.POST, entity, String.class);

            // Return the response
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }
}
