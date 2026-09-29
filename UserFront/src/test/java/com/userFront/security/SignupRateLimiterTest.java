package com.userFront.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class SignupRateLimiterTest {

	@Test
	public void blocksAfterMaxAttemptsWithinWindow() {
		SignupRateLimiter limiter = new SignupRateLimiter(3, 15);

		assertTrue(limiter.tryAcquire("10.0.0.1", 0));
		assertTrue(limiter.tryAcquire("10.0.0.1", 1));
		assertTrue(limiter.tryAcquire("10.0.0.1", 2));
		assertFalse(limiter.tryAcquire("10.0.0.1", 3));
	}

	@Test
	public void tracksClientsIndependently() {
		SignupRateLimiter limiter = new SignupRateLimiter(1, 15);

		assertTrue(limiter.tryAcquire("10.0.0.1", 0));
		assertFalse(limiter.tryAcquire("10.0.0.1", 1));
		assertTrue(limiter.tryAcquire("10.0.0.2", 1));
	}

	@Test
	public void resetsAfterWindowExpires() {
		SignupRateLimiter limiter = new SignupRateLimiter(1, 15);
		long window = TimeUnit.MINUTES.toMillis(15);

		assertTrue(limiter.tryAcquire("10.0.0.1", 0));
		assertFalse(limiter.tryAcquire("10.0.0.1", window - 1));
		assertTrue(limiter.tryAcquire("10.0.0.1", window));
	}
}
