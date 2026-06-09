package com.userFront.domain;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class AppointmentTest {

    @Test
    public void testGettersAndSetters() {
        Appointment appointment = new Appointment();
        Date date = new Date();
        User user = new User();

        appointment.setId(1L);
        appointment.setDate(date);
        appointment.setLocation("Main Branch");
        appointment.setDescription("Account inquiry");
        appointment.setConfirmed(false);
        appointment.setUser(user);

        assertEquals(Long.valueOf(1L), appointment.getId());
        assertEquals(date, appointment.getDate());
        assertEquals("Main Branch", appointment.getLocation());
        assertEquals("Account inquiry", appointment.getDescription());
        assertFalse(appointment.isConfirmed());
        assertEquals(user, appointment.getUser());
    }

    @Test
    public void testConfirmed() {
        Appointment appointment = new Appointment();
        assertFalse(appointment.isConfirmed());

        appointment.setConfirmed(true);
        assertTrue(appointment.isConfirmed());
    }
}
