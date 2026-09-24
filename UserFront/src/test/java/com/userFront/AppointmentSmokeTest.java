package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.AppointmentService;
import com.userFront.service.UserService;

public class AppointmentSmokeTest extends AbstractIntegrationTest {

	private static final String USERNAME = "smokeuser";

	@Autowired
	private UserService userService;

	@Autowired
	private AppointmentService appointmentService;

	@Before
	public void createUser() {
		if (userService.findByUsername(USERNAME) != null) {
			return;
		}

		User user = new User();
		user.setUsername(USERNAME);
		user.setPassword("password");
		user.setFirstName("Smoke");
		user.setLastName("User");
		user.setEmail("smokeuser@example.com");
		user.setPhone("555-0100");

		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");

		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, role));

		userService.createUser(user, userRoles);
	}

	@Test
	public void createsAppointmentFromThymeleafForm() throws Exception {
		mockMvc.perform(post("/appointment/create")
				.param("dateString", "2016-10-21 15:25")
				.param("location", "Boston")
				.param("description", "Smoke test appointment")
				.with(user(USERNAME).roles("USER")))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		Appointment appointment = latestAppointment();
		assertNotNull(appointment.getDate());
		assertEquals("Boston", appointment.getLocation());
		assertEquals("Smoke test appointment", appointment.getDescription());
		assertEquals(USERNAME, appointment.getUser().getUsername());

		assertEquals("2016-10-21 15:25", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(appointment.getDate()));
	}

	@Test
	public void parsesNoonWithTwelveHourClockQuirk() throws Exception {
		mockMvc.perform(post("/appointment/create")
				.param("dateString", "2016-10-21 12:30")
				.param("location", "San Francisco")
				.param("description", "Noon appointment")
				.with(user(USERNAME).roles("USER")))
				.andExpect(status().is3xxRedirection());

		// SimpleDateFormat("yyyy-MM-dd hh:mm") reads the hour in the 12-hour field,
		// so the form's noon is stored as midnight.
		assertEquals("2016-10-21 00:30",
				new SimpleDateFormat("yyyy-MM-dd HH:mm").format(latestAppointment().getDate()));
	}

	@Test
	public void listsAppointmentsForAdmin() throws Exception {
		Appointment appointment = persistAppointment("New York", "Listed appointment");

		mockMvc.perform(get("/api/appointment/all").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == " + appointment.getId() + ")].location").value("New York"));
	}

	@Test
	public void confirmsAppointmentOverGet() throws Exception {
		Appointment appointment = persistAppointment("Chicago", "Confirmed appointment");

		mockMvc.perform(get("/api/appointment/" + appointment.getId() + "/confirm")
				.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());

		assertTrue(appointmentService.findAppointment(appointment.getId()).isConfirmed());
	}

	private Appointment persistAppointment(String location, String description) {
		Appointment appointment = new Appointment();
		appointment.setDate(new Date());
		appointment.setLocation(location);
		appointment.setDescription(description);
		appointment.setUser(userService.findByUsername(USERNAME));

		return appointmentService.createAppointment(appointment);
	}

	private Appointment latestAppointment() {
		Appointment latest = null;
		for (Appointment appointment : appointmentService.findAll()) {
			if (latest == null || appointment.getId() > latest.getId()) {
				latest = appointment;
			}
		}
		assertNotNull(latest);

		return latest;
	}
}
