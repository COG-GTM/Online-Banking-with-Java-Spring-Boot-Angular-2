package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.userFront.dao.UserDao;
import com.userFront.domain.User;

public class EnabledUserFilterTest {

    private UserDao userDao;
    private EnabledUserFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    private MockHttpSession session;

    @Before
    public void setUp() {
        userDao = mock(UserDao.class);
        filter = new EnabledUserFilter(userDao);
        session = new MockHttpSession();
        request = new MockHttpServletRequest("POST", "/transfer/betweenAccounts");
        request.setSession(session);
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static User user(String username, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setEnabled(enabled);
        return user;
    }

    private static void authenticate(User principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    public void enabledUserContinuesThroughChain() throws Exception {
        authenticate(user("alice", true));
        when(userDao.findByUsername("alice")).thenReturn(user("alice", true));

        filter.doFilter(request, response, chain);

        assertSame(request, chain.getRequest());
        assertTrue(!session.isInvalid());
    }

    @Test
    public void disabledUserIsLoggedOutAndRedirected() throws Exception {
        authenticate(user("alice", true));
        when(userDao.findByUsername("alice")).thenReturn(user("alice", false));

        filter.doFilter(request, response, chain);

        assertNull(chain.getRequest());
        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(EnabledUserFilter.DISABLED_URL, response.getRedirectedUrl());
        assertEquals(0, response.getCookie("remember-me").getMaxAge());
    }

    @Test
    public void deletedUserIsLoggedOut() throws Exception {
        authenticate(user("alice", true));
        when(userDao.findByUsername("alice")).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertNull(chain.getRequest());
        assertTrue(session.isInvalid());
    }

    @Test
    public void anonymousRequestIsNotChecked() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymousUser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        filter.doFilter(request, response, chain);

        assertSame(request, chain.getRequest());
        verifyZeroInteractions(userDao);
    }
}
