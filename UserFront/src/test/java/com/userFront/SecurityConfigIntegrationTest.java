package com.userFront;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

/**
 * Integration tests for {@link com.userFront.config.SecurityConfig} driven through the full
 * Spring Security filter chain via {@link MockMvc}.
 *
 * <p>Verifies public matchers are reachable without authentication, protected paths require
 * authentication, and the form-login success/failure redirects behave as configured.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigIntegrationTest {

	private static final String USERNAME = "securityconfig_user";
	private static final String PASSWORD = "s3cret-password";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Before
	public void createTestUser() {
		if (userService.findByUsername(USERNAME) == null) {
			Role role = new Role();
			role.setRoleId(1);
			role.setName("ROLE_USER");

			User user = new User();
			user.setUsername(USERNAME);
			user.setPassword(PASSWORD);
			user.setFirstName("Security");
			user.setLastName("Config");
			user.setEmail("securityconfig_user@example.com");
			user.setPhone("5550000000");

			Set<UserRole> userRoles = new HashSet<>();
			userRoles.add(new UserRole(user, role));

			userService.createUser(user, userRoles);
		}
	}

	@Test
	public void signupPage_isPubliclyAccessible() throws Exception {
		mockMvc.perform(get("/signup"))
				.andExpect(status().isOk());
	}

	@Test
	public void staticCssResources_arePubliclyAccessible() throws Exception {
		mockMvc.perform(get("/css/main.css"))
				.andExpect(status().isOk());
	}

	@Test
	public void rootPath_isPubliclyAccessible() throws Exception {
		// Public matcher "/" hits HomeController, which redirects to /index rather than
		// bouncing to the login page.
		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/index"));
	}

	@Test
	public void protectedPath_redirectsToLogin_whenUnauthenticated() throws Exception {
		mockMvc.perform(get("/userFront"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/index"));
	}

	@Test
	public void userProfilePath_redirectsToLogin_whenUnauthenticated() throws Exception {
		mockMvc.perform(get("/user/profile"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrlPattern("**/index"));
	}

	@Test
	public void formLogin_withValidCredentials_redirectsToUserFront() throws Exception {
		// SecurityConfig sets loginPage("/index"), which also makes "/index" the
		// form-login processing URL.
		mockMvc.perform(formLogin("/index").user(USERNAME).password(PASSWORD))
				.andExpect(authenticated().withUsername(USERNAME))
				.andExpect(redirectedUrl("/userFront"));
	}

	@Test
	public void formLogin_withInvalidCredentials_redirectsToIndexError() throws Exception {
		mockMvc.perform(formLogin("/index").user(USERNAME).password("wrong-password"))
				.andExpect(unauthenticated())
				.andExpect(redirectedUrl("/index?error"));
	}
}
