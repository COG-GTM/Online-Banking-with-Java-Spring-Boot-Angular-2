package com.userFront.exception;

/**
 * Thrown when an account does not hold enough funds to satisfy a withdrawal or
 * transfer request.
 */
public class InsufficientFundsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InsufficientFundsException(String message) {
        super(message);
    }
}
