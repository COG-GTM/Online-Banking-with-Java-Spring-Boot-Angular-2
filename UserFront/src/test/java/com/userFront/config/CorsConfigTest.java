package com.userFront.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    private final CorsConfigurationSource source = new CorsConfig().corsConfigurationSource();

    @Test
    void resolvesConfigurationForApiPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/all");

        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(config.getAllowedMethods())
                .containsExactlyInAnyOrder("POST", "PUT", "GET", "OPTIONS", "DELETE");
        assertThat(config.getAllowedHeaders())
                .contains("authorization", "content-type", "x-requested-with");
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void allowsAdminPortalOriginAndRejectsOthers() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/all");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("http://localhost:4200")).isEqualTo("http://localhost:4200");
        assertThat(config.checkOrigin("http://evil.example.com")).isNull();
        assertThat(config.checkHeaders(List.of("Content-Type", "Authorization")))
                .containsExactlyInAnyOrder("Content-Type", "Authorization");
    }
}
