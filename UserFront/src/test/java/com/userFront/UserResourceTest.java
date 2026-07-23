package com.userFront;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserResourceTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockBean
    private UserService userService;

    @MockBean
    private TransactionService transactionService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    private static MockHttpSession sessionWithRole(String role) {
        List<GrantedAuthority> authorities = Collections
                .<GrantedAuthority>singletonList(new SimpleGrantedAuthority(role));
        Authentication authentication = new UsernamePasswordAuthenticationToken("tester", "n/a", authorities);
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return session;
    }

    @Test
    public void userListAllowsAdmin() throws Exception {
        User user = new User();
        user.setUsername("john");
        when(userService.findUserList()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/user/all").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));

        verify(userService, times(1)).findUserList();
    }

    @Test
    public void userListForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/user/all").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void primaryTransactionListAllowsAdmin() throws Exception {
        when(transactionService.findPrimaryTransactionList("john"))
                .thenReturn(Collections.<PrimaryTransaction>emptyList());

        mockMvc.perform(get("/api/user/primary/transaction")
                .param("username", "john")
                .session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(transactionService, times(1)).findPrimaryTransactionList("john");
    }

    @Test
    public void primaryTransactionListForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/user/primary/transaction")
                .param("username", "john")
                .session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void savingsTransactionListAllowsAdmin() throws Exception {
        when(transactionService.findSavingsTransactionList("john"))
                .thenReturn(Collections.<SavingsTransaction>emptyList());

        mockMvc.perform(get("/api/user/savings/transaction")
                .param("username", "john")
                .session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(transactionService, times(1)).findSavingsTransactionList("john");
    }

    @Test
    public void savingsTransactionListForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/user/savings/transaction")
                .param("username", "john")
                .session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void enableUserAllowsAdmin() throws Exception {
        mockMvc.perform(get("/api/user/john/enable").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk());

        verify(userService, times(1)).enableUser("john");
    }

    @Test
    public void enableUserForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/user/john/enable").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());

        verify(userService, times(0)).enableUser("john");
    }

    @Test
    public void disableUserAllowsAdmin() throws Exception {
        mockMvc.perform(get("/api/user/john/disable").session(sessionWithRole("ROLE_ADMIN")))
                .andExpect(status().isOk());

        verify(userService, times(1)).disableUser("john");
    }

    @Test
    public void disableUserForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/user/john/disable").session(sessionWithRole("ROLE_USER")))
                .andExpect(status().isForbidden());

        verify(userService, times(0)).disableUser("john");
    }
}
