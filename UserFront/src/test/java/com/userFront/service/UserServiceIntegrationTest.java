package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.userFront.AbstractBankingIntegrationTest;
import com.userFront.domain.User;

public class UserServiceIntegrationTest extends AbstractBankingIntegrationTest {

    @Test
    public void createUserPersistsEncodedPasswordAndAccounts() {
        User user = createUser("alice", "alice@example.com", "secret");

        assertNotNull(user.getUserId());
        assertNotNull("primary account should be created", user.getPrimaryAccount());
        assertNotNull("savings account should be created", user.getSavingsAccount());
        assertFalse("stored password must be hashed", "secret".equals(user.getPassword()));
        assertTrue("hash must verify against raw password",
                passwordEncoder.matches("secret", user.getPassword()));
    }

    @Test
    public void checkUsernameAndEmailExistence() {
        createUser("bob", "bob@example.com", "secret");

        assertTrue(userService.checkUsernameExists("bob"));
        assertFalse(userService.checkUsernameExists("nobody"));
        assertTrue(userService.checkEmailExists("bob@example.com"));
        assertFalse(userService.checkEmailExists("nobody@example.com"));
    }

    @Test
    public void enableAndDisableUserTogglesFlag() {
        createUser("carol", "carol@example.com", "secret");

        userService.disableUser("carol");
        assertFalse(userService.findByUsername("carol").isEnabled());

        userService.enableUser("carol");
        assertTrue(userService.findByUsername("carol").isEnabled());
    }

    @Test
    public void findUserListReturnsCreatedUsers() {
        int before = userService.findUserList().size();

        createUser("dave", "dave@example.com", "secret");

        assertEquals(before + 1, userService.findUserList().size());
        assertNotNull(userService.findByUsername("dave"));
    }
}
