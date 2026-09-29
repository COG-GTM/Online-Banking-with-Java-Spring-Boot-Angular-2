package com.userFront.security;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SignupRateLimiter {

	private final int maxAttempts;
	private final long windowMillis;
	private final Map<String, Window> windows = new ConcurrentHashMap<>();

	public SignupRateLimiter(@Value("${signup.rate-limit.max-attempts:5}") int maxAttempts,
			@Value("${signup.rate-limit.window-minutes:15}") long windowMinutes) {
		this.maxAttempts = maxAttempts;
		this.windowMillis = TimeUnit.MINUTES.toMillis(windowMinutes);
	}

	public boolean tryAcquire(String clientKey) {
		return tryAcquire(clientKey, System.currentTimeMillis());
	}

	boolean tryAcquire(String clientKey, long now) {
		evictExpired(now);
		String key = clientKey == null ? "unknown" : clientKey;
		Window window = windows.compute(key, (k, current) ->
				current == null || now - current.start >= windowMillis ? new Window(now) : current);
		synchronized (window) {
			if (window.count >= maxAttempts) {
				return false;
			}
			window.count++;
			return true;
		}
	}

	private void evictExpired(long now) {
		Iterator<Window> it = windows.values().iterator();
		while (it.hasNext()) {
			if (now - it.next().start >= windowMillis) {
				it.remove();
			}
		}
	}

	private static final class Window {
		private final long start;
		private int count;

		private Window(long start) {
			this.start = start;
		}
	}
}
