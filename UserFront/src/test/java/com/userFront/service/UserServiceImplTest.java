package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.dto.SignupForm;
import com.userFront.service.UserServiceImpl.UserServiceImpl;

public class UserServiceImplTest {

	private UserDao userDao;
	private RoleDao roleDao;
	private BCryptPasswordEncoder passwordEncoder;
	private UserServiceImpl userService;
	private Role roleUser;

	@Before
	public void setUp() {
		userDao = mock(UserDao.class);
		roleDao = mock(RoleDao.class);
		passwordEncoder = mock(BCryptPasswordEncoder.class);
		AccountService accountService = mock(AccountService.class);

		userService = new UserServiceImpl();
		ReflectionTestUtils.setField(userService, "userDao", userDao);
		ReflectionTestUtils.setField(userService, "roleDao", roleDao);
		ReflectionTestUtils.setField(userService, "accountService", accountService);
		ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);

		roleUser = new Role();
		roleUser.setRoleId(1);
		roleUser.setName("ROLE_USER");
		when(roleDao.findByName("ROLE_USER")).thenReturn(roleUser);
		when(passwordEncoder.encode("pw")).thenReturn("hashed");
		when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArguments()[0]);
	}

	private SignupForm form() {
		SignupForm form = new SignupForm();
		form.setFirstName("Mal");
		form.setLastName("Lory");
		form.setPhone("555");
		form.setEmail("mal@example.com");
		form.setUsername("mallory");
		form.setPassword("pw");
		return form;
	}

	@Test
	public void createUserPersistsNewUserWithOnlyRoleUser() {
		userService.createUser(form());

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userDao).save(saved.capture());
		User user = saved.getValue();

		assertNull(user.getUserId());
		assertTrue(user.isEnabled());
		assertEquals("mallory", user.getUsername());
		assertEquals("mal@example.com", user.getEmail());
		assertEquals("hashed", user.getPassword());
		assertEquals(1, user.getUserRoles().size());
		UserRole userRole = user.getUserRoles().iterator().next();
		assertSame(roleUser, userRole.getRole());
		assertSame(user, userRole.getUser());
		verify(roleDao, never()).save(any(Role.class));
	}

	@Test
	public void createUserDoesNothingWhenUsernameTaken() {
		User existing = new User();
		when(userDao.findByUsername("mallory")).thenReturn(existing);

		assertSame(existing, userService.createUser(form()));
		verify(userDao, never()).save(any(User.class));
	}

	@Test(expected = IllegalStateException.class)
	public void createUserFailsWhenRoleUserMissing() {
		when(roleDao.findByName("ROLE_USER")).thenReturn(null);
		userService.createUser(form());
	}

	@Test
	public void checkUserExistsChecksEmail() {
		when(userDao.findByEmail("alice@example.com")).thenReturn(new User());
		assertTrue(userService.checkUserExists("newname", "alice@example.com"));
	}
}
