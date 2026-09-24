package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;

/**
 * Regression contract for signup, form login, logout and remember-me as they behave on
 * Boot 1.5.4. Later migration waves re-run this class unchanged.
 */
@Transactional
public class AuthSmokeTest extends AbstractIntegrationTest {

    private static final String USERNAME = "smokeuser";
    private static final String PASSWORD = "smokepass";
    private static final String EMAIL = "smokeuser@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void seedRole() {
        if (roleDao.findByName("ROLE_USER") == null) {
            Role role = new Role();
            role.setRoleId(1);
            role.setName("ROLE_USER");
            roleDao.save(role);
        }
    }

    @Test
    public void signupCreatesUserWithHashedPasswordRoleAndBothAccounts() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", USERNAME)
                .param("password", PASSWORD)
                .param("firstName", "Smoke")
                .param("lastName", "User")
                .param("email", EMAIL)
                .param("phone", "5551234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        entityManager.flush();
        entityManager.clear();

        User user = userDao.findByUsername(USERNAME);
        assertNotNull(user);
        assertEquals(EMAIL, user.getEmail());
        assertFalse(PASSWORD.equals(user.getPassword()));
        assertTrue(user.getPassword().startsWith("$2a$"));
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));

        assertEquals(1, user.getUserRoles().size());
        UserRole userRole = user.getUserRoles().iterator().next();
        assertEquals("ROLE_USER", userRole.getRole().getName());

        assertNotNull(user.getPrimaryAccount());
        assertNotNull(user.getSavingsAccount());
    }

    @Test
    public void loginWithValidCredentialsRedirectsToUserFront() throws Exception {
        signup();

        mockMvc.perform(post("/index")
                .param("username", USERNAME)
                .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userFront"));
    }

    @Test
    public void loginWithBadPasswordRedirectsToIndexError() throws Exception {
        signup();

        mockMvc.perform(post("/index")
                .param("username", USERNAME)
                .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index?error"));
    }

    @Test
    public void logoutIsAGetAndDeletesRememberMeCookie() throws Exception {
        signup();

        MvcResult login = mockMvc.perform(post("/index")
                .param("username", USERNAME)
                .param("password", PASSWORD)
                .param("remember-me", "on"))
                .andExpect(redirectedUrl("/userFront"))
                .andReturn();

        Cookie issued = login.getResponse().getCookie("remember-me");
        assertNotNull(issued);

        // GET, not POST: W2 turns CSRF on and the logoutRequestMatcher must keep this working.
        MvcResult logout = mockMvc.perform(get("/logout")
                .cookie(issued)
                .session((MockHttpSession) login.getRequest().getSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index?logout"))
                .andReturn();

        Cookie rememberMe = logout.getResponse().getCookie("remember-me");
        assertNotNull(rememberMe);
        assertEquals(0, rememberMe.getMaxAge());

        // Today's behaviour: remember-me is token-based, so a copy of the cookie kept by the
        // client still authenticates after logout. Captured, not endorsed.
        mockMvc.perform(get("/userFront").cookie(issued).session(new MockHttpSession()))
                .andExpect(status().isOk());
    }

    @Test
    public void rememberMeCookieReauthenticatesOnAFreshSession() throws Exception {
        signup();

        MvcResult login = mockMvc.perform(post("/index")
                .param("username", USERNAME)
                .param("password", PASSWORD)
                .param("remember-me", "on"))
                .andExpect(redirectedUrl("/userFront"))
                .andReturn();

        Cookie rememberMe = login.getResponse().getCookie("remember-me");
        assertNotNull(rememberMe);
        assertTrue(rememberMe.getMaxAge() > 0);

        mockMvc.perform(get("/userFront").cookie(rememberMe).session(new MockHttpSession()))
                .andExpect(status().isOk());
    }

    private void signup() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", USERNAME)
                .param("password", PASSWORD)
                .param("firstName", "Smoke")
                .param("lastName", "User")
                .param("email", EMAIL)
                .param("phone", "5551234567"))
                .andExpect(redirectedUrl("/"));
    }
}
