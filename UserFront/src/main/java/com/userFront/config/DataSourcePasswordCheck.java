package com.userFront.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Aborts startup before any bean is created when the datasource password cannot be resolved,
 * instead of letting the literal placeholder reach the database as a password.
 */
@Component
public class DataSourcePasswordCheck implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            environment.getRequiredProperty("spring.datasource.password");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new IllegalStateException(
                    "Datasource password is not configured: set the DB_PASSWORD environment variable", ex);
        }
    }
}
