package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.AccountService;

public class UserServiceImplCreateUserTests {

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

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createUserNeverReusesCallerSuppliedIdentityOrPrivileges() {
		User user = new User();
		user.setUserId(1L);
		user.setUsername("newuser");
		user.setPassword("secret");
		user.setEnabled(false);
		user.getUserRoles().add(new UserRole(user, new Role()));
		user.setRecipientList(new ArrayList<>(Collections.singletonList(new Recipient())));

		UserRole signupRole = new UserRole(user, new Role());
		Set<UserRole> signupRoles = new HashSet<>(Collections.singletonList(signupRole));

		when(passwordEncoder.encode("secret")).thenReturn("encoded");

		userService.createUser(user, signupRoles);

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userDao).save(saved.capture());

		assertNull(saved.getValue().getUserId());
		assertTrue(saved.getValue().isEnabled());
		assertEquals("encoded", saved.getValue().getPassword());
		assertEquals(Collections.singleton(signupRole), saved.getValue().getUserRoles());
		assertNull(saved.getValue().getRecipientList());
		assertNull(saved.getValue().getAppointmentList());
	}
}
