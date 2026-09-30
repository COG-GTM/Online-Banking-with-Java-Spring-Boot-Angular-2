package com.userFront.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Safe, unauthenticated request a client can issue before its first state-changing call
 * so that the CSRF token repository writes the XSRF-TOKEN cookie. While CSRF protection
 * is disabled in SecurityConfig no token is bound to the request and no cookie is written.
 */
@RestController
public class CsrfResource {

    @GetMapping("/api/csrf")
    public ResponseEntity<Void> csrf(CsrfToken token) {
        if (token != null) {
            token.getToken();
        }
        return ResponseEntity.noContent().build();
    }
}
