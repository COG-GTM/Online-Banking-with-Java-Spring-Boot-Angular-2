package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.domain.User;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

	private MockMvc mockMvc;
	private User alice;
	private User mallory;
	private Principal malloryPrincipal = () -> "mallory";

	@Before
	public void setUp() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/templates/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(userController).setViewResolvers(viewResolver).build();

		alice = user(1L, "alice", "alice@example.com");
		mallory = user(2L, "mallory", "mallory@example.com");
		when(userService.findByUsername("alice")).thenReturn(alice);
		when(userService.findByUsername("mallory")).thenReturn(mallory);
	}

	@Test
	public void profilePostUpdatesLoggedInUserAndIgnoresSubmittedUsernameAndId() throws Exception {
		mockMvc.perform(post("/user/profile").principal(malloryPrincipal)
				.param("username", "alice").param("id", "1").param("userId", "1")
				.param("firstName", "Mal").param("lastName", "Lory")
				.param("email", "new@example.com").param("phone", "555-0100"))
				.andExpect(status().isOk())
				.andExpect(view().name("profile"));

		verify(userService, never()).findByUsername("alice");
		verify(userService).saveUser(mallory);
		assertEquals("mallory", mallory.getUsername());
		assertEquals(Long.valueOf(2L), mallory.getUserId());
		assertEquals("new@example.com", mallory.getEmail());
		assertEquals("Mal", mallory.getFirstName());
		assertEquals("alice@example.com", alice.getEmail());
		assertEquals("Alice", alice.getFirstName());
	}

	@Test
	public void profilePostRejectsEmailOwnedByAnotherUser() throws Exception {
		when(userService.findByEmail("alice@example.com")).thenReturn(alice);

		mockMvc.perform(post("/user/profile").principal(malloryPrincipal)
				.param("firstName", "Mal").param("lastName", "Lory")
				.param("email", "alice@example.com").param("phone", "555-0100"))
				.andExpect(status().isOk())
				.andExpect(view().name("profile"))
				.andExpect(model().attribute("emailExists", true));

		verify(userService, never()).saveUser(any(User.class));
		assertEquals("mallory@example.com", mallory.getEmail());
	}

	@Test
	public void profilePostAllowsKeepingOwnEmail() throws Exception {
		when(userService.findByEmail("mallory@example.com")).thenReturn(mallory);

		mockMvc.perform(post("/user/profile").principal(malloryPrincipal)
				.param("firstName", "Mal").param("lastName", "Lory")
				.param("email", "mallory@example.com").param("phone", "555-0100"))
				.andExpect(status().isOk());

		verify(userService).saveUser(mallory);
		assertEquals("Mal", mallory.getFirstName());
	}

	private static User user(Long id, String username, String email) {
		User user = new User();
		user.setUserId(id);
		user.setUsername(username);
		user.setEmail(email);
		user.setFirstName(Character.toUpperCase(username.charAt(0)) + username.substring(1));
		return user;
	}
}
