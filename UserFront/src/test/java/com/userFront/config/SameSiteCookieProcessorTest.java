package com.userFront.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import javax.servlet.http.Cookie;

import org.junit.Test;

public class SameSiteCookieProcessorTest {

    private final SameSiteCookieProcessor processor = new SameSiteCookieProcessor("Lax");

    @Test
    public void sessionCookieGetsSameSiteLax() {
        Cookie cookie = new Cookie("JSESSIONID", "abc123");
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        String header = processor.generateHeader(cookie);

        assertThat(header, containsString("JSESSIONID=abc123"));
        assertThat(header, containsString("HttpOnly"));
        assertThat(header, endsWith("; SameSite=Lax"));
    }

    @Test
    public void existingSameSiteAttributeIsNotDuplicated() {
        SameSiteCookieProcessor wrapped = new SameSiteCookieProcessor(
                new SameSiteCookieProcessor("Strict"), "Lax");

        String header = wrapped.generateHeader(new Cookie("XSRF-TOKEN", "t"));

        assertThat(header, endsWith("; SameSite=Strict"));
        assertEquals(header.indexOf("SameSite"), header.lastIndexOf("SameSite"));
    }
}
