package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;
import com.userFront.service.AppointmentService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentControllerTest {

	@Mock
	private AppointmentService appointmentService;

	@Mock
	private UserService userService;

	@InjectMocks
	private AppointmentController appointmentController;

	private MockMvc mockMvc;

	private User attacker;

	private Principal principal;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
		attacker = new User();
		attacker.setUsername("attacker");
		principal = new Principal() {
			public String getName() {
				return "attacker";
			}
		};
		when(userService.findByUsername("attacker")).thenReturn(attacker);
	}

	@Test
	public void createIgnoresClientSuppliedIdConfirmedAndUser() throws Exception {
		mockMvc.perform(post("/appointment/create").principal(principal)
				.param("id", "42")
				.param("confirmed", "true")
				.param("user.userId", "7")
				.param("user.username", "victim")
				.param("dateString", "2026-10-01 10:30")
				.param("location", "Boston")
				.param("description", "Mortgage review"))
				.andExpect(redirectedUrl("/userFront"));

		ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
		verify(appointmentService).createAppointment(saved.capture());
		Appointment appointment = saved.getValue();

		assertNull(appointment.getId());
		assertFalse(appointment.isConfirmed());
		assertSame(attacker, appointment.getUser());
		assertEquals("attacker", appointment.getUser().getUsername());
		assertEquals("Boston", appointment.getLocation());
		assertEquals("Mortgage review", appointment.getDescription());
		assertNotNull(appointment.getDate());
	}
}
