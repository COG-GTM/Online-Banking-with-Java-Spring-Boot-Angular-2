package com.userFront.resource;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.Appointment;
import com.userFront.service.AppointmentService;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentResourceTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentResource appointmentResource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentResource).build();
    }

    @Test
    public void testFindAppointmentList() throws Exception {
        Appointment apt1 = new Appointment();
        apt1.setId(1L);
        apt1.setLocation("Branch A");
        apt1.setDescription("Loan inquiry");
        apt1.setDate(new Date());

        Appointment apt2 = new Appointment();
        apt2.setId(2L);
        apt2.setLocation("Branch B");
        apt2.setDescription("Account opening");
        apt2.setDate(new Date());

        when(appointmentService.findAll()).thenReturn(Arrays.asList(apt1, apt2));

        mockMvc.perform(get("/api/appointment/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("Branch A"))
                .andExpect(jsonPath("$[1].location").value("Branch B"));
    }

    @Test
    public void testConfirmAppointment() throws Exception {
        mockMvc.perform(get("/api/appointment/1/confirm"))
                .andExpect(status().isOk());

        verify(appointmentService).confirmAppointment(1L);
    }
}
