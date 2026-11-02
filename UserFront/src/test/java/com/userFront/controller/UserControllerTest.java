package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
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
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.domain.User;
import com.userFront.service.UserService;

public class UserControllerTest {

	private UserService userService;
	private MockMvc mockMvc;
	private User alice;
	private User mallory;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		UserController controller = new UserController();
		ReflectionTestUtils.setField(controller, "userService", userService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setViewResolvers(new InternalResourceViewResolver("/templates/", ".html")).build();

		alice = user(1L, "alice", "alice@example.com");
		mallory = user(2L, "mallory", "mallory@example.com");
		when(userService.findByUsername("alice")).thenReturn(alice);
		when(userService.findByUsername("mallory")).thenReturn(mallory);
		when(userService.findByEmail("alice@example.com")).thenReturn(alice);
		when(userService.findByEmail("mallory@example.com")).thenReturn(mallory);
		when(userService.saveUser(any(User.class))).thenAnswer(new Answer<User>() {
			public User answer(InvocationOnMock invocation) {
				return (User) invocation.getArguments()[0];
			}
		});
	}

	@Test
	public void usernameInFormCannotRetargetTheUpdate() throws Exception {
		mockMvc.perform(post("/user/profile").principal(principal("mallory"))
				.param("username", "alice").param("id", "1").param("userId", "1")
				.param("firstName", "Mal").param("lastName", "Lory")
				.param("email", "mallory+new@example.com").param("phone", "555-0100"))
				.andExpect(status().isOk()).andExpect(view().name("profile"));

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userService).saveUser(saved.capture());
		assertEquals(Long.valueOf(2L), saved.getValue().getUserId());
		assertEquals("mallory", saved.getValue().getUsername());
		assertEquals("mallory+new@example.com", saved.getValue().getEmail());
		verify(userService, never()).findByUsername("alice");
		assertEquals("alice", alice.getUsername());
		assertEquals("alice@example.com", alice.getEmail());
		assertEquals("Alice", alice.getFirstName());
	}

	@Test
	public void emailOwnedByAnotherUserIsRejected() throws Exception {
		mockMvc.perform(post("/user/profile").principal(principal("mallory"))
				.param("firstName", "Mal").param("lastName", "Lory")
				.param("email", "alice@example.com").param("phone", "555-0100"))
				.andExpect(status().isOk()).andExpect(view().name("profile"))
				.andExpect(model().attribute("emailExists", true))
				.andExpect(model().attribute("user", mallory));

		verify(userService, never()).saveUser(any(User.class));
		assertEquals("mallory@example.com", mallory.getEmail());
		assertEquals("Mallory", mallory.getFirstName());
	}

	@Test
	public void keepingOwnEmailIsAllowed() throws Exception {
		mockMvc.perform(post("/user/profile").principal(principal("mallory"))
				.param("firstName", "Mal").param("lastName", "Lory")
				.param("email", "mallory@example.com").param("phone", "555-0100"))
				.andExpect(status().isOk()).andExpect(model().attributeDoesNotExist("emailExists"));

		verify(userService).saveUser(mallory);
		assertEquals("Mal", mallory.getFirstName());
		assertEquals("555-0100", mallory.getPhone());
	}

	private static User user(Long id, String username, String email) {
		User user = new User();
		user.setUserId(id);
		user.setUsername(username);
		user.setEmail(email);
		user.setFirstName(Character.toUpperCase(username.charAt(0)) + username.substring(1));
		user.setLastName("Doe");
		user.setPhone("555-0000");
		return user;
	}

	private static Principal principal(final String name) {
		return new Principal() {
			public String getName() {
				return name;
			}
		};
	}
}
