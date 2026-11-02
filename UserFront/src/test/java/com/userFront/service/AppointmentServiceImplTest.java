package com.userFront.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.userFront.dao.AppointmentDao;
import com.userFront.domain.Appointment;
import com.userFront.service.UserServiceImpl.AppointmentServiceImpl;

public class AppointmentServiceImplTest {

	@Mock
	private AppointmentDao appointmentDao;

	@InjectMocks
	private AppointmentServiceImpl appointmentService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createAlwaysInsertsUnconfirmedAppointment() {
		Appointment appointment = new Appointment();
		appointment.setId(7L);
		appointment.setConfirmed(true);

		appointmentService.createAppointment(appointment);

		ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
		verify(appointmentDao).save(saved.capture());
		assertNull(saved.getValue().getId());
		assertFalse(saved.getValue().isConfirmed());
	}
}
