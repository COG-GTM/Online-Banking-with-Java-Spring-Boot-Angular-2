package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserServiceImpl.UserServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

	@Mock
	private UserDao userDao;

	@Mock
	private RoleDao roleDao;

	@Mock
	private AccountService accountService;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private static Role role(int id, String name) {
		Role role = new Role();
		role.setRoleId(id);
		role.setName(name);
		return role;
	}

	@Test(expected = IllegalArgumentException.class)
	public void createUserRejectsExistingId() {
		User user = new User();
		user.setUserId(1L);
		user.setUsername("mallory");
		try {
			userService.createUser(user, Collections.<UserRole>emptySet());
		} finally {
			verify(userDao, never()).save(any(User.class));
		}
	}

	@Test
	public void createUserDiscardsPreexistingRoles() {
		when(userDao.save(any(User.class))).thenAnswer(new Answer<User>() {
			@Override
			public User answer(InvocationOnMock invocation) {
				return (User) invocation.getArguments()[0];
			}
		});

		User user = new User();
		user.setUsername("mallory");
		user.setPassword("secret");
		user.setEnabled(false);
		user.getUserRoles().add(new UserRole(user, role(2, "ROLE_ADMIN")));

		UserRole serverRole = new UserRole(user, role(1, "ROLE_USER"));
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(serverRole);

		User saved = userService.createUser(user, userRoles);

		assertEquals(1, saved.getUserRoles().size());
		assertSame(serverRole, saved.getUserRoles().iterator().next());
		assertTrue(saved.isEnabled());
	}

	@Test
	public void createUserKeepsRolesWhenSetIsAliased() {
		when(userDao.save(any(User.class))).thenAnswer(new Answer<User>() {
			@Override
			public User answer(InvocationOnMock invocation) {
				return (User) invocation.getArguments()[0];
			}
		});

		User user = new User();
		user.setUsername("alice");
		user.setPassword("secret");
		UserRole serverRole = new UserRole(user, role(1, "ROLE_USER"));
		user.getUserRoles().add(serverRole);

		User saved = userService.createUser(user, user.getUserRoles());

		assertEquals(1, saved.getUserRoles().size());
		assertSame(serverRole, saved.getUserRoles().iterator().next());
	}
}
