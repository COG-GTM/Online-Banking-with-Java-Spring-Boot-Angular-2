package com.userFront.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
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
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class TransferControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;
    private User testUser;
    private PrimaryAccount primaryAccount;
    private SavingsAccount savingsAccount;

    @Before
    public void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(transferController)
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
    public void testBetweenAccountsGet() throws Exception {
        mockMvc.perform(get("/transfer/betweenAccounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("betweenAccounts"))
                .andExpect(model().attributeExists("transferFrom"))
                .andExpect(model().attributeExists("transferTo"))
                .andExpect(model().attributeExists("amount"));
    }

    @Test
    public void testBetweenAccountsPost() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        Principal principal = () -> "testuser";

        mockMvc.perform(post("/transfer/betweenAccounts")
                .param("transferFrom", "Primary")
                .param("transferTo", "Savings")
                .param("amount", "500")
                .principal(principal))
                .andExpect(status().is3xxRedirection());

        verify(transactionService).betweenAccountsTransfer("Primary", "Savings", "500",
                primaryAccount, savingsAccount);
    }

    @Test
    public void testRecipientGet() throws Exception {
        Principal principal = () -> "testuser";
        when(transactionService.findRecipientList(any(Principal.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transfer/recipient").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("recipient"))
                .andExpect(model().attributeExists("recipientList"))
                .andExpect(model().attributeExists("recipient"));
    }

    @Test
    public void testRecipientSavePost() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        Principal principal = () -> "testuser";

        mockMvc.perform(post("/transfer/recipient/save")
                .param("name", "Bob")
                .param("email", "bob@example.com")
                .param("phone", "5551234")
                .param("accountNumber", "12345")
                .principal(principal))
                .andExpect(status().is3xxRedirection());

        verify(transactionService).saveRecipient(any(Recipient.class));
    }

    @Test
    public void testRecipientEdit() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setName("Bob");
        when(transactionService.findRecipientByName("Bob")).thenReturn(recipient);

        Principal principal = () -> "testuser";
        when(transactionService.findRecipientList(any(Principal.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transfer/recipient/edit")
                .param("recipientName", "Bob")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("recipient"))
                .andExpect(model().attributeExists("recipient"))
                .andExpect(model().attributeExists("recipientList"));
    }

    @Test
    public void testRecipientDelete() throws Exception {
        Principal principal = () -> "testuser";
        when(transactionService.findRecipientList(any(Principal.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transfer/recipient/delete")
                .param("recipientName", "Bob")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("recipient"))
                .andExpect(model().attributeExists("recipient"))
                .andExpect(model().attributeExists("recipientList"));

        verify(transactionService).deleteRecipientByName("Bob");
    }

    @Test
    public void testToSomeoneElseGet() throws Exception {
        Principal principal = () -> "testuser";
        when(transactionService.findRecipientList(any(Principal.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transfer/toSomeoneElse").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("toSomeoneElse"))
                .andExpect(model().attributeExists("recipientList"))
                .andExpect(model().attributeExists("accountType"));
    }

    @Test
    public void testToSomeoneElsePost() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        Recipient recipient = new Recipient();
        recipient.setName("Bob");
        when(transactionService.findRecipientByName("Bob")).thenReturn(recipient);

        Principal principal = () -> "testuser";

        mockMvc.perform(post("/transfer/toSomeoneElse")
                .param("recipientName", "Bob")
                .param("accountType", "Primary")
                .param("amount", "200")
                .principal(principal))
                .andExpect(status().is3xxRedirection());

        verify(transactionService).toSomeoneElseTransfer(recipient, "Primary", "200",
                primaryAccount, savingsAccount);
    }
}
