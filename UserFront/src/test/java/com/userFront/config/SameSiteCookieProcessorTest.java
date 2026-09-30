package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.Cookie;

import org.junit.Test;

public class SameSiteCookieProcessorTest {

    private final SameSiteCookieProcessor processor = new SameSiteCookieProcessor("Lax");

    @Test
    public void appendsSameSiteToSessionCookie() {
        Cookie cookie = new Cookie("JSESSIONID", "abc123");
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        String header = processor.generateHeader(cookie);

        assertTrue(header.startsWith("JSESSIONID=abc123"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.endsWith("; SameSite=Lax"));
    }

    @Test
    public void appendsSameSiteToRememberMeAndXsrfCookies() {
        assertTrue(processor.generateHeader(new Cookie("remember-me", "token")).endsWith("; SameSite=Lax"));
        assertTrue(processor.generateHeader(new Cookie("XSRF-TOKEN", "token")).endsWith("; SameSite=Lax"));
    }

    @Test
    public void addsSameSiteOnlyOnce() {
        String header = processor.generateHeader(new Cookie("JSESSIONID", "abc123"));

        assertEquals(header.indexOf("SameSite"), header.lastIndexOf("SameSite"));
    }
}
