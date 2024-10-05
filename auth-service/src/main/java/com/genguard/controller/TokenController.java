package com.genguard.controller;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final RestTemplate restTemplate; // RestTemplate for making HTTP requests
    private final String keycloakTokenUrl = "http://localhost:8080/realms/{realm}/protocol/openid-connect/token"; // Keycloak token endpoint
    private final String clientId = "your-client-id"; // Replace with your client ID
    private final String clientSecret = "your-client-secret"; // Replace with your client secret
    private final String realm = "your-realm"; // Replace with your realm

    @ExceptionHandler(value = AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Void> handleAuthNotFoundException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/validate")
    @PreAuthorize("hasAnyRole('MY_APP_USER', 'MY_APP_ADMIN')")
    public ResponseEntity<Void> validate() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestHeader("refresh-token") String refreshToken) {
        try {
            // Set up the HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Set up the body parameters for the token refresh request
            String body = "grant_type=refresh_token"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&refresh_token=" + refreshToken;

            // Create an HTTP entity with headers and body
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Construct the complete URL for the token endpoint
            String url = UriComponentsBuilder.fromUriString(keycloakTokenUrl)
                    .buildAndExpand(realm)
                    .toString();

            // Make a POST request to Keycloak's token endpoint
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, AccessTokenResponse.class);

            // Return the response
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
