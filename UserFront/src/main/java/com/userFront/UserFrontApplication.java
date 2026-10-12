package com.userFront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserFrontApplication {

	public static void main(String[] args) {
		// CVE-2024-56337: Tomcat's case-sensitivity check is unreliable with the JDK canonical-path cache on Java 8/11.
		System.setProperty("sun.io.useCanonCaches", "false");
		SpringApplication.run(UserFrontApplication.class, args);
	}
}
