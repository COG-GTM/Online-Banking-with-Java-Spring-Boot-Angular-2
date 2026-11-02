package com.userFront.config;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.Cookie;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.controller.TransferController;
import com.userFront.resource.AppointmentResource;
import com.userFront.resource.CsrfResource;
import com.userFront.resource.UserResource;
import com.userFront.service.AccountService;
import com.userFront.service.AppointmentService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {UserResource.class, AppointmentResource.class, CsrfResource.class, TransferController.class})
@Import(SecurityConfig.class)
// csrf() replaces the CsrfFilter's token repository in the cached context, so give each test a fresh one.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CsrfProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private UserSecurityService userSecurityService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void confirmAppointmentViaGetIsRejected() throws Exception {
        mockMvc.perform(get("/api/appointment/1/confirm"))
                .andExpect(status().isMethodNotAllowed());

        verify(appointmentService, never()).confirmAppointment(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void confirmAppointmentWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/appointment/1/confirm"))
                .andExpect(status().isForbidden());

        verify(appointmentService, never()).confirmAppointment(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void confirmAppointmentWithCsrfTokenSucceeds() throws Exception {
        mockMvc.perform(post("/api/appointment/1/confirm").with(csrf()))
                .andExpect(status().isOk());

        verify(appointmentService).confirmAppointment(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void enableAndDisableUserViaGetAreRejected() throws Exception {
        mockMvc.perform(get("/api/user/bob/enable")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/api/user/bob/disable")).andExpect(status().isMethodNotAllowed());

        verify(userService, never()).enableUser(anyString());
        verify(userService, never()).disableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void enableAndDisableUserWithoutCsrfTokenAreForbidden() throws Exception {
        mockMvc.perform(post("/api/user/bob/enable")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/user/bob/disable")).andExpect(status().isForbidden());

        verify(userService, never()).enableUser(anyString());
        verify(userService, never()).disableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void disableUserWithCsrfTokenSucceeds() throws Exception {
        mockMvc.perform(post("/api/user/bob/disable").with(csrf()))
                .andExpect(status().isOk());

        verify(userService).disableUser("bob");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void xsrfCookieFromCsrfEndpointIsAcceptedAsHeader() throws Exception {
        Cookie xsrf = mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isNoContent())
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(xsrf);
        assertFalse(xsrf.isHttpOnly());

        mockMvc.perform(post("/api/appointment/5/confirm").cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue()))
                .andExpect(status().isOk());

        verify(appointmentService).confirmAppointment(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void mismatchedXsrfHeaderIsForbidden() throws Exception {
        Cookie xsrf = mockMvc.perform(get("/api/csrf"))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/appointment/5/confirm").cookie(xsrf).header("X-XSRF-TOKEN", "forged"))
                .andExpect(status().isForbidden());

        verify(appointmentService, never()).confirmAppointment(anyLong());
    }

    @Test
    public void loginWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/index").param("username", "admin").param("password", "pw"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void recipientDeleteRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/transfer/recipient/delete").param("recipientName", "alice"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/transfer/recipient/delete").param("recipientName", "alice"))
                .andExpect(status().isForbidden());
        verify(transactionService, never()).deleteRecipientByName(anyString());

        mockMvc.perform(post("/transfer/recipient/delete").param("recipientName", "alice").with(csrf()))
                .andExpect(redirectedUrl("/transfer/recipient"));
        verify(transactionService).deleteRecipientByName("alice");
    }

    @Test
    @WithMockUser
    public void logoutRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(post("/logout")).andExpect(status().isForbidden());

        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(redirectedUrl("/index?logout"));
    }

    @Test
    public void loginRotatesXsrfCookieSoImmediateLogoutSucceeds() throws Exception {
        when(userSecurityService.loadUserByUsername("admin")).thenReturn(
                new User("admin", new BCryptPasswordEncoder(4).encode("pw"), Collections.emptyList()));

        Cookie preLogin = mockMvc.perform(get("/api/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        MvcResult login = mockMvc.perform(post("/index").param("username", "admin").param("password", "pw")
                        .cookie(preLogin).header("X-XSRF-TOKEN", preLogin.getValue()))
                .andExpect(redirectedUrl("/userFront"))
                .andReturn();

        Cookie rotated = Arrays.stream(login.getResponse().getCookies())
                .filter(c -> "XSRF-TOKEN".equals(c.getName()) && !c.getValue().isEmpty())
                .reduce((first, last) -> last).orElse(null);
        assertNotNull(rotated);
        assertFalse(rotated.getValue().equals(preLogin.getValue()));

        mockMvc.perform(post("/logout").session((MockHttpSession) login.getRequest().getSession(false))
                        .cookie(rotated).header("X-XSRF-TOKEN", rotated.getValue()))
                .andExpect(redirectedUrl("/index?logout"));
    }

    @Test
    public void preflightAllowsXsrfHeader() throws Exception {
        mockMvc.perform(options("/api/appointment/1/confirm")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("x-xsrf-token")));
    }
}
