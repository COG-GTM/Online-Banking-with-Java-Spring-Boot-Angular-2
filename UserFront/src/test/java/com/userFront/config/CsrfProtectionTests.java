package com.userFront.config;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.controller.TransferController;
import com.userFront.resource.AppointmentResource;
import com.userFront.resource.UserResource;
import com.userFront.service.AppointmentService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {UserResource.class, AppointmentResource.class, TransferController.class})
@Import(SecurityConfig.class)
public class CsrfProtectionTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private UserSecurityService userSecurityService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void userEnableDisableRejectGet() throws Exception {
        mockMvc.perform(get("/api/user/alice/enable")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/api/user/alice/disable")).andExpect(status().isMethodNotAllowed());
        verify(userService, never()).enableUser(anyString());
        verify(userService, never()).disableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void userEnableDisableRejectPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/user/alice/enable")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/user/alice/disable")).andExpect(status().isForbidden());
        verify(userService, never()).enableUser(anyString());
        verify(userService, never()).disableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void userEnableDisableAcceptPostWithCsrfToken() throws Exception {
        mockMvc.perform(post("/api/user/alice/enable").with(csrf())).andExpect(status().isOk());
        mockMvc.perform(post("/api/user/alice/disable").with(csrf())).andExpect(status().isOk());
        verify(userService).enableUser("alice");
        verify(userService).disableUser("alice");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void appointmentConfirmRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/api/appointment/1/confirm")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/appointment/1/confirm")).andExpect(status().isForbidden());
        verify(appointmentService, never()).confirmAppointment(anyLong());

        mockMvc.perform(post("/api/appointment/1/confirm").with(csrf())).andExpect(status().isOk());
        verify(appointmentService).confirmAppointment(1L);
    }

    @Test
    @WithMockUser
    public void recipientDeleteRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/transfer/recipient/delete").param("recipientName", "bob"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/transfer/recipient/delete").param("recipientName", "bob"))
                .andExpect(status().isForbidden());
        verify(transactionService, never()).deleteRecipientByName(anyString());

        mockMvc.perform(post("/transfer/recipient/delete").param("recipientName", "bob").with(csrf()))
                .andExpect(status().isOk());
        verify(transactionService).deleteRecipientByName("bob");
    }
}
