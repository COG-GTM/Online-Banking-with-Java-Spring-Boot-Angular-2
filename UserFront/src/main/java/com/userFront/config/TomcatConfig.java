package com.userFront.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    /**
     * Spring Boot 1.5 registers Tomcat's default servlet via
     * Context.addServletMapping, which no longer exists in Tomcat 9.
     * All requests are handled by the DispatcherServlet mapped to "/".
     */
    @Bean
    public EmbeddedServletContainerCustomizer disableDefaultServlet() {
        return container -> container.setRegisterDefaultServlet(false);
    }

}
