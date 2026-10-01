package com.userFront.service;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;

public interface TransactionService {
	int DEFAULT_PAGE_SIZE = 20;

	int MAX_PAGE_SIZE = 100;

	Page<PrimaryTransaction> findPrimaryTransactionPage(String username, int page, int size);

	Page<SavingsTransaction> findSavingsTransactionPage(String username, int page, int size);

	void savePrimaryDepositTransaction(PrimaryTransaction primaryTransaction);

	void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction);

	void savePrimaryWithdrawTransaction(PrimaryTransaction primaryTransaction);

	void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction);

	void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, PrimaryAccount primaryAccount,
			SavingsAccount savingsAccount) throws Exception;

	List<Recipient> findRecipientList(Principal principal);

	Recipient saveRecipient(Recipient recipient);

	Recipient findRecipientByName(String recipientName);

	void deleteRecipientByName(String recipientName);
	
	void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount);
	

}
