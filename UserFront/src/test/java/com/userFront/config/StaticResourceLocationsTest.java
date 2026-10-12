package com.userFront.config;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StaticResourceLocationsTest.Config.class, webEnvironment = WebEnvironment.NONE)
public class StaticResourceLocationsTest {

	@Autowired
	private ResourceProperties resourceProperties;

	@Test
	public void staticResourcesAreServedFromTheClasspathOnly() {
		assertArrayEquals(new String[] { "classpath:/static/" }, resourceProperties.getStaticLocations());
	}

	@Configuration
	@EnableConfigurationProperties(ResourceProperties.class)
	static class Config {
	}

}
