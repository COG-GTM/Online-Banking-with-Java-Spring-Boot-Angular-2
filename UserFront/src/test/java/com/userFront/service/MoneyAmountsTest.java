package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import com.userFront.service.exception.InsufficientFundsException;
import com.userFront.service.exception.InvalidAmountException;

public class MoneyAmountsTest {

	@Test
	public void parsesPositiveAmountsToTwoDecimals() {
		assertEquals(new BigDecimal("100.00"), MoneyAmounts.parse("100"));
		assertEquals(new BigDecimal("0.01"), MoneyAmounts.parse(" 0.01 "));
		assertEquals(new BigDecimal("12.50"), MoneyAmounts.parse("12.500"));
		assertEquals(MoneyAmounts.MAX_AMOUNT, MoneyAmounts.parse("999999999999.99"));
	}

	@Test
	public void rejectsInvalidAmounts() {
		String[] invalid = { null, "", "   ", "-1000000", "-0.01", "0", "0.00", "abc", "NaN", "Infinity",
				"1.001", "1E-3", "1000000000000", "1e400" };
		for (String raw : invalid) {
			try {
				MoneyAmounts.parse(raw);
				fail("expected rejection for " + raw);
			} catch (InvalidAmountException expected) {
			}
		}
	}

	@Test
	public void requireFundsAllowsExactBalance() {
		MoneyAmounts.requireFunds(new BigDecimal("50.00"), new BigDecimal("50.00"));
	}

	@Test(expected = InsufficientFundsException.class)
	public void requireFundsRejectsOverdraft() {
		MoneyAmounts.requireFunds(new BigDecimal("50.00"), new BigDecimal("50.01"));
	}
}
