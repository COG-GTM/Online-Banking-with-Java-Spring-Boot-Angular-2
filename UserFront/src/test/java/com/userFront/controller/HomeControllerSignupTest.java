package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerSignupTest {

	@Mock
	private UserService userService;

	@Mock
	private RoleDao roleDao;

	@InjectMocks
	private HomeController homeController;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void signupIgnoresNonFormFields() throws Exception {
		Role userRole = new Role();
		userRole.setName("ROLE_USER");
		when(roleDao.findByName("ROLE_USER")).thenReturn(userRole);

		mockMvc.perform(post("/signup")
				.param("firstName", "Mallory")
				.param("lastName", "Attacker")
				.param("phone", "555")
				.param("email", "mallory@example.com")
				.param("username", "mallory")
				.param("password", "secret")
				.param("userId", "1")
				.param("enabled", "false")
				.param("userRoles[0].role.roleId", "2")
				.param("userRoles[0].role.name", "ROLE_ADMIN"))
				.andExpect(redirectedUrl("/"));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		ArgumentCaptor<Set> rolesCaptor = ArgumentCaptor.forClass(Set.class);
		verify(userService).createUser(userCaptor.capture(), rolesCaptor.capture());

		User user = userCaptor.getValue();
		assertNull(user.getUserId());
		assertTrue(user.isEnabled());
		assertTrue(user.getUserRoles().isEmpty());
		assertEquals("mallory", user.getUsername());
		assertEquals("mallory@example.com", user.getEmail());

		Set<UserRole> roles = rolesCaptor.getValue();
		assertEquals(1, roles.size());
		assertEquals("ROLE_USER", roles.iterator().next().getRole().getName());
	}
}
