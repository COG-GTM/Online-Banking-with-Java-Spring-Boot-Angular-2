package com.userFront.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransferException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidTransferException(String message) {
		super(message);
	}
}
