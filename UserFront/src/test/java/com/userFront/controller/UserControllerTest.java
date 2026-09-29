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
import org.mockito.ArgumentCaptor;
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
	private User bob;

	private final Principal alicePrincipal = new Principal() {
		@Override
		public String getName() {
			return "alice";
		}
	};

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController)
				.setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
				.build();

		alice = user(1L, "alice", "alice@bank.test");
		bob = user(2L, "bob", "bob@bank.test");
		when(userService.findByUsername("alice")).thenReturn(alice);
		when(userService.findByUsername("bob")).thenReturn(bob);
	}

	@Test
	public void profilePostUpdatesAuthenticatedUserAndIgnoresSubmittedUsername() throws Exception {
		mockMvc.perform(post("/user/profile").principal(alicePrincipal)
				.param("username", "bob")
				.param("userId", "2")
				.param("id", "2")
				.param("firstName", "Alice")
				.param("lastName", "Updated")
				.param("email", "alice.new@bank.test")
				.param("phone", "555-0100"))
				.andExpect(status().isOk())
				.andExpect(view().name("profile"))
				.andExpect(model().attribute("user", alice));

		verify(userService, never()).findByUsername("bob");
		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userService).saveUser(saved.capture());
		assertEquals(alice, saved.getValue());
		assertEquals(Long.valueOf(1L), alice.getUserId());
		assertEquals("alice", alice.getUsername());
		assertEquals("alice.new@bank.test", alice.getEmail());
		assertEquals("555-0100", alice.getPhone());
		assertEquals("Alice", alice.getFirstName());
		assertEquals("Updated", alice.getLastName());
		assertEquals("bob@bank.test", bob.getEmail());
	}

	@Test
	public void profilePostRejectsEmailOwnedByAnotherUser() throws Exception {
		when(userService.findByEmail("bob@bank.test")).thenReturn(bob);

		mockMvc.perform(post("/user/profile").principal(alicePrincipal)
				.param("firstName", "Alice")
				.param("lastName", "Doe")
				.param("email", "bob@bank.test")
				.param("phone", "555-0100"))
				.andExpect(status().isOk())
				.andExpect(view().name("profile"))
				.andExpect(model().attribute("emailExists", true))
				.andExpect(model().attribute("user", alice));

		verify(userService, never()).saveUser(any(User.class));
		assertEquals("alice@bank.test", alice.getEmail());
	}

	@Test
	public void profilePostAllowsKeepingOwnEmail() throws Exception {
		when(userService.findByEmail("alice@bank.test")).thenReturn(alice);

		mockMvc.perform(post("/user/profile").principal(alicePrincipal)
				.param("firstName", "Alicia")
				.param("lastName", "Doe")
				.param("email", "alice@bank.test")
				.param("phone", "555-0199"))
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("emailExists"));

		verify(userService).saveUser(alice);
		verify(userService, never()).findByUsername("bob");
		assertEquals("Alicia", alice.getFirstName());
	}

	private static User user(Long id, String username, String email) {
		User user = new User();
		user.setUserId(id);
		user.setUsername(username);
		user.setEmail(email);
		return user;
	}
}
