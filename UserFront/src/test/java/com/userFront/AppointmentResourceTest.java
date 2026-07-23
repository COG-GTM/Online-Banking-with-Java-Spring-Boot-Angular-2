package com.userFront;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.userFront.domain.Appointment;
import com.userFront.service.AppointmentService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppointmentResourceTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockBean
    private AppointmentService appointmentService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    private static MockHttpSession sessionWithRole(String role) {
        List<GrantedAuthority> authorities = Collections
                .<GrantedAuthority>singletonList(new SimpleGrantedAuthority(role));
        Authentication authentication = new UsernamePasswordAuthenticationToken("tester", "n/a", authorities);
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return session;
    }

    @Test
    public void findAppointmentListAllowsAdmin() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setId(7L);
        appointment.setLocation("Uptown Branch");
        when(appointmentService.findAll()).thenReturn(Collections.singletonList(appointment));

        mockMvc.perform(get("/api/appointment/all").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].location").value("Uptown Branch"));

        verify(appointmentService, times(1)).findAll();
    }

    @Test
    public void findAppointmentListForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/appointment/all").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void confirmAppointmentAllowsAdmin() throws Exception {
        mockMvc.perform(get("/api/appointment/5/confirm").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk());

        verify(appointmentService, times(1)).confirmAppointment(5L);
    }

    @Test
    public void confirmAppointmentForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/appointment/5/confirm").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());

        verify(appointmentService, times(0)).confirmAppointment(5L);
    }
}
