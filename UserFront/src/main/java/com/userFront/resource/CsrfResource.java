package com.userFront.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets the AdminPortal SPA obtain the XSRF-TOKEN cookie (issued by the CSRF filter) before its first POST.
 */
@RestController
public class CsrfResource {

    @RequestMapping(value = "/api/csrf", method = RequestMethod.GET)
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().build();
    }
}
