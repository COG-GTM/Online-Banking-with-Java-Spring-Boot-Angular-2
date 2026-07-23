package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.UserSecurityService;

/**
 * Unit tests for {@link UserSecurityService#loadUserByUsername(String)} using a
 * mocked {@link UserDao} (no Spring context / database required).
 */
@RunWith(MockitoJUnitRunner.class)
public class UserSecurityServiceTest {

	@Mock
	private UserDao userDao;

	@InjectMocks
	private UserSecurityService userSecurityService;

	@Test
	public void loadUserByUsername_returnsUser_whenUserExists() {
		User user = new User();
		user.setUsername("john");
		user.setPassword("encoded-password");
		when(userDao.findByUsername("john")).thenReturn(user);

		UserDetails details = userSecurityService.loadUserByUsername("john");

		assertNotNull(details);
		assertEquals("john", details.getUsername());
		assertSame(user, details);
		verify(userDao).findByUsername("john");
	}

	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsername_throws_whenUserNotFound() {
		when(userDao.findByUsername("ghost")).thenReturn(null);

		userSecurityService.loadUserByUsername("ghost");
	}
}
