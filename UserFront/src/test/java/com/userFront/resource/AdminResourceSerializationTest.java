package com.userFront.resource;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userFront.domain.Appointment;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.service.AppointmentService;
import com.userFront.service.UserService;

public class AdminResourceSerializationTest {

    private static final String PASSWORD_HASH = "$2a$12$abcdefghijklmnopqrstuuSecretHashValue";
    private static final String RECIPIENT_ACCOUNT = "RECIPIENT-ACCT-998877";

    private MockMvc mockMvc;
    private User user;

    @Before
    public void setUp() {
        user = sampleUser();

        UserService userService = mock(UserService.class);
        when(userService.findUserList()).thenReturn(Collections.singletonList(user));
        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userService", userService);

        Appointment appointment = new Appointment();
        appointment.setId(7L);
        appointment.setDate(new Date(0));
        appointment.setLocation("Main branch");
        appointment.setDescription("Mortgage review");
        appointment.setUser(user);
        AppointmentService appointmentService = mock(AppointmentService.class);
        when(appointmentService.findAll()).thenReturn(Collections.singletonList(appointment));
        AppointmentResource appointmentResource = new AppointmentResource();
        ReflectionTestUtils.setField(appointmentResource, "appointmentService", appointmentService);

        mockMvc = MockMvcBuilders.standaloneSetup(userResource, appointmentResource).build();
    }

    @Test
    public void userListReturnsOnlyAdminPortalFields() throws Exception {
        mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[0].primaryAccount.accountBalance").value(1250.5))
                .andExpect(jsonPath("$[0].savingsAccount.accountBalance").value(300))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].recipientList").doesNotExist())
                .andExpect(jsonPath("$[0].authorities").doesNotExist())
                .andExpect(jsonPath("$[0].primaryAccount.accountNumber").doesNotExist())
                .andExpect(jsonPath("$[0].savingsAccount.accountNumber").doesNotExist())
                .andExpect(jsonPath("$[0].primaryAccount.id").doesNotExist());

        String body = mockMvc.perform(get("/api/user/all")).andReturn().getResponse().getContentAsString();
        assertThat(body, not(containsString(PASSWORD_HASH)));
        assertThat(body, not(containsString(RECIPIENT_ACCOUNT)));
    }

    @Test
    public void appointmentListEmbedsOnlyUsername() throws Exception {
        mockMvc.perform(get("/api/appointment/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].description").value("Mortgage review"))
                .andExpect(jsonPath("$[0].confirmed").value(false))
                .andExpect(jsonPath("$[0].user.username").value("alice"))
                .andExpect(jsonPath("$[0].user.password").doesNotExist())
                .andExpect(jsonPath("$[0].user.recipientList").doesNotExist())
                .andExpect(jsonPath("$[0].user.primaryAccount").doesNotExist())
                .andExpect(jsonPath("$[0].user.email").doesNotExist());

        String body = mockMvc.perform(get("/api/appointment/all")).andReturn().getResponse().getContentAsString();
        assertThat(body, not(containsString(PASSWORD_HASH)));
        assertThat(body, not(containsString(RECIPIENT_ACCOUNT)));
    }

    @Test
    public void userEntitySerializationOmitsPasswordAndRecipients() throws Exception {
        String json = new ObjectMapper().writeValueAsString(user);

        assertThat(json, not(containsString("\"password\"")));
        assertThat(json, not(containsString(PASSWORD_HASH)));
        assertThat(json, not(containsString("recipientList")));
        assertThat(json, not(containsString(RECIPIENT_ACCOUNT)));
    }

    @Test
    public void appointmentEntitySerializationOmitsUser() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setUser(user);

        String json = new ObjectMapper().writeValueAsString(appointment);

        assertThat(json, not(containsString("\"user\"")));
        assertThat(json, not(containsString(PASSWORD_HASH)));
    }

    @Test
    public void toStringOmitsPasswordAndRecipients() {
        String text = user.toString();

        assertFalse(text.contains(PASSWORD_HASH));
        assertFalse(text.contains("password"));
        assertFalse(text.contains(RECIPIENT_ACCOUNT));
    }

    private static User sampleUser() {
        PrimaryAccount primary = new PrimaryAccount();
        primary.setId(11L);
        primary.setAccountNumber(11223101);
        primary.setAccountBalance(new BigDecimal("1250.50"));

        SavingsAccount savings = new SavingsAccount();
        savings.setId(12L);
        savings.setAccountNumber(11223102);
        savings.setAccountBalance(new BigDecimal("300"));

        User user = new User();
        user.setUserId(1L);
        user.setUsername("alice");
        user.setPassword(PASSWORD_HASH);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@example.com");
        user.setPhone("555-0100");
        user.setPrimaryAccount(primary);
        user.setSavingsAccount(savings);

        Recipient recipient = new Recipient();
        recipient.setName("Bob Payee");
        recipient.setEmail("bob@example.com");
        recipient.setAccountNumber(RECIPIENT_ACCOUNT);
        recipient.setUser(user);
        user.setRecipientList(Collections.singletonList(recipient));
        return user;
    }
}
