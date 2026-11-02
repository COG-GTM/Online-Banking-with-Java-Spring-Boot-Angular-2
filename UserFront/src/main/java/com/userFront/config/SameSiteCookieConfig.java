package com.userFront.config;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SameSiteCookieConfig implements EmbeddedServletContainerCustomizer {

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof TomcatEmbeddedServletContainerFactory) {
            ((TomcatEmbeddedServletContainerFactory) container).addContextCustomizers(
                    context -> context.setCookieProcessor(new SameSiteCookieProcessor("Lax")));
        }
    }
}
