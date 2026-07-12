package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.userFront.AbstractBankingIntegrationTest;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;

public class AccountServiceIntegrationTest extends AbstractBankingIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PrimaryTransactionDao primaryTransactionDao;

    @Autowired
    private SavingsTransactionDao savingsTransactionDao;

    private Principal principal;

    @Before
    public void setUp() {
        createUser("accountUser", "account@example.com", "secret");
        principal = () -> "accountUser";
    }

    @Test
    public void depositToPrimaryIncreasesBalanceAndRecordsTransaction() {
        accountService.deposit("Primary", 100.0, principal);

        User user = userService.findByUsername("accountUser");
        assertEquals(0, new BigDecimal("100.0").compareTo(user.getPrimaryAccount().getAccountBalance()));

        List<PrimaryTransaction> transactions = primaryTransactionDao.findAll();
        assertEquals(1, transactions.size());
        assertEquals("Deposit to Primary Account", transactions.get(0).getDescription());
    }

    @Test
    public void depositToSavingsIncreasesBalanceAndRecordsTransaction() {
        accountService.deposit("Savings", 250.0, principal);

        User user = userService.findByUsername("accountUser");
        assertEquals(0, new BigDecimal("250.0").compareTo(user.getSavingsAccount().getAccountBalance()));

        List<SavingsTransaction> transactions = savingsTransactionDao.findAll();
        assertEquals(1, transactions.size());
        assertEquals("Deposit to savings Account", transactions.get(0).getDescription());
    }

    @Test
    public void withdrawFromPrimaryDecreasesBalance() {
        accountService.deposit("Primary", 100.0, principal);
        accountService.withdraw("Primary", 40.0, principal);

        User user = userService.findByUsername("accountUser");
        assertEquals(0, new BigDecimal("60.0").compareTo(user.getPrimaryAccount().getAccountBalance()));
    }

    @Test
    public void withdrawFromSavingsDecreasesBalance() {
        accountService.deposit("Savings", 100.0, principal);
        accountService.withdraw("Savings", 30.0, principal);

        User user = userService.findByUsername("accountUser");
        assertEquals(0, new BigDecimal("70.0").compareTo(user.getSavingsAccount().getAccountBalance()));
        assertFalse(savingsTransactionDao.findAll().isEmpty());
    }
}
