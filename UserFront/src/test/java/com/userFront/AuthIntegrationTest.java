package com.userFront;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractBankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        createUser("loginUser", "login@example.com", "secret");
    }

    @Test
    public void loginPageIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/index")).andExpect(status().isOk());
        mockMvc.perform(get("/signup")).andExpect(status().isOk());
    }

    @Test
    public void protectedPageRedirectsAnonymousToLogin() throws Exception {
        mockMvc.perform(get("/userFront"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/index"));
    }

    @Test
    public void formLoginSucceedsWithValidCredentials() throws Exception {
        mockMvc.perform(formLogin("/index").user("loginUser").password("secret"))
                .andExpect(authenticated().withUsername("loginUser"));
    }

    @Test
    public void formLoginFailsWithInvalidCredentials() throws Exception {
        mockMvc.perform(formLogin("/index").user("loginUser").password("wrong"))
                .andExpect(unauthenticated());
    }

    @Test
    public void adminApiForbiddenForRegularUser() throws Exception {
        mockMvc.perform(get("/api/user/all").with(user("loginUser").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void adminApiAllowedForAdminUser() throws Exception {
        mockMvc.perform(get("/api/user/all").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
