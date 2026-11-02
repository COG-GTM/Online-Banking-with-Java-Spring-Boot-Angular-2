package com.userFront.resource;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.service.AppointmentService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

public class AdminActionMethodTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private UserResource userResource;

    @InjectMocks
    private AppointmentResource appointmentResource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userResource, appointmentResource).build();
    }

    @Test
    public void stateChangingAdminActionsRejectGet() throws Exception {
        mockMvc.perform(get("/api/user/alice/enable")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/api/user/alice/disable")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/api/appointment/7/confirm")).andExpect(status().isMethodNotAllowed());

        verify(userService, never()).enableUser(anyString());
        verify(userService, never()).disableUser(anyString());
        verify(appointmentService, never()).confirmAppointment(anyLong());
    }

    @Test
    public void stateChangingAdminActionsAcceptPost() throws Exception {
        mockMvc.perform(post("/api/user/alice/enable")).andExpect(status().isOk());
        mockMvc.perform(post("/api/user/alice/disable")).andExpect(status().isOk());
        mockMvc.perform(post("/api/appointment/7/confirm")).andExpect(status().isOk());

        verify(userService).enableUser("alice");
        verify(userService).disableUser("alice");
        verify(appointmentService).confirmAppointment(7L);
    }

    @Test
    public void encodedUsernameIsBoundAsSinglePathVariable() throws Exception {
        mockMvc.perform(post("/api/user/{username}/disable", "john.doe")).andExpect(status().isOk());

        verify(userService).disableUser("john.doe");
    }
}
