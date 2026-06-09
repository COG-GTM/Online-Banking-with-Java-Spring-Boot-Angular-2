package com.userFront.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard JSON error envelope returned by the REST API.
 */
public class ApiErrorResponse {

    private int status;
    private String message;
    private Map<String, Object> details = new HashMap<>();

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public ApiErrorResponse addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
