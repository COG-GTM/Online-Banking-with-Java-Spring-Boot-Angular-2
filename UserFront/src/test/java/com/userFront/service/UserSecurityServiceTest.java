package com.userFront.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@RunWith(MockitoJUnitRunner.class)
public class UserSecurityServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserSecurityService userSecurityService;

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
    }

    @Test
    public void testLoadUserByUsernameSuccess() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        UserDetails result = userSecurityService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userDao).findByUsername("testuser");
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testLoadUserByUsernameNotFound() {
        when(userDao.findByUsername("unknown")).thenReturn(null);

        userSecurityService.loadUserByUsername("unknown");
    }
}
