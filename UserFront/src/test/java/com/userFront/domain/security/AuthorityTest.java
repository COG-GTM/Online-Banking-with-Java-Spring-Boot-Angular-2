package com.userFront.domain.security;

import static org.junit.Assert.*;

import org.junit.Test;

public class AuthorityTest {

    @Test
    public void testGetAuthority() {
        Authority authority = new Authority("ROLE_USER");
        assertEquals("ROLE_USER", authority.getAuthority());
    }

    @Test
    public void testGetAuthorityAdmin() {
        Authority authority = new Authority("ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", authority.getAuthority());
    }
}
