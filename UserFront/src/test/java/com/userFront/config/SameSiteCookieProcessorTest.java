package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.Cookie;

import org.junit.Test;

public class SameSiteCookieProcessorTest {

    @Test
    public void addsSameSiteLaxOnce() {
        String header = new SameSiteCookieProcessor("Lax").generateHeader(new Cookie("JSESSIONID", "abc"));

        assertTrue(header, header.startsWith("JSESSIONID=abc"));
        assertTrue(header, header.contains("SameSite=Lax"));
        assertEquals(header, header.indexOf("SameSite="), header.lastIndexOf("SameSite="));
    }
}
