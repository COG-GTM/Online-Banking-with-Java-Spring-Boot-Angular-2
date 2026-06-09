package com.userFront.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserServiceImpl.UserServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private AccountService accountService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");
    }

    @Test
    public void testSave() {
        userServiceImpl.save(testUser);
        verify(userDao).save(testUser);
    }

    @Test
    public void testFindByUsername() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        User result = userServiceImpl.findByUsername("testuser");

        assertEquals("testuser", result.getUsername());
        verify(userDao).findByUsername("testuser");
    }

    @Test
    public void testFindByEmail() {
        when(userDao.findByEmail("test@example.com")).thenReturn(testUser);

        User result = userServiceImpl.findByEmail("test@example.com");

        assertEquals("test@example.com", result.getEmail());
        verify(userDao).findByEmail("test@example.com");
    }

    @Test
    public void testCreateUserNewUser() {
        when(userDao.findByUsername("testuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(accountService.createPrimaryAccount()).thenReturn(new PrimaryAccount());
        when(accountService.createSavingsAccount()).thenReturn(new SavingsAccount());
        when(userDao.save(any(User.class))).thenReturn(testUser);

        Role role = new Role();
        role.setName("ROLE_USER");
        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(testUser, role));

        User result = userServiceImpl.createUser(testUser, userRoles);

        assertNotNull(result);
        verify(passwordEncoder).encode("password123");
        verify(roleDao).save(role);
        verify(accountService).createPrimaryAccount();
        verify(accountService).createSavingsAccount();
        verify(userDao).save(testUser);
    }

    @Test
    public void testCreateUserExistingUser() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        Set<UserRole> userRoles = new HashSet<>();
        User result = userServiceImpl.createUser(testUser, userRoles);

        assertEquals(testUser, result);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    public void testCheckUserExistsTrue() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        assertTrue(userServiceImpl.checkUserExists("testuser", "test@example.com"));
    }

    @Test
    public void testCheckUserExistsFalse() {
        when(userDao.findByUsername("newuser")).thenReturn(null);
        when(userDao.findByEmail("newuser")).thenReturn(null);

        assertFalse(userServiceImpl.checkUserExists("newuser", "new@example.com"));
    }

    @Test
    public void testCheckUsernameExistsTrue() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        assertTrue(userServiceImpl.checkUsernameExists("testuser"));
    }

    @Test
    public void testCheckUsernameExistsFalse() {
        when(userDao.findByUsername("unknown")).thenReturn(null);

        assertFalse(userServiceImpl.checkUsernameExists("unknown"));
    }

    @Test
    public void testCheckEmailExistsTrue() {
        when(userDao.findByEmail("test@example.com")).thenReturn(testUser);

        assertTrue(userServiceImpl.checkEmailExists("test@example.com"));
    }

    @Test
    public void testCheckEmailExistsFalse() {
        when(userDao.findByEmail("unknown@example.com")).thenReturn(null);

        assertFalse(userServiceImpl.checkEmailExists("unknown@example.com"));
    }

    @Test
    public void testSaveUser() {
        when(userDao.save(testUser)).thenReturn(testUser);

        User result = userServiceImpl.saveUser(testUser);

        assertEquals(testUser, result);
        verify(userDao).save(testUser);
    }

    @Test
    public void testEnableUser() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        userServiceImpl.enableUser("testuser");

        assertTrue(testUser.isEnabled());
        verify(userDao).save(testUser);
    }

    @Test
    public void testDisableUser() {
        when(userDao.findByUsername("testuser")).thenReturn(testUser);

        userServiceImpl.disableUser("testuser");

        assertFalse(testUser.isEnabled());
        verify(userDao).save(testUser);
    }

    @Test
    public void testFindUserList() {
        List<User> users = Arrays.asList(testUser, new User());
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userServiceImpl.findUserList();

        assertEquals(2, result.size());
        verify(userDao).findAll();
    }
}
