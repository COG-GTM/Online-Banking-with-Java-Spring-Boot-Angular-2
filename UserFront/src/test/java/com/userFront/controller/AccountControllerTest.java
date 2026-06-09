package com.userFront.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.service.AccountService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private User testUser;
    private PrimaryAccount primaryAccount;
    private SavingsAccount savingsAccount;

    @Before
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setViewResolvers(viewResolver).build();

        primaryAccount = new PrimaryAccount();
        primaryAccount.setAccountBalance(new BigDecimal("1000.00"));

        savingsAccount = new SavingsAccount();
        savingsAccount.setAccountBalance(new BigDecimal("2000.00"));

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPrimaryAccount(primaryAccount);
        testUser.setSavingsAccount(savingsAccount);
    }

    @Test
    public void testPrimaryAccount() throws Exception {
        when(transactionService.findPrimaryTransactionList("testuser")).thenReturn(Collections.emptyList());
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        Principal principal = () -> "testuser";

        mockMvc.perform(get("/account/primaryAccount").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("primaryAccount"))
                .andExpect(model().attributeExists("primaryAccount"))
                .andExpect(model().attributeExists("primaryTransactionList"));
    }

    @Test
    public void testSavingsAccount() throws Exception {
        when(transactionService.findSavingsTransactionList("testuser")).thenReturn(Collections.emptyList());
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        Principal principal = () -> "testuser";

        mockMvc.perform(get("/account/savingsAccount").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("savingsAccount"))
                .andExpect(model().attributeExists("savingsAccount"))
                .andExpect(model().attributeExists("savingsTransactionList"));
    }

    @Test
    public void testDepositGet() throws Exception {
        mockMvc.perform(get("/account/deposit"))
                .andExpect(status().isOk())
                .andExpect(view().name("deposit"))
                .andExpect(model().attributeExists("accountType"))
                .andExpect(model().attributeExists("amount"));
    }

    @Test
    public void testDepositPost() throws Exception {
        Principal principal = () -> "testuser";

        mockMvc.perform(post("/account/deposit")
                .param("amount", "500.0")
                .param("accountType", "Primary")
                .principal(principal))
                .andExpect(status().is3xxRedirection());

        verify(accountService).deposit(eq("Primary"), eq(500.0), any(Principal.class));
    }

    @Test
    public void testWithdrawGet() throws Exception {
        mockMvc.perform(get("/account/withdraw"))
                .andExpect(status().isOk())
                .andExpect(view().name("withdraw"))
                .andExpect(model().attributeExists("accountType"))
                .andExpect(model().attributeExists("amount"));
    }

    @Test
    public void testWithdrawPost() throws Exception {
        Principal principal = () -> "testuser";

        mockMvc.perform(post("/account/withdraw")
                .param("amount", "200.0")
                .param("accountType", "Savings")
                .principal(principal))
                .andExpect(status().is3xxRedirection());

        verify(accountService).withdraw(eq("Savings"), eq(200.0), any(Principal.class));
    }
}
