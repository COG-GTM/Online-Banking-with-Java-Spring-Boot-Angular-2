package com.userFront.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.userFront.exception.InsufficientFundsException;

public class AmountUtilTest {

    @Test
    public void parsePositiveAmountAcceptsValidValue() {
        assertEquals(0, new BigDecimal("12.34").compareTo(AmountUtil.parsePositiveAmount(" 12.34 ")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePositiveAmountRejectsNonNumeric() {
        AmountUtil.parsePositiveAmount("not-a-number");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePositiveAmountRejectsNull() {
        AmountUtil.parsePositiveAmount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePositiveAmountRejectsNegative() {
        AmountUtil.parsePositiveAmount("-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parsePositiveAmountRejectsZero() {
        AmountUtil.parsePositiveAmount("0");
    }

    @Test(expected = InsufficientFundsException.class)
    public void requireSufficientFundsRejectsOverdraw() {
        AmountUtil.requireSufficientFunds(new BigDecimal("10"), new BigDecimal("10.01"));
    }

    @Test
    public void requireSufficientFundsAllowsExactBalance() {
        AmountUtil.requireSufficientFunds(new BigDecimal("10"), new BigDecimal("10"));
    }
}
