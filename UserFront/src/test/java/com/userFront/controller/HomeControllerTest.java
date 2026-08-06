package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

public class HomeControllerTest {

	private UserService userService;
	private RoleDao roleDao;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		userService = Mockito.mock(UserService.class);
		roleDao = Mockito.mock(RoleDao.class);

		Role role = new Role();
		role.setName("ROLE_USER");
		Mockito.when(roleDao.findByName("ROLE_USER")).thenReturn(role);
		Mockito.when(userService.checkUserExists(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

		HomeController homeController = new HomeController();
		ReflectionTestUtils.setField(homeController, "userService", userService);
		ReflectionTestUtils.setField(homeController, "roleDao", roleDao);

		mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
	}

	@Test
	public void signupIgnoresUserIdAndEnabledRequestParameters() throws Exception {
		mockMvc.perform(post("/signup")
				.param("userId", "1")
				.param("enabled", "false")
				.param("username", "attacker")
				.param("password", "attackerPassword")
				.param("firstName", "Att")
				.param("lastName", "Acker")
				.param("email", "attacker@example.com")
				.param("phone", "111-111-1111"))
				.andExpect(status().is3xxRedirection());

		User created = captureCreatedUser();

		assertNull("signup must not accept a client supplied identifier", created.getUserId());
		assertTrue("signup must not accept the enabled flag", created.isEnabled());
	}

	@Test
	public void signupCopiesOnlyTheFormFieldsOntoTheNewUser() throws Exception {
		mockMvc.perform(post("/signup")
				.param("username", "jsmith")
				.param("password", "secret")
				.param("firstName", "John")
				.param("lastName", "Smith")
				.param("email", "jsmith@example.com")
				.param("phone", "222-222-2222"))
				.andExpect(status().is3xxRedirection());

		User created = captureCreatedUser();

		assertNull(created.getUserId());
		assertEquals("jsmith", created.getUsername());
		assertEquals("secret", created.getPassword());
		assertEquals("John", created.getFirstName());
		assertEquals("Smith", created.getLastName());
		assertEquals("jsmith@example.com", created.getEmail());
		assertEquals("222-222-2222", created.getPhone());
		assertNull(created.getPrimaryAccount());
		assertNull(created.getSavingsAccount());
	}

	@SuppressWarnings("unchecked")
	private User captureCreatedUser() {
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(userService).createUser(userCaptor.capture(), (Set<UserRole>) Mockito.any());

		return userCaptor.getValue();
	}
}
