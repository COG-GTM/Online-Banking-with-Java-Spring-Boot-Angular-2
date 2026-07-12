package com.userFront.util;

import java.math.BigDecimal;

import com.userFront.exception.InsufficientFundsException;

/**
 * Helpers for parsing and validating monetary amounts. All money handling uses
 * {@link BigDecimal} to avoid the precision loss inherent in binary floating
 * point arithmetic.
 */
public final class AmountUtil {

    private AmountUtil() {
    }

    /**
     * Parse a user-supplied amount and ensure it is a strictly positive value.
     *
     * @throws IllegalArgumentException if the value is not a valid number or is
     *                                  not greater than zero
     */
    public static BigDecimal parsePositiveAmount(String amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        BigDecimal value;
        try {
            value = new BigDecimal(amount.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + amount, e);
        }

        return requirePositive(value);
    }

    /**
     * Ensure an already-parsed amount is strictly positive.
     *
     * @throws IllegalArgumentException if the value is null or not greater than zero
     */
    public static BigDecimal requirePositive(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return amount;
    }

    /**
     * Ensure the given balance can cover the requested amount.
     *
     * @throws InsufficientFundsException if {@code balance} is less than {@code amount}
     */
    public static void requireSufficientFunds(BigDecimal balance, BigDecimal amount) {
        if (balance == null || balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for the requested amount");
        }
    }
}
