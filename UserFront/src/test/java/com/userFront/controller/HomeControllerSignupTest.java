package com.userFront.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.service.UserService;

public class HomeControllerSignupTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).setViewResolvers(viewResolver).build();
    }

    @Test
    public void acceptsSafeUsernames() {
        assertTrue(HomeController.isValidUsername("alice"));
        assertTrue(HomeController.isValidUsername("john.doe_99-x"));
        assertTrue(HomeController.isValidUsername("abc"));
        assertTrue(HomeController.isValidUsername("a2345678901234567890123456789012"));
    }

    @Test
    public void rejectsUsernamesThatCouldRewriteUrls() {
        String[] invalid = { null, "", "ab", "alice/disable?x=", "../appointment/7/confirm?x=",
                "mallory2/enable?x=", "bob#frag", "a b c", "a%2Fb", "alice\\x", "..",
                "a23456789012345678901234567890123" };
        for (String username : invalid) {
            assertFalse(String.valueOf(username), HomeController.isValidUsername(username));
        }
    }

    @Test
    public void signupWithPathCharactersIsRejectedWithoutCreatingUser() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "alice/disable?x=")
                .param("email", "mallory@example.com")
                .param("password", "pw"))
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("usernameInvalid", true));

        verify(userService, never()).checkUserExists(anyString(), anyString());
        verify(userService, never()).createUser(any(User.class), any(Set.class));
    }

    @Test
    public void signupWithValidUsernameCreatesUser() throws Exception {
        when(userService.checkUserExists("mallory", "mallory@example.com")).thenReturn(false);

        mockMvc.perform(post("/signup")
                .param("username", "mallory")
                .param("email", "mallory@example.com")
                .param("password", "pw"))
                .andExpect(redirectedUrl("/"));

        verify(userService).createUser(any(User.class), any(Set.class));
    }
}
