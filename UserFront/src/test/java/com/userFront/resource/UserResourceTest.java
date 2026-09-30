package com.userFront.resource;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserResource userResource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    @Test
    public void postEnableEnablesUser() throws Exception {
        mockMvc.perform(post("/api/user/alice/enable")).andExpect(status().isOk());
        verify(userService).enableUser("alice");
    }

    @Test
    public void postDisableDisablesUser() throws Exception {
        mockMvc.perform(post("/api/user/alice/disable")).andExpect(status().isOk());
        verify(userService).disableUser("alice");
    }

    @Test
    public void getEnableIsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/user/alice/enable")).andExpect(status().isMethodNotAllowed());
        verify(userService, never()).enableUser("alice");
    }

    @Test
    public void getDisableIsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/user/alice/disable")).andExpect(status().isMethodNotAllowed());
        verify(userService, never()).disableUser("alice");
    }
}
