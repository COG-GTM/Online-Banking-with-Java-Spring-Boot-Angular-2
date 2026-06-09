package com.userFront.domain;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

public class SavingsTransactionTest {

    @Test
    public void testDefaultConstructor() {
        SavingsTransaction tx = new SavingsTransaction();
        assertNull(tx.getId());
        assertNull(tx.getDate());
        assertNull(tx.getDescription());
    }

    @Test
    public void testParameterizedConstructor() {
        SavingsAccount account = new SavingsAccount();
        Date date = new Date();
        BigDecimal balance = new BigDecimal("2000.00");

        SavingsTransaction tx = new SavingsTransaction(date, "Deposit", "Account", "Finished",
                700.0, balance, account);

        assertEquals(date, tx.getDate());
        assertEquals("Deposit", tx.getDescription());
        assertEquals("Account", tx.getType());
        assertEquals("Finished", tx.getStatus());
        assertEquals(700.0, tx.getAmount(), 0.001);
        assertEquals(balance, tx.getAvailableBalance());
        assertEquals(account, tx.getSavingsAccount());
    }

    @Test
    public void testSetters() {
        SavingsTransaction tx = new SavingsTransaction();
        Date date = new Date();
        SavingsAccount account = new SavingsAccount();

        tx.setId(2L);
        tx.setDate(date);
        tx.setDescription("Transfer");
        tx.setType("Transfer");
        tx.setStatus("Finished");
        tx.setAmount(300.0);
        tx.setAvailableBalance(new BigDecimal("1700.00"));
        tx.setSavingsAccount(account);

        assertEquals(Long.valueOf(2L), tx.getId());
        assertEquals(date, tx.getDate());
        assertEquals("Transfer", tx.getDescription());
        assertEquals("Transfer", tx.getType());
        assertEquals("Finished", tx.getStatus());
        assertEquals(300.0, tx.getAmount(), 0.001);
        assertEquals(new BigDecimal("1700.00"), tx.getAvailableBalance());
        assertEquals(account, tx.getSavingsAccount());
    }
}
