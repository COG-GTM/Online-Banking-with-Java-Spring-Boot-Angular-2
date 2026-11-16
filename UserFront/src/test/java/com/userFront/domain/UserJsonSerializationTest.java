package com.userFront.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.userFront.dto.AppointmentDto;

public class UserJsonSerializationTest {

    private static final String HASH = "$2a$12$abcdefghijklmnopqrstuv";

    private final ObjectMapper mapper = new ObjectMapper();

    private User user() {
        User user = new User();
        user.setUserId(7L);
        user.setUsername("alice");
        user.setPassword(HASH);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@example.com");
        user.setPhone("555-0100");
        Recipient recipient = new Recipient();
        recipient.setName("Bob");
        recipient.setAccountNumber("123456");
        user.setRecipientList(Collections.singletonList(recipient));
        PrimaryAccount primary = new PrimaryAccount();
        primary.setAccountBalance(new java.math.BigDecimal("100.00"));
        user.setPrimaryAccount(primary);
        return user;
    }

    @Test
    public void userJsonOmitsPasswordAndRecipientsButKeepsAdminFields() throws Exception {
        String json = mapper.writeValueAsString(user());
        JsonNode node = mapper.readTree(json);

        assertFalse(node.has("password"));
        assertFalse(node.has("recipientList"));
        assertFalse(json.contains(HASH));
        assertEquals("alice", node.get("username").asText());
        assertEquals("Alice", node.get("firstName").asText());
        assertEquals("Smith", node.get("lastName").asText());
        assertEquals("alice@example.com", node.get("email").asText());
        assertEquals("555-0100", node.get("phone").asText());
        assertTrue(node.get("enabled").asBoolean());
        assertEquals(100.00, node.get("primaryAccount").get("accountBalance").asDouble(), 0.001);
    }

    @Test
    public void passwordIsStillAcceptedOnDeserialization() throws Exception {
        User user = mapper.readValue("{\"username\":\"bob\",\"password\":\"secret\"}", User.class);
        assertEquals("secret", user.getPassword());
    }

    @Test
    public void toStringOmitsPassword() {
        assertFalse(user().toString().contains(HASH));
    }

    @Test
    public void appointmentJsonExposesOnlyUsernameOfUser() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setId(3L);
        appointment.setDate(new Date());
        appointment.setDescription("Loan");
        appointment.setUser(user());

        String entityJson = mapper.writeValueAsString(appointment);
        assertFalse(mapper.readTree(entityJson).has("user"));
        assertFalse(entityJson.contains(HASH));

        String dtoJson = mapper.writeValueAsString(new AppointmentDto(appointment));
        JsonNode node = mapper.readTree(dtoJson);
        assertFalse(dtoJson.contains(HASH));
        assertEquals("alice", node.get("user").get("username").asText());
        assertEquals(2, node.get("user").size());
        assertEquals(3L, node.get("id").asLong());
        assertEquals("Loan", node.get("description").asText());
    }
}
