package com.userFront.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

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
    private User testUser;

    @Before
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                .setViewResolvers(viewResolver).build();

        testUser = new User();
        testUser.setUsername("testuser");
    }

    @Test
    public void testCreateAppointmentGet() throws Exception {
        mockMvc.perform(get("/appointment/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointment"))
                .andExpect(model().attributeExists("appointment"))
                .andExpect(model().attributeExists("dateString"));
    }

    @Test
    public void testCreateAppointmentPost() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(appointmentService.createAppointment(any(Appointment.class))).thenReturn(new Appointment());

        Principal principal = () -> "testuser";

        mockMvc.perform(post("/appointment/create")
                .param("dateString", "2024-01-15 10:30")
                .param("location", "Main Branch")
                .param("description", "Loan inquiry")
                .principal(principal))
                .andExpect(status().is3xxRedirection());

        verify(appointmentService).createAppointment(any(Appointment.class));
    }
}
