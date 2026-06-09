package com.userFront.domain;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

public class PrimaryTransactionTest {

    @Test
    public void testDefaultConstructor() {
        PrimaryTransaction tx = new PrimaryTransaction();
        assertNull(tx.getId());
        assertNull(tx.getDate());
        assertNull(tx.getDescription());
    }

    @Test
    public void testParameterizedConstructor() {
        PrimaryAccount account = new PrimaryAccount();
        Date date = new Date();
        BigDecimal balance = new BigDecimal("1000.00");

        PrimaryTransaction tx = new PrimaryTransaction(date, "Deposit", "Account", "Finished",
                500.0, balance, account);

        assertEquals(date, tx.getDate());
        assertEquals("Deposit", tx.getDescription());
        assertEquals("Account", tx.getType());
        assertEquals("Finished", tx.getStatus());
        assertEquals(500.0, tx.getAmount(), 0.001);
        assertEquals(balance, tx.getAvailableBalance());
        assertEquals(account, tx.getPrimaryAccount());
    }

    @Test
    public void testSetters() {
        PrimaryTransaction tx = new PrimaryTransaction();
        Date date = new Date();
        PrimaryAccount account = new PrimaryAccount();

        tx.setId(1L);
        tx.setDate(date);
        tx.setDescription("Withdrawal");
        tx.setType("Transfer");
        tx.setStatus("Pending");
        tx.setAmount(200.0);
        tx.setAvailableBalance(new BigDecimal("800.00"));
        tx.setPrimaryAccount(account);

        assertEquals(Long.valueOf(1L), tx.getId());
        assertEquals(date, tx.getDate());
        assertEquals("Withdrawal", tx.getDescription());
        assertEquals("Transfer", tx.getType());
        assertEquals("Pending", tx.getStatus());
        assertEquals(200.0, tx.getAmount(), 0.001);
        assertEquals(new BigDecimal("800.00"), tx.getAvailableBalance());
        assertEquals(account, tx.getPrimaryAccount());
    }
}
