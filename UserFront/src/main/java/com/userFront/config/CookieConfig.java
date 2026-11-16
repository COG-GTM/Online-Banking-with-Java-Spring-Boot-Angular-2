package com.userFront.config;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieConfig {

    @Bean
    public EmbeddedServletContainerCustomizer sameSiteCookieCustomizer() {
        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                if (container instanceof TomcatEmbeddedServletContainerFactory) {
                    ((TomcatEmbeddedServletContainerFactory) container).addContextCustomizers(new TomcatContextCustomizer() {
                        @Override
                        public void customize(Context context) {
                            Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
                            cookieProcessor.setSameSiteCookies("Lax");
                            context.setCookieProcessor(cookieProcessor);
                        }
                    });
                }
            }
        };
    }
}
