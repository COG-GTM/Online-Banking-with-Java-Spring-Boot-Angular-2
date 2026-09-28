package com.userFront.service;

import java.math.BigDecimal;

public final class AmountValidator {

	public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");
	private static final int MAX_SCALE = 2;
	private static final int MAX_INPUT_LENGTH = 32;

	private AmountValidator() {
	}

	public static BigDecimal parse(String rawAmount) {
		if (rawAmount == null) {
			throw new InvalidAmountException("Amount is required");
		}
		String trimmed = rawAmount.trim();
		if (trimmed.isEmpty() || trimmed.length() > MAX_INPUT_LENGTH) {
			throw new InvalidAmountException("Amount is required");
		}

		BigDecimal amount;
		try {
			amount = new BigDecimal(trimmed);
		} catch (NumberFormatException e) {
			throw new InvalidAmountException("Amount must be a number");
		}

		if (amount.signum() <= 0) {
			throw new InvalidAmountException("Amount must be greater than zero");
		}
		if (amount.stripTrailingZeros().scale() > MAX_SCALE) {
			throw new InvalidAmountException("Amount cannot have more than " + MAX_SCALE + " decimal places");
		}
		if (amount.compareTo(MAX_AMOUNT) > 0) {
			throw new InvalidAmountException("Amount exceeds the maximum of " + MAX_AMOUNT.toPlainString());
		}
		return amount.setScale(MAX_SCALE);
	}

	public static void requireSufficientFunds(BigDecimal balance, BigDecimal amount) {
		if (balance == null || balance.compareTo(amount) < 0) {
			throw new InvalidAmountException("Insufficient funds");
		}
	}
}
