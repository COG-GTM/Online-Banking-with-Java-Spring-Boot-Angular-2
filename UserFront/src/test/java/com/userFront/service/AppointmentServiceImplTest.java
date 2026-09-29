package com.userFront.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.userFront.dao.AppointmentDao;
import com.userFront.domain.Appointment;
import com.userFront.service.UserServiceImpl.AppointmentServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceImplTest {

	@Mock
	private AppointmentDao appointmentDao;

	@InjectMocks
	private AppointmentServiceImpl appointmentService;

	@Test
	public void createAppointmentAlwaysInsertsUnconfirmed() {
		Appointment appointment = new Appointment();
		appointment.setId(42L);
		appointment.setConfirmed(true);

		appointmentService.createAppointment(appointment);

		ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);
		verify(appointmentDao).save(saved.capture());
		assertNull(saved.getValue().getId());
		assertFalse(saved.getValue().isConfirmed());
	}
}
