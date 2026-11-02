package com.userFront.service.exception;

public class TransactionRejectedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TransactionRejectedException(String message) {
		super(message);
	}
}
