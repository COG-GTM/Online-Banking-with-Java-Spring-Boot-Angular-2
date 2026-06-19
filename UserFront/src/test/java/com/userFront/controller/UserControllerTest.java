package com.userFront.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.userFront.domain.User;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

	private MockMvc mockMvc;
	private Principal principal;
	private User user;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController)
				.setViewResolvers(new StubViewResolver()).build();

		principal = new Principal() {
			public String getName() {
				return "john";
			}
		};

		user = new User();
		user.setUsername("john");
		user.setFirstName("John");
		user.setLastName("Doe");
		user.setEmail("john@example.com");
		user.setPhone("123");
	}

	@Test
	public void profile_GET_returnsProfileViewWithUserData() throws Exception {
		when(userService.findByUsername("john")).thenReturn(user);

		mockMvc.perform(get("/user/profile").principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("profile"))
				.andExpect(model().attribute("user", user));
	}

	@Test
	public void profile_POST_updatesUserAndReturnsProfileView() throws Exception {
		when(userService.findByUsername("john")).thenReturn(user);

		mockMvc.perform(post("/user/profile")
				.param("username", "john")
				.param("firstName", "Johnny")
				.param("lastName", "Doe")
				.param("email", "johnny@example.com")
				.param("phone", "999"))
				.andExpect(status().isOk())
				.andExpect(view().name("profile"))
				.andExpect(model().attribute("user", user));

		verify(userService).saveUser(user);
	}
}
