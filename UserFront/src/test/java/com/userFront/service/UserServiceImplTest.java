package com.userFront.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.UserServiceImpl;

public class UserServiceImplTest {

	private UserDao userDao;
	private UserServiceImpl userService;

	@Before
	public void setUp() {
		userDao = mock(UserDao.class);
		userService = new UserServiceImpl();
		ReflectionTestUtils.setField(userService, "userDao", userDao);
	}

	@Test
	public void checkUserExistsDetectsDuplicateEmail() {
		when(userDao.findByEmail("taken@example.com")).thenReturn(new User());

		assertTrue(userService.checkUserExists("newuser", "taken@example.com"));
	}

	@Test
	public void checkUserExistsDetectsDuplicateUsername() {
		when(userDao.findByUsername("taken")).thenReturn(new User());

		assertTrue(userService.checkUserExists("taken", "new@example.com"));
	}

	@Test
	public void checkUserExistsFalseWhenBothFree() {
		assertFalse(userService.checkUserExists("newuser", "new@example.com"));
	}
}
