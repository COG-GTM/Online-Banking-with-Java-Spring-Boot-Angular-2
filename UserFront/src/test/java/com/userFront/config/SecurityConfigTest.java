package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityConfigTest {

	private final BCryptPasswordEncoder encoder = new SecurityConfig().passwordEncoder();

	@Test
	public void passwordEncoderUsesStrength12() {
		String hash = encoder.encode("password");
		assertTrue(hash.startsWith("$2a$12$"));
	}

	@Test
	public void passwordEncoderGeneratesUniqueSaltsAcrossInstances() {
		String first = encoder.encode("password");
		String second = new SecurityConfig().passwordEncoder().encode("password");
		assertNotEquals(first.substring(0, 29), second.substring(0, 29));
		assertTrue(encoder.matches("password", first));
		assertTrue(encoder.matches("password", second));
	}

	@Test
	public void passwordEncoderRejectsWrongPassword() {
		String hash = encoder.encode("password");
		assertEquals(false, encoder.matches("wrong", hash));
	}
}
