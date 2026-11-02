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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.Cookie;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.resource.AppointmentResource;
import com.userFront.resource.CsrfResource;
import com.userFront.resource.UserResource;
import com.userFront.service.AppointmentService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {UserResource.class, AppointmentResource.class, CsrfResource.class})
@Import(SecurityConfig.class)
public class CsrfProtectionTest {

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
    public void loginWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/index").param("username", "admin").param("password", "pw"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void enableUserWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/user/bob/enable"))
                .andExpect(status().isForbidden());

        verify(userService, never()).enableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void enableUserWithCsrfTokenSucceeds() throws Exception {
        mockMvc.perform(post("/api/user/bob/enable").with(csrf()))
                .andExpect(status().isOk());

        verify(userService).enableUser("bob");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void enableUserViaGetIsRejected() throws Exception {
        mockMvc.perform(get("/api/user/bob/enable"))
                .andExpect(status().isMethodNotAllowed());

        verify(userService, never()).enableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void disableUserWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/user/bob/disable"))
                .andExpect(status().isForbidden());

        verify(userService, never()).disableUser(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void confirmAppointmentRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(get("/api/appointment/1/confirm"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/appointment/1/confirm"))
                .andExpect(status().isForbidden());
        verify(appointmentService, never()).confirmAppointment(anyLong());

        mockMvc.perform(post("/api/appointment/1/confirm").with(csrf()))
                .andExpect(status().isOk());
        verify(appointmentService).confirmAppointment(1L);
    }

    // csrf() swaps the CsrfFilter's token repository in the cached context; start from a clean one.
    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @WithMockUser(roles = "ADMIN")
    public void xsrfCookieIssuedByCsrfEndpointIsAcceptedAsHeader() throws Exception {
        Cookie xsrf = mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(xsrf);
        assertFalse(xsrf.isHttpOnly());

        mockMvc.perform(post("/api/user/bob/enable").cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue()))
                .andExpect(status().isOk());

        verify(userService).enableUser("bob");
    }

    // csrf() swaps the CsrfFilter's token repository in the cached context; start from a clean one.
    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @WithMockUser(roles = "ADMIN")
    public void mismatchedXsrfHeaderIsForbidden() throws Exception {
        Cookie xsrf = mockMvc.perform(get("/api/csrf"))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/user/bob/enable").cookie(xsrf).header("X-XSRF-TOKEN", "forged"))
                .andExpect(status().isForbidden());

        verify(userService, never()).enableUser(anyString());
    }

    @Test
    @WithMockUser
    public void logoutRequiresPostWithCsrfToken() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(redirectedUrl("/index?logout"));
    }

    @Test
    public void preflightAllowsXsrfHeader() throws Exception {
        mockMvc.perform(options("/api/user/bob/enable")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("x-xsrf-token")));
    }
}
