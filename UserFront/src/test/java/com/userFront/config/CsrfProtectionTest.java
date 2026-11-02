package com.userFront.config;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.controller.AccountController;
import com.userFront.controller.TransferController;
import com.userFront.resource.AppointmentResource;
import com.userFront.resource.UserResource;
import com.userFront.service.AccountService;
import com.userFront.service.AppointmentService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { UserResource.class, AppointmentResource.class, TransferController.class,
        AccountController.class })
@Import(SecurityConfig.class)
public class CsrfProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityService userSecurityService;

    @MockBean
    private UserService userService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private AccountService accountService;

    @Test
    public void disableUserRejectsGet() throws Exception {
        mockMvc.perform(get("/api/user/alice/disable").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
        verify(userService, never()).disableUser(anyString());
    }

    @Test
    public void disableUserRejectsPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/user/alice/disable").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        verify(userService, never()).disableUser(anyString());
    }

    @Test
    public void disableUserAcceptsPostWithCsrfHeader() throws Exception {
        mockMvc.perform(post("/api/user/alice/disable").with(user("admin").roles("ADMIN")).with(csrf().asHeader()))
                .andExpect(status().isOk());
        verify(userService).disableUser("alice");
    }

    @Test
    public void enableUserRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/api/user/mallory/enable").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/user/mallory/enable").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        verify(userService, never()).enableUser(anyString());

        mockMvc.perform(post("/api/user/mallory/enable").with(user("admin").roles("ADMIN")).with(csrf().asHeader()))
                .andExpect(status().isOk());
        verify(userService).enableUser("mallory");
    }

    @Test
    public void confirmAppointmentRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/api/appointment/7/confirm").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/appointment/7/confirm").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        verify(appointmentService, never()).confirmAppointment(any(Long.class));

        mockMvc.perform(post("/api/appointment/7/confirm").with(user("admin").roles("ADMIN")).with(csrf().asHeader()))
                .andExpect(status().isOk());
        verify(appointmentService).confirmAppointment(7L);
    }

    @Test
    public void recipientDeleteRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/transfer/recipient/delete").param("recipientName", "bob").with(user("carol")))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/transfer/recipient/delete").param("recipientName", "bob").with(user("carol")))
                .andExpect(status().isForbidden());
        verify(transactionService, never()).deleteRecipientByName(anyString(), any(Principal.class));

        mockMvc.perform(post("/transfer/recipient/delete").param("recipientName", "bob").with(user("carol"))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/transfer/recipient"));
        verify(transactionService).deleteRecipientByName(eq("bob"), any(Principal.class));
    }

    @Test
    public void moneyMovementRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/account/withdraw").param("amount", "100").param("accountType", "Primary")
                .with(user("carol")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/transfer/toSomeoneElse").param("recipientName", "mallory")
                .param("accountType", "Primary").param("amount", "100").with(user("carol")))
                .andExpect(status().isForbidden());
        verify(accountService, never()).withdraw(anyString(), anyDouble(), any(Principal.class));
        verify(transactionService, never()).findRecipientByName(anyString(), any(Principal.class));
    }

    @Test
    public void logoutRequiresCsrfToken() throws Exception {
        mockMvc.perform(post("/logout").with(user("carol")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/logout").with(user("carol")).with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/index?logout"));
    }
}
