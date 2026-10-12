package com.userFront.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class DataBindingConfig {

    public static final String[] DISALLOWED_FIELD_PATTERNS = {"*[*", "*]*"};

    @InitBinder
    public void rejectIndexedPropertyPaths(WebDataBinder binder) {
        binder.setDisallowedFields(DISALLOWED_FIELD_PATTERNS);
    }
}
