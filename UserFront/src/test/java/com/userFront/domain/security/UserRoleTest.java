package com.userFront.domain.security;

import static org.junit.Assert.*;

import org.junit.Test;

import com.userFront.domain.User;

public class UserRoleTest {

    @Test
    public void testParameterizedConstructor() {
        User user = new User();
        user.setUsername("testuser");

        Role role = new Role();
        role.setName("ROLE_USER");

        UserRole userRole = new UserRole(user, role);

        assertEquals(user, userRole.getUser());
        assertEquals(role, userRole.getRole());
    }

    @Test
    public void testDefaultConstructor() {
        UserRole userRole = new UserRole();
        assertNull(userRole.getUser());
        assertNull(userRole.getRole());
    }

    @Test
    public void testSetters() {
        UserRole userRole = new UserRole();

        User user = new User();
        user.setUsername("admin");
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        userRole.setUserRoleId(1L);
        userRole.setUser(user);
        userRole.setRole(role);

        assertEquals(1L, userRole.getUserRoleId());
        assertEquals(user, userRole.getUser());
        assertEquals(role, userRole.getRole());
    }
}
