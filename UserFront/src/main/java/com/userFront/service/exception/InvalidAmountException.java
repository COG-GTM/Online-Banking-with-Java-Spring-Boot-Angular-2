package com.userFront.service.exception;

public class InvalidAmountException extends TransactionRejectedException {

	private static final long serialVersionUID = 1L;

	public InvalidAmountException(String message) {
		super(message);
	}
}
