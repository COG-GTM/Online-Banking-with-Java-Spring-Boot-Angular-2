package com.userFront.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.AccountServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @Mock
    private PrimaryAccountDao primaryAccountDao;

    @Mock
    private SavingsAccountDao savingsAccountDao;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private PrimaryAccount primaryAccount;
    private SavingsAccount savingsAccount;
    private Principal principal;

    @Before
    public void setUp() {
        primaryAccount = new PrimaryAccount();
        primaryAccount.setId(1L);
        primaryAccount.setAccountBalance(new BigDecimal("1000.00"));

        savingsAccount = new SavingsAccount();
        savingsAccount.setId(1L);
        savingsAccount.setAccountBalance(new BigDecimal("2000.00"));

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPrimaryAccount(primaryAccount);
        testUser.setSavingsAccount(savingsAccount);

        principal = () -> "testuser";
    }

    @Test
    public void testCreatePrimaryAccount() {
        PrimaryAccount saved = new PrimaryAccount();
        saved.setAccountBalance(new BigDecimal(0.0));

        when(primaryAccountDao.save(any(PrimaryAccount.class))).thenReturn(saved);
        when(primaryAccountDao.findByAccountNumber(anyInt())).thenReturn(saved);

        PrimaryAccount result = accountService.createPrimaryAccount();

        assertNotNull(result);
        verify(primaryAccountDao).save(any(PrimaryAccount.class));
        verify(primaryAccountDao).findByAccountNumber(anyInt());
    }

    @Test
    public void testCreateSavingsAccount() {
        SavingsAccount saved = new SavingsAccount();
        saved.setAccountBalance(new BigDecimal(0.0));

        when(savingsAccountDao.save(any(SavingsAccount.class))).thenReturn(saved);
        when(savingsAccountDao.findByAccountNumber(anyInt())).thenReturn(saved);

        SavingsAccount result = accountService.createSavingsAccount();

        assertNotNull(result);
        verify(savingsAccountDao).save(any(SavingsAccount.class));
        verify(savingsAccountDao).findByAccountNumber(anyInt());
    }

    @Test
    public void testDepositToPrimaryAccount() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        accountService.deposit("Primary", 500.0, principal);

        assertEquals(0, new BigDecimal("1500.0").compareTo(primaryAccount.getAccountBalance()));
        verify(primaryAccountDao).save(primaryAccount);
        verify(transactionService).savePrimaryDepositTransaction(any(PrimaryTransaction.class));
    }

    @Test
    public void testDepositToSavingsAccount() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        accountService.deposit("Savings", 300.0, principal);

        assertEquals(0, new BigDecimal("2300.0").compareTo(savingsAccount.getAccountBalance()));
        verify(savingsAccountDao).save(savingsAccount);
        verify(transactionService).saveSavingsDepositTransaction(any(SavingsTransaction.class));
    }

    @Test
    public void testWithdrawFromPrimaryAccount() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        accountService.withdraw("Primary", 200.0, principal);

        assertEquals(0, new BigDecimal("800.0").compareTo(primaryAccount.getAccountBalance()));
        verify(primaryAccountDao).save(primaryAccount);
        verify(transactionService).savePrimaryWithdrawTransaction(any(PrimaryTransaction.class));
    }

    @Test
    public void testWithdrawFromSavingsAccount() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        accountService.withdraw("Savings", 500.0, principal);

        assertEquals(0, new BigDecimal("1500.0").compareTo(savingsAccount.getAccountBalance()));
        verify(savingsAccountDao).save(savingsAccount);
        verify(transactionService).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
    }

    @Test
    public void testDepositCaseInsensitive() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        accountService.deposit("primary", 100.0, principal);

        assertEquals(0, new BigDecimal("1100.0").compareTo(primaryAccount.getAccountBalance()));
        verify(primaryAccountDao).save(primaryAccount);
    }

    @Test
    public void testWithdrawCaseInsensitive() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        accountService.withdraw("savings", 100.0, principal);

        assertEquals(0, new BigDecimal("1900.0").compareTo(savingsAccount.getAccountBalance()));
        verify(savingsAccountDao).save(savingsAccount);
    }
}
