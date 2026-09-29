package com.userFront.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.UserServiceImpl;

public class UserServiceImplDisableTest {

    private UserDao userDao;
    private SessionRegistryImpl sessionRegistry;
    private UserServiceImpl userService;

    @Before
    public void setUp() {
        userDao = mock(UserDao.class);
        sessionRegistry = new SessionRegistryImpl();
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "userDao", userDao);
        ReflectionTestUtils.setField(userService, "sessionRegistry", sessionRegistry);
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }

    @Test
    public void disableUserExpiresOnlyThatUsersSessions() {
        User stored = user("alice");
        when(userDao.findByUsername("alice")).thenReturn(stored);
        sessionRegistry.registerNewSession("alice-1", user("alice"));
        sessionRegistry.registerNewSession("alice-2", user("alice"));
        sessionRegistry.registerNewSession("bob-1", user("bob"));

        userService.disableUser("alice");

        assertFalse(stored.isEnabled());
        verify(userDao).save(stored);
        assertTrue(sessionRegistry.getSessionInformation("alice-1").isExpired());
        assertTrue(sessionRegistry.getSessionInformation("alice-2").isExpired());
        assertFalse(sessionRegistry.getSessionInformation("bob-1").isExpired());
    }
}
