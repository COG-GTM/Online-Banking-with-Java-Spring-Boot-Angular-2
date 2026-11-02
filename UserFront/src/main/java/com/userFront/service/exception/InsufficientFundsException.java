package com.userFront.service.exception;

public class InsufficientFundsException extends TransactionRejectedException {

	private static final long serialVersionUID = 1L;

	public InsufficientFundsException() {
		super("Insufficient funds for this transaction.");
	}
}
