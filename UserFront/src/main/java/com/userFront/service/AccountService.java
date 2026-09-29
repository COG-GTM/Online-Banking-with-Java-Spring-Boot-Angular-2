package com.userFront.service;

import java.security.Principal;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

public interface AccountService {

	PrimaryAccount createPrimaryAccount();
	SavingsAccount createSavingsAccount();
	
	void deposit(String accountType, String amount, Principal principal);
	void withdraw(String accountType, String amount, Principal principal);
}
