package com.userFront;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
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

    private Appointment appointment;

    @Before
    public void setUp() {
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setLocation("Downtown Branch");
        appointment.setDescription("Loan consultation");
        appointment.setConfirmed(false);
    }

    @Test
    public void createAppointmentDelegatesToDaoSave() {
        when(appointmentDao.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.createAppointment(appointment);

        assertThat(result).isSameAs(appointment);
        verify(appointmentDao, times(1)).save(appointment);
    }

    @Test
    public void findAllReturnsAppointmentsFromDao() {
        Appointment second = new Appointment();
        second.setId(2L);
        List<Appointment> appointments = Arrays.asList(appointment, second);
        when(appointmentDao.findAll()).thenReturn(appointments);

        List<Appointment> result = appointmentService.findAll();

        assertThat(result).hasSize(2).containsExactly(appointment, second);
        verify(appointmentDao, times(1)).findAll();
    }

    @Test
    public void findAppointmentReturnsAppointmentFromDao() {
        when(appointmentDao.findOne(1L)).thenReturn(appointment);

        Appointment result = appointmentService.findAppointment(1L);

        assertThat(result).isSameAs(appointment);
        verify(appointmentDao, times(1)).findOne(1L);
    }

    @Test
    public void confirmAppointmentFlipsConfirmedFlagAndSaves() {
        when(appointmentDao.findOne(1L)).thenReturn(appointment);

        appointmentService.confirmAppointment(1L);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentDao, times(1)).findOne(1L);
        verify(appointmentDao, times(1)).save(captor.capture());
        assertThat(captor.getValue().isConfirmed()).isTrue();
        assertThat(appointment.isConfirmed()).isTrue();
    }
}
