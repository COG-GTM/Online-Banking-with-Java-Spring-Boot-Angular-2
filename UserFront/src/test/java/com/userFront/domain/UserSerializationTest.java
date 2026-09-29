package com.userFront.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserSerializationTest {

    private static final String HASH = "$2a$12$abcdefghijklmnopqrstuuHASHVALUEHASHVALUEHASHVALUEHASHVA";

    private final ObjectMapper mapper = new ObjectMapper();

    private static User user() {
        User user = new User();
        user.setUserId(7L);
        user.setUsername("alice");
        user.setPassword(HASH);
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmail("alice@example.com");
        user.setPhone("555-0100");
        return user;
    }

    @Test
    public void userJsonOmitsPassword() throws Exception {
        String json = mapper.writeValueAsString(user());

        assertFalse(mapper.readTree(json).has("password"));
        assertFalse(json.contains(HASH));
        assertEquals("alice", mapper.readTree(json).get("username").asText());
    }

    @Test
    public void appointmentJsonOmitsUserPassword() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDate(new Date(0));
        appointment.setUser(user());

        JsonNode user = mapper.readTree(mapper.writeValueAsString(appointment)).get("user");

        assertFalse(user.has("password"));
        assertEquals("alice", user.get("username").asText());
    }

    @Test
    public void userToStringOmitsPassword() {
        String text = user().toString();

        assertFalse(text.contains(HASH));
        assertFalse(text.contains("password"));
        assertTrue(text.contains("alice"));
    }
}
