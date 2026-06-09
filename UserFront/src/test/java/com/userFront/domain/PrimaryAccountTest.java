package com.userFront.domain;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PrimaryAccountTest {

    @Test
    public void testGettersAndSetters() {
        PrimaryAccount account = new PrimaryAccount();
        account.setId(1L);
        account.setAccountNumber(12345);
        account.setAccountBalance(new BigDecimal("1500.00"));

        assertEquals(Long.valueOf(1L), account.getId());
        assertEquals(12345, account.getAccountNumber());
        assertEquals(new BigDecimal("1500.00"), account.getAccountBalance());
    }

    @Test
    public void testTransactionList() {
        PrimaryAccount account = new PrimaryAccount();
        List<PrimaryTransaction> transactions = new ArrayList<>();
        transactions.add(new PrimaryTransaction());
        account.setPrimaryTransactionList(transactions);

        assertEquals(1, account.getPrimaryTransactionList().size());
    }
}
