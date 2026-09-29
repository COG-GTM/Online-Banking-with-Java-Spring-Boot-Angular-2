package com.userFront.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class TransferAmountTest {

	@Test
	public void parsesValidAmountsToTwoDecimalPlaces() {
		assertEquals(new BigDecimal("10.00"), TransferAmount.parse("10"));
		assertEquals(new BigDecimal("10.50"), TransferAmount.parse(" 10.5 "));
		assertEquals(new BigDecimal("0.01"), TransferAmount.parse("0.01"));
		assertEquals(new BigDecimal("1000000.00"), TransferAmount.parse("1000000.00"));
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsNegativeAmount() {
		TransferAmount.parse("-1000000");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsZero() {
		TransferAmount.parse("0.00");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsAmountAboveMaximum() {
		TransferAmount.parse("1000000.01");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsMoreThanTwoDecimalPlaces() {
		TransferAmount.parse("1.001");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsExponentNotation() {
		TransferAmount.parse("1e9");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsNonNumeric() {
		TransferAmount.parse("abc");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsNaN() {
		TransferAmount.parse("NaN");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsBlank() {
		TransferAmount.parse("  ");
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsNull() {
		TransferAmount.parse(null);
	}

	@Test
	public void allowsDebitOfEntireBalance() {
		TransferAmount.requireSufficientFunds(new BigDecimal("50.00"), new BigDecimal("50.00"));
	}

	@Test(expected = InvalidTransferException.class)
	public void rejectsOverdraft() {
		TransferAmount.requireSufficientFunds(new BigDecimal("50.00"), new BigDecimal("50.01"));
	}
}
