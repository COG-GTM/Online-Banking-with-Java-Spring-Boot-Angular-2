package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.security.SignupRateLimiter;
import com.userFront.service.UserService;

public class HomeControllerTest {

	private UserService userService;
	private SignupRateLimiter rateLimiter;
	private HomeController controller;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		rateLimiter = mock(SignupRateLimiter.class);
		when(rateLimiter.tryAcquire(anyString())).thenReturn(true);
		controller = new HomeController();
		ReflectionTestUtils.setField(controller, "userService", userService);
		ReflectionTestUtils.setField(controller, "roleDao", mock(RoleDao.class));
		ReflectionTestUtils.setField(controller, "signupRateLimiter", rateLimiter);
		request = new MockHttpServletRequest();
		request.setRemoteAddr("10.0.0.1");
		response = new MockHttpServletResponse();
	}

	private User user(String username, String email) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		return user;
	}

	@Test
	public void existingUsernameAndExistingEmailProduceIdenticalModel() {
		when(userService.checkUserExists("taken", "free@example.com")).thenReturn(true);
		when(userService.checkUserExists("free", "taken@example.com")).thenReturn(true);
		when(userService.checkUsernameExists("taken")).thenReturn(true);
		when(userService.checkEmailExists("taken@example.com")).thenReturn(true);

		ExtendedModelMap usernameModel = new ExtendedModelMap();
		ExtendedModelMap emailModel = new ExtendedModelMap();
		User takenUsername = user("taken", "free@example.com");
		User takenEmail = user("free", "taken@example.com");

		assertEquals("signup", controller.signupPost(takenUsername, usernameModel, request, response));
		assertEquals("signup", controller.signupPost(takenEmail, emailModel, request, response));

		assertEquals(usernameModel.keySet(), emailModel.keySet());
		assertEquals(Boolean.TRUE, usernameModel.get("signupUnavailable"));
		assertFalse(usernameModel.containsKey("usernameExists"));
		assertFalse(emailModel.containsKey("emailExists"));
		verify(userService, never()).createUser(any(User.class), any(Set.class));
	}

	@Test
	public void uniqueConstraintViolationIsReportedGenerically() {
		when(userService.createUser(any(User.class), any(Set.class)))
				.thenThrow(new DataIntegrityViolationException("duplicate"));
		ExtendedModelMap model = new ExtendedModelMap();

		String view = controller.signupPost(user("new", "race@example.com"), model, request, response);

		assertEquals("signup", view);
		assertEquals(Boolean.TRUE, model.get("signupUnavailable"));
		assertEquals(200, response.getStatus());
	}

	@Test
	public void successfulSignupRedirects() {
		ExtendedModelMap model = new ExtendedModelMap();

		String view = controller.signupPost(user("new", "new@example.com"), model, request, response);

		assertEquals("redirect:/", view);
		verify(userService).createUser(any(User.class), any(Set.class));
	}

	@Test
	public void rateLimitedRequestsAreRejectedBeforeLookup() {
		when(rateLimiter.tryAcquire("10.0.0.1")).thenReturn(false);
		ExtendedModelMap model = new ExtendedModelMap();

		String view = controller.signupPost(user("any", "any@example.com"), model, request, response);

		assertEquals("signup", view);
		assertEquals(429, response.getStatus());
		assertTrue(model.containsKey("signupRateLimited"));
		verify(userService, never()).checkUserExists(anyString(), anyString());
	}
}
