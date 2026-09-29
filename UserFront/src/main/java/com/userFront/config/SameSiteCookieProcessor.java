package com.userFront.config;

import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;

public class SameSiteCookieProcessor extends Rfc6265CookieProcessor {

    private final String sameSite;

    public SameSiteCookieProcessor(String sameSite) {
        this.sameSite = sameSite;
    }

    @Override
    public String generateHeader(Cookie cookie) {
        String header = super.generateHeader(cookie);
        if (header.toLowerCase(Locale.ROOT).contains("; samesite=")) {
            return header;
        }
        return header + "; SameSite=" + sameSite;
    }
}
