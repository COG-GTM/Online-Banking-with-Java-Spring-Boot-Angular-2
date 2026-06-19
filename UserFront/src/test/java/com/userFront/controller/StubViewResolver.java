package com.userFront.controller;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * A no-op {@link ViewResolver} for standalone MockMvc controller tests. It
 * resolves every view name to a {@link View} that renders nothing, so tests can
 * assert on the returned view name and model without rendering Thymeleaf
 * templates or triggering redirect/forward processing.
 */
class StubViewResolver implements ViewResolver {

	public View resolveViewName(String viewName, Locale locale) {
		return new View() {
			public String getContentType() {
				return "text/html";
			}

			public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
				// no-op
			}
		};
	}
}
