package com.userFront.domain;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPhone("1234567890");
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals(Long.valueOf(1L), user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    public void testIsEnabledDefault() {
        User newUser = new User();
        assertTrue(newUser.isEnabled());
    }

    @Test
    public void testSetEnabled() {
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    public void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    public void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    public void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    public void testGetAuthorities() {
        Role role = new Role();
        role.setName("ROLE_USER");

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user, role));
        user.setUserRoles(userRoles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void testGetAuthoritiesMultipleRoles() {
        Role role1 = new Role();
        role1.setName("ROLE_USER");
        Role role2 = new Role();
        role2.setName("ROLE_ADMIN");

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user, role1));
        userRoles.add(new UserRole(user, role2));
        user.setUserRoles(userRoles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(2, authorities.size());
    }

    @Test
    public void testPrimaryAccount() {
        PrimaryAccount pa = new PrimaryAccount();
        user.setPrimaryAccount(pa);
        assertEquals(pa, user.getPrimaryAccount());
    }

    @Test
    public void testSavingsAccount() {
        SavingsAccount sa = new SavingsAccount();
        user.setSavingsAccount(sa);
        assertEquals(sa, user.getSavingsAccount());
    }

    @Test
    public void testToString() {
        String str = user.toString();
        assertTrue(str.contains("testuser"));
        assertTrue(str.contains("test@example.com"));
    }
}
