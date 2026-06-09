package com.userFront.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.RecipientDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PrimaryTransactionDao primaryTransactionDao;

    @Mock
    private SavingsTransactionDao savingsTransactionDao;

    @Mock
    private PrimaryAccountDao primaryAccountDao;

    @Mock
    private SavingsAccountDao savingsAccountDao;

    @Mock
    private RecipientDao recipientDao;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private PrimaryAccount primaryAccount;
    private SavingsAccount savingsAccount;

    @Before
    public void setUp() {
        primaryAccount = new PrimaryAccount();
        primaryAccount.setId(1L);
        primaryAccount.setAccountBalance(new BigDecimal("1000.00"));
        primaryAccount.setPrimaryTransactionList(Collections.emptyList());

        savingsAccount = new SavingsAccount();
        savingsAccount.setId(1L);
        savingsAccount.setAccountBalance(new BigDecimal("2000.00"));
        savingsAccount.setSavingsTransactionList(Collections.emptyList());

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPrimaryAccount(primaryAccount);
        testUser.setSavingsAccount(savingsAccount);
    }

    @Test
    public void testFindPrimaryTransactionList() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        List<PrimaryTransaction> result = transactionService.findPrimaryTransactionList("testuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService).findByUsername("testuser");
    }

    @Test
    public void testFindSavingsTransactionList() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        List<SavingsTransaction> result = transactionService.findSavingsTransactionList("testuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService).findByUsername("testuser");
    }

    @Test
    public void testSavePrimaryDepositTransaction() {
        PrimaryTransaction tx = new PrimaryTransaction();
        transactionService.savePrimaryDepositTransaction(tx);
        verify(primaryTransactionDao).save(tx);
    }

    @Test
    public void testSaveSavingsDepositTransaction() {
        SavingsTransaction tx = new SavingsTransaction();
        transactionService.saveSavingsDepositTransaction(tx);
        verify(savingsTransactionDao).save(tx);
    }

    @Test
    public void testSavePrimaryWithdrawTransaction() {
        PrimaryTransaction tx = new PrimaryTransaction();
        transactionService.savePrimaryWithdrawTransaction(tx);
        verify(primaryTransactionDao).save(tx);
    }

    @Test
    public void testSaveSavingsWithdrawTransaction() {
        SavingsTransaction tx = new SavingsTransaction();
        transactionService.saveSavingsWithdrawTransaction(tx);
        verify(savingsTransactionDao).save(tx);
    }

    @Test
    public void testBetweenAccountsTransferPrimaryToSavings() throws Exception {
        transactionService.betweenAccountsTransfer("Primary", "Savings", "500",
                primaryAccount, savingsAccount);

        assertEquals(0, new BigDecimal("500.00").compareTo(primaryAccount.getAccountBalance()));
        assertEquals(0, new BigDecimal("2500.00").compareTo(savingsAccount.getAccountBalance()));
        verify(primaryAccountDao).save(primaryAccount);
        verify(savingsAccountDao).save(savingsAccount);
        verify(primaryTransactionDao).save(any(PrimaryTransaction.class));
    }

    @Test
    public void testBetweenAccountsTransferSavingsToPrimary() throws Exception {
        transactionService.betweenAccountsTransfer("Savings", "Primary", "300",
                primaryAccount, savingsAccount);

        assertEquals(0, new BigDecimal("1300.00").compareTo(primaryAccount.getAccountBalance()));
        assertEquals(0, new BigDecimal("1700.00").compareTo(savingsAccount.getAccountBalance()));
        verify(primaryAccountDao).save(primaryAccount);
        verify(savingsAccountDao).save(savingsAccount);
        verify(savingsTransactionDao).save(any(SavingsTransaction.class));
    }

    @Test(expected = Exception.class)
    public void testBetweenAccountsTransferInvalid() throws Exception {
        transactionService.betweenAccountsTransfer("Primary", "Primary", "100",
                primaryAccount, savingsAccount);
    }

    @Test
    public void testFindRecipientList() {
        Recipient r1 = new Recipient();
        r1.setName("Recipient1");
        r1.setUser(testUser);

        User otherUser = new User();
        otherUser.setUsername("otheruser");
        Recipient r2 = new Recipient();
        r2.setName("Recipient2");
        r2.setUser(otherUser);

        when(recipientDao.findAll()).thenReturn(Arrays.asList(r1, r2));

        Principal principal = () -> "testuser";
        List<Recipient> result = transactionService.findRecipientList(principal);

        assertEquals(1, result.size());
        assertEquals("Recipient1", result.get(0).getName());
    }

    @Test
    public void testSaveRecipient() {
        Recipient recipient = new Recipient();
        recipient.setName("TestRecipient");
        when(recipientDao.save(recipient)).thenReturn(recipient);

        Recipient result = transactionService.saveRecipient(recipient);

        assertEquals("TestRecipient", result.getName());
        verify(recipientDao).save(recipient);
    }

    @Test
    public void testFindRecipientByName() {
        Recipient recipient = new Recipient();
        recipient.setName("John");
        when(recipientDao.findByName("John")).thenReturn(recipient);

        Recipient result = transactionService.findRecipientByName("John");

        assertEquals("John", result.getName());
    }

    @Test
    public void testDeleteRecipientByName() {
        transactionService.deleteRecipientByName("John");
        verify(recipientDao).deleteByName("John");
    }

    @Test
    public void testToSomeoneElseTransferFromPrimary() {
        Recipient recipient = new Recipient();
        recipient.setName("Bob");

        transactionService.toSomeoneElseTransfer(recipient, "Primary", "200",
                primaryAccount, savingsAccount);

        assertEquals(0, new BigDecimal("800.00").compareTo(primaryAccount.getAccountBalance()));
        verify(primaryAccountDao).save(primaryAccount);
        verify(primaryTransactionDao).save(any(PrimaryTransaction.class));
    }

    @Test
    public void testToSomeoneElseTransferFromSavings() {
        Recipient recipient = new Recipient();
        recipient.setName("Bob");

        transactionService.toSomeoneElseTransfer(recipient, "Savings", "300",
                primaryAccount, savingsAccount);

        assertEquals(0, new BigDecimal("1700.00").compareTo(savingsAccount.getAccountBalance()));
        verify(savingsAccountDao).save(savingsAccount);
        verify(savingsTransactionDao).save(any(SavingsTransaction.class));
    }
}
