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

public class AppointmentServiceMassAssignmentTest {

	@Mock
	private AppointmentDao appointmentDao;

	@InjectMocks
	private AppointmentServiceImpl appointmentService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createAlwaysInsertsUnconfirmedRow() {
		Appointment appointment = new Appointment();
		appointment.setId(7L);
		appointment.setConfirmed(true);

		appointmentService.createAppointment(appointment);

		ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
		verify(appointmentDao).save(captor.capture());
		assertNull("create must never merge over an existing row", captor.getValue().getId());
		assertFalse("create must never persist a confirmed appointment", captor.getValue().isConfirmed());
	}
}
