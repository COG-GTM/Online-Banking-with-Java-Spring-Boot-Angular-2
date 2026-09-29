package com.userFront.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.userFront.dao.UserDao;
import com.userFront.resource.AppointmentResource;
import com.userFront.resource.UserResource;
import com.userFront.service.AppointmentService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@SpringJUnitWebConfig(classes = { SecurityConfig.class, SecurityConfigTest.TestConfig.class })
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean(FilterChainProxy.class))
                .build();
    }

    @Test
    void publicPathsAreAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/signup")).andExpect(status().isOk());
        mockMvc.perform(get("/css/main.css")).andExpect(status().isOk());
        mockMvc.perform(get("/error/404")).andExpect(status().isOk());
    }

    @Test
    void protectedPageRedirectsAnonymousUserToLoginPage() throws Exception {
        mockMvc.perform(get("/userFront"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    void adminApiIsForbiddenForUserRole() throws Exception {
        mockMvc.perform(get("/api/user/all").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/appointment/all").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminApiIsAllowedForAdminRole() throws Exception {
        mockMvc.perform(get("/api/user/all").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/appointment/all").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void formLoginUsesUserSecurityServiceAndPasswordEncoder() throws Exception {
        when(userSecurityService.loadUserByUsername("alice")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("alice")
                        .password(passwordEncoder.encode("secret"))
                        .roles("USER")
                        .build());

        mockMvc.perform(post("/index").param("username", "alice").param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userFront"));
    }

    @Test
    void formLoginFailureRedirectsToErrorPage() throws Exception {
        when(userSecurityService.loadUserByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("not found"));

        mockMvc.perform(post("/index").param("username", "nobody").param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index?error"));
    }

    @Test
    void logoutWorksWithGet() throws Exception {
        mockMvc.perform(get("/logout").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index?logout"));
    }

    private static MockHttpSession sessionWithRole(String role) {
        SecurityContext securityContext = new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated(
                "user", null, List.of(new SimpleGrantedAuthority(role))));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return session;
    }

    @Configuration
    @EnableWebMvc
    @Import({ UserResource.class, AppointmentResource.class, StubPageController.class })
    static class TestConfig {

        @Bean
        UserDao userDao() {
            return mock(UserDao.class);
        }

        @Bean
        UserSecurityService userSecurityService() {
            return mock(UserSecurityService.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        TransactionService transactionService() {
            return mock(TransactionService.class);
        }

        @Bean
        AppointmentService appointmentService() {
            return mock(AppointmentService.class);
        }
    }

    @RestController
    static class StubPageController {

        @GetMapping({ "/", "/signup", "/css/main.css", "/error/404", "/userFront", "/index" })
        String page() {
            return "ok";
        }
    }
}
