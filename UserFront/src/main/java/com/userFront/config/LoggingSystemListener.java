package com.userFront.config;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * Spring Boot 1.5's LogbackLoggingSystem depends on org.slf4j.impl.StaticLoggerBinder,
 * which logback 1.3+ no longer provides. Boot's logging system is disabled so that
 * logback configures itself from logback.xml, and JUL is still routed to SLF4J.
 */
public class LoggingSystemListener implements ApplicationListener<ApplicationStartingEvent>, Ordered {

	@Override
	public void onApplicationEvent(ApplicationStartingEvent event) {
		if (System.getProperty(LoggingSystem.SYSTEM_PROPERTY) == null) {
			System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
		}
		if (!SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.removeHandlersForRootLogger();
			SLF4JBridgeHandler.install();
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
