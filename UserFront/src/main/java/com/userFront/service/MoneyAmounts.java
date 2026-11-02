package com.userFront.service;

import java.math.BigDecimal;

import com.userFront.service.exception.InsufficientFundsException;
import com.userFront.service.exception.InvalidAmountException;

public final class MoneyAmounts {

	// Keeps cents exact when the amount is also stored as a double in the transaction history.
	public static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

	private MoneyAmounts() {
	}

	public static BigDecimal parse(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			throw new InvalidAmountException("Please enter an amount.");
		}

		BigDecimal value;
		try {
			value = new BigDecimal(raw.trim());
		} catch (NumberFormatException e) {
			throw new InvalidAmountException("Amount must be a number.");
		}

		if (value.signum() <= 0) {
			throw new InvalidAmountException("Amount must be greater than zero.");
		}
		if (value.stripTrailingZeros().scale() > 2) {
			throw new InvalidAmountException("Amount cannot have more than two decimal places.");
		}
		if (value.compareTo(MAX_AMOUNT) > 0) {
			throw new InvalidAmountException("Amount exceeds the maximum allowed.");
		}

		return value.setScale(2);
	}

	public static void requireFunds(BigDecimal balance, BigDecimal amount) {
		if (balance == null || balance.compareTo(amount) < 0) {
			throw new InsufficientFundsException();
		}
	}
}
