package com.userFront.resource.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.userFront.domain.Appointment;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;

public class AdminDtoSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static User user() {
        PrimaryAccount primary = new PrimaryAccount();
        primary.setId(11L);
        primary.setAccountNumber(11223101);
        primary.setAccountBalance(new BigDecimal("100.50"));
        SavingsAccount savings = new SavingsAccount();
        savings.setId(12L);
        savings.setAccountNumber(11223102);
        savings.setAccountBalance(new BigDecimal("2000.00"));

        User user = new User();
        user.setUserId(7L);
        user.setUsername("alice");
        user.setPassword("$2a$12$secret-hash");
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@example.com");
        user.setPhone("555-0100");
        user.setEnabled(false);
        user.setPrimaryAccount(primary);
        user.setSavingsAccount(savings);
        return user;
    }

    private static Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            names.add(it.next());
        }
        return names;
    }

    @Test
    public void userSummaryExposesOnlyAdminFields() throws Exception {
        JsonNode node = mapper.readTree(mapper.writeValueAsString(UserSummary.from(user())));

        assertEquals(new HashSet<>(Arrays.asList("userId", "username", "firstName", "lastName",
                "email", "phone", "enabled", "primaryAccount", "savingsAccount")), fieldNames(node));
        assertFalse(node.get("enabled").asBoolean());
        assertEquals(new HashSet<>(Arrays.asList("accountBalance")), fieldNames(node.get("primaryAccount")));
        assertEquals(100.50, node.get("primaryAccount").get("accountBalance").asDouble(), 0.001);
        assertEquals(2000.00, node.get("savingsAccount").get("accountBalance").asDouble(), 0.001);
    }

    @Test
    public void userSummaryToleratesMissingAccounts() throws Exception {
        User user = user();
        user.setPrimaryAccount(null);
        user.setSavingsAccount(null);

        JsonNode node = mapper.readTree(mapper.writeValueAsString(UserSummary.from(user)));

        assertTrue(node.get("primaryAccount").isNull());
        assertTrue(node.get("savingsAccount").isNull());
    }

    @Test
    public void appointmentSummaryExposesOnlyUsername() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setId(3L);
        appointment.setDate(new Date(0));
        appointment.setLocation("Branch 1");
        appointment.setDescription("Mortgage");
        appointment.setConfirmed(true);
        appointment.setUser(user());

        JsonNode node = mapper.readTree(mapper.writeValueAsString(AppointmentSummary.from(appointment)));

        assertEquals(new HashSet<>(Arrays.asList("id", "date", "location", "description", "confirmed", "user")),
                fieldNames(node));
        assertEquals(new HashSet<>(Arrays.asList("username")), fieldNames(node.get("user")));
        assertEquals("alice", node.get("user").get("username").asText());
        assertTrue(node.get("confirmed").asBoolean());
    }
}
