package com.userFront.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
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

	private User user;

	@Before
	public void setUp() {
		user = new User();
		user.setUsername("john");
		user.setEmail("john@example.com");
		user.setPassword("plain");
	}

	@Test
	public void createUser_createsUserWithRolesAndAccounts() {
		Role role = new Role();
		role.setName("ROLE_USER");
		Set<UserRole> userRoles = new HashSet<UserRole>();
		userRoles.add(new UserRole(user, role));

		when(userDao.findByUsername("john")).thenReturn(null);
		when(passwordEncoder.encode("plain")).thenReturn("encrypted");
		PrimaryAccount primaryAccount = new PrimaryAccount();
		SavingsAccount savingsAccount = new SavingsAccount();
		when(accountService.createPrimaryAccount()).thenReturn(primaryAccount);
		when(accountService.createSavingsAccount()).thenReturn(savingsAccount);
		when(userDao.save(user)).thenReturn(user);

		User result = userService.createUser(user, userRoles);

		assertSame(user, result);
		assertSame(primaryAccount, user.getPrimaryAccount());
		assertSame(savingsAccount, user.getSavingsAccount());
		assertTrue(user.getUserRoles().containsAll(userRoles));
		verify(passwordEncoder).encode("plain");
		verify(roleDao).save(role);
		verify(userDao).save(user);
	}

	@Test
	public void checkUserExists_existingUser_returnsTrue() {
		when(userDao.findByUsername("john")).thenReturn(user);

		assertTrue(userService.checkUserExists("john", "john@example.com"));
	}

	@Test
	public void checkUserExists_nonExistentUser_returnsFalse() {
		when(userDao.findByUsername(anyString())).thenReturn(null);
		when(userDao.findByEmail(anyString())).thenReturn(null);

		assertFalse(userService.checkUserExists("ghost", "ghost@example.com"));
	}

	@Test
	public void checkUsernameExists_existingUsername_returnsTrue() {
		when(userDao.findByUsername("john")).thenReturn(user);

		assertTrue(userService.checkUsernameExists("john"));
	}

	@Test
	public void checkEmailExists_existingEmail_returnsTrue() {
		when(userDao.findByEmail("john@example.com")).thenReturn(user);

		assertTrue(userService.checkEmailExists("john@example.com"));
	}

	@Test
	public void enableUser_setsUserEnabled() {
		user.setEnabled(false);
		when(userDao.findByUsername("john")).thenReturn(user);

		userService.enableUser("john");

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userDao).save(captor.capture());
		assertTrue(captor.getValue().isEnabled());
	}

	@Test
	public void disableUser_setsUserDisabled() {
		user.setEnabled(true);
		when(userDao.findByUsername("john")).thenReturn(user);

		userService.disableUser("john");

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userDao).save(captor.capture());
		assertFalse(captor.getValue().isEnabled());
	}

	@Test
	public void findByUsername_existingUser_returnsUser() {
		when(userDao.findByUsername("john")).thenReturn(user);

		assertSame(user, userService.findByUsername("john"));
	}

	@Test
	public void findByUsername_nonExistent_returnsNull() {
		when(userDao.findByUsername("ghost")).thenReturn(null);

		assertNull(userService.findByUsername("ghost"));
	}
}
