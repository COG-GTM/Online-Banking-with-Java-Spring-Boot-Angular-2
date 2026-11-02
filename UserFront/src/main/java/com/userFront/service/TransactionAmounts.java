package com.userFront.service;

import java.math.BigDecimal;

public final class TransactionAmounts {

	public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000.00");

	private TransactionAmounts() {
	}

	public static BigDecimal parse(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			throw new InvalidTransactionException("Please enter an amount.");
		}

		BigDecimal amount;
		try {
			amount = new BigDecimal(raw.trim());
		} catch (NumberFormatException e) {
			throw new InvalidTransactionException("Amount must be a number, for example 25.00.");
		}

		if (amount.signum() <= 0) {
			throw new InvalidTransactionException("Amount must be greater than zero.");
		}
		if (amount.stripTrailingZeros().scale() > 2) {
			throw new InvalidTransactionException("Amount can have at most two decimal places.");
		}
		if (amount.compareTo(MAX_AMOUNT) > 0) {
			throw new InvalidTransactionException("Amount cannot exceed " + MAX_AMOUNT.toPlainString() + ".");
		}

		return amount.setScale(2);
	}

	public static void requireSufficientFunds(BigDecimal balance, BigDecimal amount) {
		if (balance == null || balance.compareTo(amount) < 0) {
			throw new InvalidTransactionException("Insufficient funds for this transaction.");
		}
	}
}
