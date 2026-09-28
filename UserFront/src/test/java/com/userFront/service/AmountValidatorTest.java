package com.userFront.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class AmountValidatorTest {

	@Test
	public void parsesPositiveAmount() {
		assertEquals(new BigDecimal("12.50"), AmountValidator.parse(" 12.5 "));
	}

	@Test
	public void acceptsMaximumAmount() {
		assertEquals(AmountValidator.MAX_AMOUNT, AmountValidator.parse("1000000"));
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsNegativeAmount() {
		AmountValidator.parse("-1000000");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsZero() {
		AmountValidator.parse("0");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsNonNumeric() {
		AmountValidator.parse("abc");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsNaN() {
		AmountValidator.parse("NaN");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsNull() {
		AmountValidator.parse(null);
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsBlank() {
		AmountValidator.parse("  ");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsAmountAboveMaximum() {
		AmountValidator.parse("1000000.01");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsHugeExponent() {
		AmountValidator.parse("1E+999999999");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsSubCentPrecision() {
		AmountValidator.parse("0.001");
	}

	@Test
	public void allowsDebitOfEntireBalance() {
		AmountValidator.requireSufficientFunds(new BigDecimal("10.00"), new BigDecimal("10.00"));
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsOverdraft() {
		AmountValidator.requireSufficientFunds(new BigDecimal("10.00"), new BigDecimal("10.01"));
	}
}
