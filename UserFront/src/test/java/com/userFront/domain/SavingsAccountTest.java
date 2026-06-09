package com.userFront.domain;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SavingsAccountTest {

    @Test
    public void testGettersAndSetters() {
        SavingsAccount account = new SavingsAccount();
        account.setId(1L);
        account.setAccountNumber(67890);
        account.setAccountBalance(new BigDecimal("2500.00"));

        assertEquals(Long.valueOf(1L), account.getId());
        assertEquals(67890, account.getAccountNumber());
        assertEquals(new BigDecimal("2500.00"), account.getAccountBalance());
    }

    @Test
    public void testTransactionList() {
        SavingsAccount account = new SavingsAccount();
        List<SavingsTransaction> transactions = new ArrayList<>();
        transactions.add(new SavingsTransaction());
        account.setSavingsTransactionList(transactions);

        assertEquals(1, account.getSavingsTransactionList().size());
    }
}
