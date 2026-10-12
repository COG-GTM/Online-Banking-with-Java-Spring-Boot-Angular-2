package com.userFront.config;

import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * Spring Boot 1.5's LogbackLoggingSystem depends on org.slf4j.impl.StaticLoggerBinder,
 * which logback 1.3+ no longer provides. Boot's logging system is disabled so that
 * logback configures itself from logback.xml, and JUL is still routed to SLF4J.
 * The logging.file, logging.path and logging.level.* properties that Boot would
 * otherwise apply are applied here instead.
 */
public class LoggingSystemListener implements ApplicationListener<ApplicationEvent>, Ordered {

	private static final String FILE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %5level --- [%t] %-40.40logger{39} : %msg%n";

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			onStarting();
		}
		else if (event instanceof ApplicationEnvironmentPreparedEvent) {
			onEnvironmentPrepared(((ApplicationEnvironmentPreparedEvent) event).getEnvironment());
		}
	}

	private void onStarting() {
		if (System.getProperty(LoggingSystem.SYSTEM_PROPERTY) == null) {
			System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
		}
		if (!SLF4JBridgeHandler.isInstalled()) {
			SLF4JBridgeHandler.removeHandlersForRootLogger();
			SLF4JBridgeHandler.install();
		}
	}

	private void onEnvironmentPrepared(ConfigurableEnvironment environment) {
		if (!LoggingSystem.NONE.equals(System.getProperty(LoggingSystem.SYSTEM_PROPERTY))) {
			return;
		}
		ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		if (!(factory instanceof LoggerContext)) {
			return;
		}
		LoggerContext context = (LoggerContext) factory;
		LogFile logFile = LogFile.get(environment);
		if (logFile != null) {
			addFileAppender(context, logFile.toString());
		}
		Map<String, Object> levels = new RelaxedPropertyResolver(environment).getSubProperties("logging.level.");
		for (Map.Entry<String, Object> entry : levels.entrySet()) {
			String name = entry.getKey().equalsIgnoreCase(Logger.ROOT_LOGGER_NAME) ? Logger.ROOT_LOGGER_NAME : entry.getKey();
			String value = environment.resolvePlaceholders(String.valueOf(entry.getValue())).trim();
			Level level = "false".equalsIgnoreCase(value) ? Level.OFF : Level.toLevel(value, null);
			if (level != null) {
				context.getLogger(name).setLevel(level);
			}
		}
	}

	private void addFileAppender(LoggerContext context, String file) {
		Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
		if (root.getAppender("FILE") != null) {
			return;
		}
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern(FILE_PATTERN);
		encoder.start();

		RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setContext(context);
		appender.setName("FILE");
		appender.setEncoder(encoder);
		appender.setFile(file);

		FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
		rollingPolicy.setContext(context);
		rollingPolicy.setParent(appender);
		rollingPolicy.setFileNamePattern(file + ".%i");
		rollingPolicy.start();
		appender.setRollingPolicy(rollingPolicy);

		SizeBasedTriggeringPolicy<ch.qos.logback.classic.spi.ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
		triggeringPolicy.setContext(context);
		triggeringPolicy.setMaxFileSize(FileSize.valueOf("10MB"));
		triggeringPolicy.start();
		appender.setTriggeringPolicy(triggeringPolicy);

		appender.start();
		root.addAppender(appender);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
