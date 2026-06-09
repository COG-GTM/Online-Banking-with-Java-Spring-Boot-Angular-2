package com.userFront.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class RecipientTest {

    @Test
    public void testGettersAndSetters() {
        Recipient recipient = new Recipient();
        User user = new User();

        recipient.setId(1L);
        recipient.setName("Bob Smith");
        recipient.setEmail("bob@example.com");
        recipient.setPhone("5551234567");
        recipient.setAccountNumber("123456789");
        recipient.setDescription("Business partner");
        recipient.setUser(user);

        assertEquals(Long.valueOf(1L), recipient.getId());
        assertEquals("Bob Smith", recipient.getName());
        assertEquals("bob@example.com", recipient.getEmail());
        assertEquals("5551234567", recipient.getPhone());
        assertEquals("123456789", recipient.getAccountNumber());
        assertEquals("Business partner", recipient.getDescription());
        assertEquals(user, recipient.getUser());
    }
}
