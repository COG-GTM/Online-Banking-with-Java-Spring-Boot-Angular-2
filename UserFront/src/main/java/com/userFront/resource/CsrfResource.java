package com.userFront.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfResource {

    @RequestMapping(value = "/api/csrf", method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (token == null) {
            return ResponseEntity.noContent().build();
        }

        Map<String, String> body = new HashMap<>();
        body.put("headerName", token.getHeaderName());
        body.put("parameterName", token.getParameterName());
        body.put("token", token.getToken());

        return ResponseEntity.ok(body);
    }
}
