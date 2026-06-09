package com.userFront.resource;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
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
    public void testUserList() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        when(userService.findUserList()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    public void testGetPrimaryTransactionList() throws Exception {
        PrimaryAccount pa = new PrimaryAccount();
        PrimaryTransaction tx = new PrimaryTransaction(new Date(), "Deposit", "Account", "Finished",
                100.0, new BigDecimal("1100.00"), pa);

        when(transactionService.findPrimaryTransactionList("testuser"))
                .thenReturn(Arrays.asList(tx));

        mockMvc.perform(get("/api/user/primary/transaction").param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Deposit"))
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    public void testGetSavingsTransactionList() throws Exception {
        SavingsAccount sa = new SavingsAccount();
        SavingsTransaction tx = new SavingsTransaction(new Date(), "Withdrawal", "Account", "Finished",
                200.0, new BigDecimal("1800.00"), sa);

        when(transactionService.findSavingsTransactionList("testuser"))
                .thenReturn(Arrays.asList(tx));

        mockMvc.perform(get("/api/user/savings/transaction").param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Withdrawal"))
                .andExpect(jsonPath("$[0].amount").value(200.0));
    }

    @Test
    public void testEnableUser() throws Exception {
        mockMvc.perform(get("/api/user/testuser/enable"))
                .andExpect(status().isOk());

        verify(userService).enableUser("testuser");
    }

    @Test
    public void testDisableUser() throws Exception {
        mockMvc.perform(get("/api/user/testuser/disable"))
                .andExpect(status().isOk());

        verify(userService).disableUser("testuser");
    }
}
