package com.userFront.domain.security;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RoleTest {

    @Test
    public void testGettersAndSetters() {
        Role role = new Role();
        role.setRoleId(1);
        role.setName("ROLE_USER");

        assertEquals(1, role.getRoleId());
        assertEquals("ROLE_USER", role.getName());
    }

    @Test
    public void testUserRoles() {
        Role role = new Role();
        Set<UserRole> userRoles = new HashSet<>();
        role.setUserRoles(userRoles);

        assertNotNull(role.getUserRoles());
        assertTrue(role.getUserRoles().isEmpty());
    }
}
