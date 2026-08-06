package com.userFront.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.userFront.exception.InsufficientFundsException;
import com.userFront.exception.InvalidAmountException;

@ControllerAdvice(assignableTypes = { AccountController.class, TransferController.class })
public class TransactionExceptionHandler {

	@ExceptionHandler({ InvalidAmountException.class, InsufficientFundsException.class, NumberFormatException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ModelAndView handleRejectedAmount(RuntimeException e) {
		ModelAndView modelAndView = new ModelAndView("transactionError");
		modelAndView.addObject("errorMessage", messageFor(e));

		return modelAndView;
	}

	private String messageFor(RuntimeException e) {
		if (e instanceof NumberFormatException) {
			return "Please specify a valid amount.";
		}

		return e.getMessage();
	}
}
