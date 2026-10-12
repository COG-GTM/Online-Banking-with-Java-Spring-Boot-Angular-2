package com.userFront.config;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 1.5 registers Tomcat's default servlet through
 * Context.addServletMapping(String, String), which no longer exists in Tomcat 9.
 * Static resources are served by Spring MVC, so the default servlet is not needed.
 */
@Configuration
public class TomcatConfig implements EmbeddedServletContainerCustomizer {

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof TomcatEmbeddedServletContainerFactory) {
            ((TomcatEmbeddedServletContainerFactory) container).setRegisterDefaultServlet(false);
        }
    }
}
