package com.userFront.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User userWithPassword() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("$2a$12$hashedvalue");
        return user;
    }

    @Test
    public void userJsonOmitsPassword() throws Exception {
        String json = objectMapper.writeValueAsString(userWithPassword());

        assertThat(json).contains("\"username\":\"alice\"");
        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("hashedvalue");
    }

    @Test
    public void appointmentJsonOmitsUserPassword() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setUser(userWithPassword());

        String json = objectMapper.writeValueAsString(appointment);

        assertThat(json).contains("\"username\":\"alice\"");
        assertThat(json).doesNotContain("hashedvalue");
    }

    @Test
    public void userJsonStillAcceptsPasswordOnInput() throws Exception {
        User user = objectMapper.readValue("{\"username\":\"bob\",\"password\":\"secret\"}", User.class);

        assertThat(user.getPassword()).isEqualTo("secret");
    }

    @Test
    public void toStringOmitsPassword() {
        assertThat(userWithPassword().toString()).doesNotContain("hashedvalue");
    }
}
