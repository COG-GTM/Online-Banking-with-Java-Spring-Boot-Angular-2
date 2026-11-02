package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;

import com.userFront.dto.SignupForm;
import com.userFront.service.UserService;

public class HomeControllerSignupTest {

	private UserService userService;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		when(userService.checkUserExists(anyString(), anyString())).thenReturn(false);

		HomeController controller = new HomeController();
		ReflectionTestUtils.setField(controller, "userService", userService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void signupIgnoresNonFormFields() throws Exception {
		mockMvc.perform(post("/signup")
				.param("firstName", "Mal").param("lastName", "Lory").param("phone", "555")
				.param("email", "mal@example.com").param("username", "mallory").param("password", "pw")
				.param("userId", "1")
				.param("enabled", "false")
				.param("userRoles[0].role.roleId", "2")
				.param("primaryAccount.id", "1"))
				.andExpect(redirectedUrl("/"));

		ArgumentCaptor<SignupForm> form = ArgumentCaptor.forClass(SignupForm.class);
		verify(userService).createUser(form.capture());
		assertEquals("mallory", form.getValue().getUsername());
		assertEquals("pw", form.getValue().getPassword());
	}

	@Test
	public void binderAllowsOnlySignupFields() {
		WebDataBinder binder = new WebDataBinder(new SignupForm(), "user");
		new HomeController().restrictSignupFields(binder);
		assertEquals(6, binder.getAllowedFields().length);
		for (String field : new String[] { "userId", "enabled", "userRoles[0].role.roleId" }) {
			assertEquals(false, java.util.Arrays.asList(binder.getAllowedFields()).contains(field));
		}
		assertNull(binder.getDisallowedFields());
	}
}
