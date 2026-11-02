package com.userFront.config;

import java.lang.reflect.Method;

import javax.servlet.http.Cookie;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;

/**
 * Adds {@code SameSite} to every cookie Tomcat writes (JSESSIONID, remember-me, XSRF-TOKEN).
 * Tomcat 8.5.42+ supports this natively, so use that when available and append the
 * attribute ourselves on older versions such as the 8.5.15 bundled with Boot 1.5.4.
 */
public class SameSiteCookieProcessor extends Rfc6265CookieProcessor {

    private final String sameSite;

    public SameSiteCookieProcessor(String sameSite) {
        this.sameSite = sameSite;
        try {
            Method setSameSiteCookies = getClass().getMethod("setSameSiteCookies", String.class);
            setSameSiteCookies.invoke(this, sameSite);
        } catch (ReflectiveOperationException e) {
            // Older Tomcat: handled in generateHeader.
        }
    }

    @Override
    public String generateHeader(Cookie cookie) {
        String header = super.generateHeader(cookie);
        if (header.toLowerCase().contains("samesite=")) {
            return header;
        }
        return header + "; SameSite=" + sameSite;
    }
}
