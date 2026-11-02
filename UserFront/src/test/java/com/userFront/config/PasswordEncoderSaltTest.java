package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderSaltTest {

    private static final String PASSWORD = "correct horse battery staple";

    private static String salt(String hash) {
        return hash.substring(7, 29);
    }

    @Test
    public void freshEncodersDoNotRepeatSalts() {
        String first = new SecurityConfig().passwordEncoder().encode(PASSWORD);
        String second = new SecurityConfig().passwordEncoder().encode(PASSWORD);

        assertNotEquals(salt(first), salt(second));
    }

    @Test
    public void saltsAreUniqueAcrossEncodes() {
        BCryptPasswordEncoder encoder = new SecurityConfig().passwordEncoder();
        Set<String> salts = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            assertTrue(salts.add(salt(encoder.encode(PASSWORD))));
        }
    }

    @Test
    public void usesCost12AndVerifies() {
        BCryptPasswordEncoder encoder = new SecurityConfig().passwordEncoder();
        String hash = encoder.encode(PASSWORD);

        assertEquals("$12$", hash.substring(3, 7));
        assertTrue(encoder.matches(PASSWORD, hash));
    }
}
