package com.userFront.account.service;

import java.security.Principal;

import com.userFront.account.domain.PrimaryAccount;
import com.userFront.account.domain.SavingsAccount;

public interface AccountService {

	PrimaryAccount createPrimaryAccount();
	SavingsAccount createSavingsAccount();
	
	void deposit(String accountType, double amount, Principal principal);
	void withdraw(String accountType, double amount, Principal principal);
}
