package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;
import com.userFront.service.AppointmentService;
import com.userFront.service.UserService;

public class AppointmentMassAssignmentTest {

	@Mock
	private AppointmentService appointmentService;

	@Mock
	private UserService userService;

	@InjectMocks
	private AppointmentController controller;

	private MockMvc mockMvc;

	private User mallory;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		mallory = new User();
		mallory.setUsername("mallory");
		when(userService.findByUsername("mallory")).thenReturn(mallory);
	}

	@Test
	public void createIgnoresClientSuppliedIdConfirmedAndUser() throws Exception {
		Principal principal = () -> "mallory";

		mockMvc.perform(post("/appointment/create")
				.principal(principal)
				.param("id", "7")
				.param("confirmed", "true")
				.param("user.userId", "1")
				.param("user.username", "alice")
				.param("dateString", "2026-12-01 10:00")
				.param("location", "Boston")
				.param("description", "takeover"))
				.andExpect(status().is3xxRedirection());

		ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
		verify(appointmentService).createAppointment(captor.capture());
		Appointment saved = captor.getValue();

		assertNull("client-supplied id must not reach save()", saved.getId());
		assertFalse("client must not be able to self-confirm", saved.isConfirmed());
		assertSame("owner must be the authenticated principal", mallory, saved.getUser());
		assertEquals("mallory", saved.getUser().getUsername());
		assertEquals("Boston", saved.getLocation());
		assertEquals("takeover", saved.getDescription());
	}
}
