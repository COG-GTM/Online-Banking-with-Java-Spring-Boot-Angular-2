package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest {

	@Mock
	private UserService userService;

	@Mock
	private RoleDao roleDao;

	@InjectMocks
	private HomeController homeController;

	private User user(String username, String email) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		return user;
	}

	@Test
	public void duplicateEmailLooksUpEachFieldOnce() {
		when(userService.checkEmailExists("taken@example.com")).thenReturn(true);
		when(userService.checkUsernameExists("fresh")).thenReturn(false);
		ExtendedModelMap model = new ExtendedModelMap();

		String view = homeController.signupPost(user("fresh", "taken@example.com"), model);

		assertEquals("signup", view);
		assertEquals(true, model.get("emailExists"));
		assertFalse(model.containsAttribute("usernameExists"));
		verify(userService, times(1)).checkEmailExists("taken@example.com");
		verify(userService, times(1)).checkUsernameExists("fresh");
		verify(userService, never()).createUser(any(User.class), anySetOf(UserRole.class));
	}

	@Test
	public void duplicateUsernameAndEmailFlagsBoth() {
		when(userService.checkEmailExists("taken@example.com")).thenReturn(true);
		when(userService.checkUsernameExists("taken")).thenReturn(true);
		ExtendedModelMap model = new ExtendedModelMap();

		String view = homeController.signupPost(user("taken", "taken@example.com"), model);

		assertEquals("signup", view);
		assertEquals(true, model.get("emailExists"));
		assertEquals(true, model.get("usernameExists"));
		verify(userService, times(1)).checkEmailExists("taken@example.com");
		verify(userService, times(1)).checkUsernameExists("taken");
	}

	@Test
	public void newUserIsCreated() {
		when(roleDao.findByName("ROLE_USER")).thenReturn(new Role());
		ExtendedModelMap model = new ExtendedModelMap();

		String view = homeController.signupPost(user("fresh", "fresh@example.com"), model);

		assertEquals("redirect:/", view);
		assertTrue(model.isEmpty());
		verify(userService, times(1)).checkEmailExists("fresh@example.com");
		verify(userService, times(1)).checkUsernameExists("fresh");
		verify(userService, times(1)).createUser(any(User.class), anySetOf(UserRole.class));
	}
}
