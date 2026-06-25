package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;

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
	private com.userFront.service.UserServiceImpl.UserServiceImpl userService;

	private static User user(String username, String email) {
		User u = new User();
		u.setUsername(username);
		u.setEmail(email);
		return u;
	}

	// ---------- checkUsernameExists ----------

	@Test
	public void checkUsernameExists_present_returnsTrue() {
		when(userDao.findByUsername("john")).thenReturn(user("john", "john@x.com"));
		assertTrue(userService.checkUsernameExists("john"));
	}

	@Test
	public void checkUsernameExists_absent_returnsFalse() {
		when(userDao.findByUsername("john")).thenReturn(null);
		assertFalse(userService.checkUsernameExists("john"));
	}

	// ---------- checkEmailExists ----------

	@Test
	public void checkEmailExists_present_returnsTrue() {
		when(userDao.findByEmail("john@x.com")).thenReturn(user("john", "john@x.com"));
		assertTrue(userService.checkEmailExists("john@x.com"));
	}

	@Test
	public void checkEmailExists_absent_returnsFalse() {
		when(userDao.findByEmail("john@x.com")).thenReturn(null);
		assertFalse(userService.checkEmailExists("john@x.com"));
	}

	// ---------- checkUserExists ----------

	@Test
	public void checkUserExists_emailTakenButUsernameFree_returnsTrue() {
		// Regression guard: the email argument (not the username) must drive the
		// email lookup. Username is free, email is taken -> user exists.
		when(userDao.findByUsername("newuser")).thenReturn(null);
		when(userDao.findByEmail("taken@x.com")).thenReturn(user("someoneelse", "taken@x.com"));

		assertTrue(userService.checkUserExists("newuser", "taken@x.com"));
	}

	@Test
	public void checkUserExists_usernameTaken_returnsTrue() {
		when(userDao.findByUsername("john")).thenReturn(user("john", "john@x.com"));

		assertTrue(userService.checkUserExists("john", "free@x.com"));
	}

	@Test
	public void checkUserExists_bothFree_returnsFalse() {
		when(userDao.findByUsername("newuser")).thenReturn(null);
		when(userDao.findByEmail("free@x.com")).thenReturn(null);

		assertFalse(userService.checkUserExists("newuser", "free@x.com"));
	}

	// ---------- createUser ----------

	@Test
	public void createUser_newUser_encodesPasswordAndPersists() {
		User newUser = user("john", "john@x.com");
		newUser.setPassword("plain");

		Role role = new Role();
		role.setName("ROLE_USER");
		Set<UserRole> roles = new HashSet<UserRole>();
		roles.add(new UserRole(newUser, role));

		when(userDao.findByUsername("john")).thenReturn(null);
		when(passwordEncoder.encode("plain")).thenReturn("ENCODED");
		when(userDao.save(newUser)).thenReturn(newUser);

		User result = userService.createUser(newUser, roles);

		assertSame(newUser, result);
		assertEquals("ENCODED", newUser.getPassword());
		verify(roleDao).save(role);
		verify(accountService).createPrimaryAccount();
		verify(accountService).createSavingsAccount();
		verify(userDao).save(newUser);
	}

	@Test
	public void createUser_existingUser_doesNothingAndReturnsExisting() {
		User existing = user("john", "john@x.com");
		User incoming = user("john", "john@x.com");
		incoming.setPassword("plain");

		when(userDao.findByUsername("john")).thenReturn(existing);

		User result = userService.createUser(incoming, new HashSet<UserRole>());

		assertSame(existing, result);
		verify(passwordEncoder, never()).encode(any(String.class));
		verify(userDao, never()).save(any(User.class));
	}
}
