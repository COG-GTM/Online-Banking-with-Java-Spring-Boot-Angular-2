package com.userFront.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.userFront.exception.InsufficientFundsException;
import com.userFront.exception.InvalidAmountException;

public class AmountValidatorTest {

	@Test
	public void parsesValidAmount() {
		assertEquals(new BigDecimal("10.50"), AmountValidator.parse(" 10.50 "));
	}

	@Test
	public void normalizesScaleToTwoDecimals() {
		assertEquals(new BigDecimal("10.00"), AmountValidator.parse("10"));
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsNegativeAmount() {
		AmountValidator.parse("-1000000");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsZeroAmount() {
		AmountValidator.parse("0");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsAmountBelowOneCent() {
		AmountValidator.parse("0.001");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsAmountAboveMaximum() {
		AmountValidator.parse("1000000.01");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsScientificNotationAboveMaximum() {
		AmountValidator.parse("1e9");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsNonNumericAmount() {
		AmountValidator.parse("abc");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsInfinity() {
		AmountValidator.parse("Infinity");
	}

	@Test(expected = InvalidAmountException.class)
	public void rejectsMissingAmount() {
		AmountValidator.parse("   ");
	}

	@Test(expected = InsufficientFundsException.class)
	public void rejectsOverdraft() {
		AmountValidator.requireSufficientFunds(new BigDecimal("50.00"), new BigDecimal("50.01"));
	}

	@Test
	public void allowsDebitUpToTheFullBalance() {
		AmountValidator.requireSufficientFunds(new BigDecimal("50.00"), new BigDecimal("50.00"));
	}
}
