package com.userFront.service;

import java.math.BigDecimal;

public final class TransferAmount {

	public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
	public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");
	private static final int SCALE = 2;
	private static final int MAX_INPUT_LENGTH = 20;

	private TransferAmount() {
	}

	public static BigDecimal parse(String rawAmount) {
		String trimmed = rawAmount == null ? "" : rawAmount.trim();
		if (trimmed.isEmpty()) {
			throw new InvalidTransferException("Please specify an amount.");
		}
		if (trimmed.length() > MAX_INPUT_LENGTH || !trimmed.matches("\\d+(\\.\\d{1,2})?")) {
			throw new InvalidTransferException("Amount must be a positive number with at most 2 decimal places.");
		}

		BigDecimal amount = new BigDecimal(trimmed).setScale(SCALE);
		if (amount.compareTo(MIN_AMOUNT) < 0) {
			throw new InvalidTransferException("Amount must be at least " + MIN_AMOUNT.toPlainString() + ".");
		}
		if (amount.compareTo(MAX_AMOUNT) > 0) {
			throw new InvalidTransferException("Amount cannot exceed " + MAX_AMOUNT.toPlainString() + ".");
		}
		return amount;
	}

	public static void requireSufficientFunds(BigDecimal balance, BigDecimal amount) {
		if (balance == null || balance.compareTo(amount) < 0) {
			throw new InvalidTransferException("Insufficient funds for this transfer.");
		}
	}
}
