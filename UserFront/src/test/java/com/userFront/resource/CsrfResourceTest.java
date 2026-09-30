package com.userFront.resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.config.SecurityConfig;
import com.userFront.service.UserServiceImpl.UserSecurityService;

@RunWith(SpringRunner.class)
@WebMvcTest(CsrfResource.class)
@Import(SecurityConfig.class)
public class CsrfResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityService userSecurityService;

    /**
     * Only the status is asserted: the XSRF-TOKEN cookie is written once CSRF protection
     * is enabled with a cookie-based token repository, and is absent while it is disabled.
     */
    @Test
    public void csrfEndpointReturnsNoContentForAnonymousClient() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isNoContent());
    }
}
