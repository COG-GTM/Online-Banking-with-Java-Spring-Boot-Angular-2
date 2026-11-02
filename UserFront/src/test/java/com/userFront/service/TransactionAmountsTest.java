package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

public class TransactionAmountsTest {

	@Test
	public void parsesPositiveAmountsToTwoDecimals() {
		assertEquals(new BigDecimal("25.00"), TransactionAmounts.parse("25"));
		assertEquals(new BigDecimal("0.01"), TransactionAmounts.parse(" 0.01 "));
		assertEquals(new BigDecimal("10.50"), TransactionAmounts.parse("10.500"));
	}

	@Test
	public void rejectsNegativeZeroAndMalformedAmounts() {
		String[] invalid = { "-1000000", "-0.01", "0", "0.00", "", "   ", null, "abc", "NaN", "Infinity", "1e400",
				"10.001", "1000000000.01" };
		for (String raw : invalid) {
			try {
				TransactionAmounts.parse(raw);
				fail("Expected rejection of amount: " + raw);
			} catch (InvalidTransactionException expected) {
			}
		}
	}

	@Test
	public void requiresSufficientFunds() {
		TransactionAmounts.requireSufficientFunds(new BigDecimal("100.00"), new BigDecimal("100.00"));
		try {
			TransactionAmounts.requireSufficientFunds(new BigDecimal("100.00"), new BigDecimal("100.01"));
			fail("Expected insufficient funds");
		} catch (InvalidTransactionException expected) {
		}
	}
}
