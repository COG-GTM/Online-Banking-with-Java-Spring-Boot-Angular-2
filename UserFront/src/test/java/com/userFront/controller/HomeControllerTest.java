package com.userFront.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.dao.RoleDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;
    private User testUser;

    @Before
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setViewResolvers(viewResolver).build();

        PrimaryAccount primaryAccount = new PrimaryAccount();
        primaryAccount.setAccountBalance(new BigDecimal("1000.00"));

        SavingsAccount savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("2000.00"));

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPrimaryAccount(primaryAccount);
        testUser.setSavingsAccount(savingsAccount);
    }

    @Test
    public void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testSignupGet() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void testSignupPostNewUser() throws Exception {
        when(userService.checkUserExists(anyString(), anyString())).thenReturn(false);

        Role role = new Role();
        role.setName("ROLE_USER");
        when(roleDao.findByName("ROLE_USER")).thenReturn(role);
        when(userService.createUser(any(User.class), anySet())).thenReturn(testUser);

        mockMvc.perform(post("/signup")
                .param("username", "newuser")
                .param("email", "new@example.com")
                .param("password", "pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testSignupPostUsernameExists() throws Exception {
        when(userService.checkUserExists(anyString(), anyString())).thenReturn(true);
        when(userService.checkUsernameExists(anyString())).thenReturn(true);
        when(userService.checkEmailExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/signup")
                .param("username", "testuser")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("usernameExists", true));
    }

    @Test
    public void testSignupPostEmailExists() throws Exception {
        when(userService.checkUserExists(anyString(), anyString())).thenReturn(true);
        when(userService.checkUsernameExists(anyString())).thenReturn(false);
        when(userService.checkEmailExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/signup")
                .param("username", "newuser")
                .param("email", "existing@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("emailExists", true));
    }

    @Test
    public void testUserFront() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        Principal principal = () -> "testuser";

        mockMvc.perform(get("/userFront").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("userFront"))
                .andExpect(model().attributeExists("primaryAccount"))
                .andExpect(model().attributeExists("savingsAccount"));
    }
}
