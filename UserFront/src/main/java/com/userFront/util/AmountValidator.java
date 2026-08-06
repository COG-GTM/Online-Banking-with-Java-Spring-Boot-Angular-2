package com.userFront.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.userFront.exception.InsufficientFundsException;
import com.userFront.exception.InvalidAmountException;

/**
 * Server side validation of monetary amounts submitted by users.
 */
public final class AmountValidator {

	public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");

	public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");

	private AmountValidator() {
	}

	/**
	 * Parses a user supplied amount and validates it.
	 *
	 * @throws InvalidAmountException if the value is missing, not a number or outside the accepted range
	 */
	public static BigDecimal parse(String amount) {
		if (amount == null || amount.trim().isEmpty()) {
			throw new InvalidAmountException("Please specify an amount.");
		}

		BigDecimal parsedAmount;
		try {
			parsedAmount = new BigDecimal(amount.trim());
		} catch (NumberFormatException e) {
			throw new InvalidAmountException("Please specify a valid amount.");
		}

		return validate(parsedAmount);
	}

	/**
	 * Validates an already parsed amount: it must be positive, no more than two decimal places
	 * and within the accepted range.
	 *
	 * @throws InvalidAmountException if the amount is not acceptable
	 */
	public static BigDecimal validate(BigDecimal amount) {
		if (amount == null) {
			throw new InvalidAmountException("Please specify an amount.");
		}
		if (amount.scale() > 2) {
			throw new InvalidAmountException("Amount cannot have more than two decimal places.");
		}
		if (amount.compareTo(MIN_AMOUNT) < 0) {
			throw new InvalidAmountException("Amount must be at least " + MIN_AMOUNT.toPlainString() + ".");
		}
		if (amount.compareTo(MAX_AMOUNT) > 0) {
			throw new InvalidAmountException("Amount cannot exceed " + MAX_AMOUNT.toPlainString() + ".");
		}

		return amount.setScale(2, RoundingMode.UNNECESSARY);
	}

	/**
	 * Ensures the account holds enough funds for the requested debit.
	 *
	 * @throws InsufficientFundsException if the balance would go negative
	 */
	public static void requireSufficientFunds(BigDecimal balance, BigDecimal amount) {
		if (balance == null || balance.compareTo(amount) < 0) {
			throw new InsufficientFundsException("Insufficient funds for this transaction.");
		}
	}
}
