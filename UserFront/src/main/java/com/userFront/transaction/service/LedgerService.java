package com.userFront.transaction.service;

import java.util.List;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.transaction.domain.PrimaryTransaction;
import com.userFront.transaction.domain.Recipient;
import com.userFront.transaction.domain.SavingsTransaction;

public interface LedgerService {

	List<PrimaryTransaction> findPrimaryTransactionList(String username);

	List<SavingsTransaction> findSavingsTransactionList(String username);

	void savePrimaryDepositTransaction(PrimaryTransaction primaryTransaction);

	void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction);

	void savePrimaryWithdrawTransaction(PrimaryTransaction primaryTransaction);

	void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction);

	void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, PrimaryAccount primaryAccount,
			SavingsAccount savingsAccount) throws Exception;

	void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, PrimaryAccount primaryAccount,
			SavingsAccount savingsAccount);
}
