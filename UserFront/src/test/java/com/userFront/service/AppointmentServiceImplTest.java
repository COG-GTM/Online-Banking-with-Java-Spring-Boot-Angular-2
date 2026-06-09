package com.userFront.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.userFront.dao.AppointmentDao;
import com.userFront.domain.Appointment;
import com.userFront.service.UserServiceImpl.AppointmentServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceImplTest {

    @Mock
    private AppointmentDao appointmentDao;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment testAppointment;

    @Before
    public void setUp() {
        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setDate(new Date());
        testAppointment.setLocation("Branch Office");
        testAppointment.setDescription("Account inquiry");
        testAppointment.setConfirmed(false);
    }

    @Test
    public void testCreateAppointment() {
        when(appointmentDao.save(testAppointment)).thenReturn(testAppointment);

        Appointment result = appointmentService.createAppointment(testAppointment);

        assertEquals(testAppointment, result);
        verify(appointmentDao).save(testAppointment);
    }

    @Test
    public void testFindAll() {
        List<Appointment> appointments = Arrays.asList(testAppointment, new Appointment());
        when(appointmentDao.findAll()).thenReturn(appointments);

        List<Appointment> result = appointmentService.findAll();

        assertEquals(2, result.size());
        verify(appointmentDao).findAll();
    }

    @Test
    public void testFindAppointment() {
        when(appointmentDao.findOne(1L)).thenReturn(testAppointment);

        Appointment result = appointmentService.findAppointment(1L);

        assertEquals(testAppointment, result);
        verify(appointmentDao).findOne(1L);
    }

    @Test
    public void testConfirmAppointment() {
        when(appointmentDao.findOne(1L)).thenReturn(testAppointment);

        appointmentService.confirmAppointment(1L);

        assertTrue(testAppointment.isConfirmed());
        verify(appointmentDao).save(testAppointment);
    }
}
