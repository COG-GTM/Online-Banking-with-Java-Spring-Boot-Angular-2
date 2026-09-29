package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;

import com.userFront.domain.User;

public class HomeControllerSignupBindingTests {

	@Test
	public void signupBinderIgnoresProtectedUserFields() {
		User user = new User();
		WebDataBinder binder = new WebDataBinder(user, "user");
		new HomeController().initUserBinder(binder);

		MutablePropertyValues values = new MutablePropertyValues();
		values.add("username", "newuser");
		values.add("password", "secret");
		values.add("firstName", "First");
		values.add("lastName", "Last");
		values.add("email", "new@example.com");
		values.add("phone", "555");
		values.add("userId", "1");
		values.add("enabled", "false");
		values.add("primaryAccount.id", "1");
		values.add("savingsAccount.id", "1");
		values.add("recipientList[0].id", "1");
		values.add("appointmentList[0].id", "1");
		binder.bind(values);

		assertEquals("newuser", user.getUsername());
		assertEquals("secret", user.getPassword());
		assertEquals("First", user.getFirstName());
		assertEquals("Last", user.getLastName());
		assertEquals("new@example.com", user.getEmail());
		assertEquals("555", user.getPhone());

		assertNull(user.getUserId());
		assertTrue(user.isEnabled());
		assertNull(user.getPrimaryAccount());
		assertNull(user.getSavingsAccount());
		assertNull(user.getRecipientList());
		assertNull(user.getAppointmentList());
	}
}
