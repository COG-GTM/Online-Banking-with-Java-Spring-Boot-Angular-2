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

public class AppointmentControllerTest {

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
		Principal principal = new Principal() {
			public String getName() {
				return "mallory";
			}
		};

		mockMvc.perform(post("/appointment/create").principal(principal)
				.param("id", "7")
				.param("confirmed", "true")
				.param("user.userId", "1")
				.param("user.username", "alice")
				.param("dateString", "2026-12-01 10:00")
				.param("location", "Boston")
				.param("description", "takeover"))
				.andExpect(status().is3xxRedirection());

		ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
		verify(appointmentService).createAppointment(saved.capture());
		Appointment appointment = saved.getValue();
		assertNull(appointment.getId());
		assertFalse(appointment.isConfirmed());
		assertSame(mallory, appointment.getUser());
		assertEquals("mallory", appointment.getUser().getUsername());
		assertEquals("Boston", appointment.getLocation());
		assertEquals("takeover", appointment.getDescription());
	}
}
