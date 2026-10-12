package com.userFront.config;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * Spring Boot 1.5's LogbackLoggingSystem depends on org.slf4j.impl.StaticLoggerBinder,
 * which logback 1.3+ (and slf4j 2.x) no longer provide. Boot's logging system is disabled so that
 * logback configures itself from logback.xml, JUL is still routed to SLF4J, and
 * logging.level.* properties are still applied.
 */
public class LoggingSystemListener implements ApplicationListener<ApplicationEvent>, Ordered {

	private static final String LEVEL_PREFIX = "logging.level.";

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			if (System.getProperty(LoggingSystem.SYSTEM_PROPERTY) == null) {
				System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
			}
			if (!SLF4JBridgeHandler.isInstalled()) {
				SLF4JBridgeHandler.removeHandlersForRootLogger();
				SLF4JBridgeHandler.install();
			}
		}
		else if (event instanceof ApplicationEnvironmentPreparedEvent) {
			applyLogLevels(((ApplicationEnvironmentPreparedEvent) event).getEnvironment());
		}
	}

	private void applyLogLevels(ConfigurableEnvironment environment) {
		if (!LoggingSystem.NONE.equals(System.getProperty(LoggingSystem.SYSTEM_PROPERTY))
				|| !(LoggerFactory.getILoggerFactory() instanceof LoggerContext)) {
			return;
		}
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Map<String, Object> levels = new RelaxedPropertyResolver(environment).getSubProperties(LEVEL_PREFIX);
		for (Map.Entry<String, Object> entry : levels.entrySet()) {
			String name = entry.getKey();
			String value = environment.resolvePlaceholders(String.valueOf(entry.getValue())).trim();
			Level level = "false".equalsIgnoreCase(value) ? Level.OFF : Level.toLevel(value, null);
			if (level == null) {
				continue;
			}
			if ("root".equalsIgnoreCase(name)) {
				name = org.slf4j.Logger.ROOT_LOGGER_NAME;
			}
			context.getLogger(name).setLevel(level);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
