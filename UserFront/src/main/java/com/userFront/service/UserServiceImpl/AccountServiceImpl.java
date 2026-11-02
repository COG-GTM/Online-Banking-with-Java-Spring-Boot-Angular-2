package com.userFront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.dao.AccountLockDao;
import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.AccountService;
import com.userFront.service.MoneyAmounts;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.exception.InvalidAmountException;
import com.userFront.service.exception.TransactionRejectedException;

@Service
public class AccountServiceImpl implements AccountService {

	private static int nextAccountNumber = 11223145;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@Autowired
	private AccountLockDao accountLockDao;

	@Autowired
	private UserService userService;
	
	@Autowired
	private TransactionService transactionService;

	public PrimaryAccount createPrimaryAccount() {
		PrimaryAccount primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountBalance(new BigDecimal(0.0));
		primaryAccount.setAccountNumber(accountGen());

		primaryAccountDao.save(primaryAccount);

		return primaryAccountDao.findByAccountNumber(primaryAccount.getAccountNumber());
	}

	public SavingsAccount createSavingsAccount() {
		SavingsAccount savingsAccount = new SavingsAccount();
		savingsAccount.setAccountBalance(new BigDecimal(0.0));
		savingsAccount.setAccountNumber(accountGen());

		savingsAccountDao.save(savingsAccount);

		return savingsAccountDao.findByAccountNumber(savingsAccount.getAccountNumber());
	}
	
	@Transactional
	public void deposit(String accountType, BigDecimal amount, Principal principal) {
		requirePositive(amount);
		User user = userService.findByUsername(principal.getName());

		if (accountType.equalsIgnoreCase("Primary")) {
			PrimaryAccount primaryAccount = accountLockDao.lockPrimaryAccount(user.getPrimaryAccount().getId());
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(amount));
			primaryAccountDao.save(primaryAccount);

			Date date = new Date();

			PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Deposit to Primary Account", "Account", "Finished", amount.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
			transactionService.savePrimaryDepositTransaction(primaryTransaction);

		} else if (accountType.equalsIgnoreCase("Savings")) {
			SavingsAccount savingsAccount = accountLockDao.lockSavingsAccount(user.getSavingsAccount().getId());
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(amount));
			savingsAccountDao.save(savingsAccount);

			Date date = new Date();
			SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Deposit to savings Account", "Account", "Finished", amount.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
			transactionService.saveSavingsDepositTransaction(savingsTransaction);
		} else {
			throw new TransactionRejectedException("Please select a valid account.");
		}
	}

	@Transactional
	public void withdraw(String accountType, BigDecimal amount, Principal principal) {
		requirePositive(amount);
		User user = userService.findByUsername(principal.getName());

		if (accountType.equalsIgnoreCase("Primary")) {
			PrimaryAccount primaryAccount = accountLockDao.lockPrimaryAccount(user.getPrimaryAccount().getId());
			MoneyAmounts.requireFunds(primaryAccount.getAccountBalance(), amount);
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(amount));
			primaryAccountDao.save(primaryAccount);

			Date date = new Date();

			PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Withdraw from Primary Account", "Account", "Finished", amount.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
			transactionService.savePrimaryWithdrawTransaction(primaryTransaction);
		} else if (accountType.equalsIgnoreCase("Savings")) {
			SavingsAccount savingsAccount = accountLockDao.lockSavingsAccount(user.getSavingsAccount().getId());
			MoneyAmounts.requireFunds(savingsAccount.getAccountBalance(), amount);
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(amount));
			savingsAccountDao.save(savingsAccount);

			Date date = new Date();
			SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Withdraw from savings Account", "Account", "Finished", amount.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
			transactionService.saveSavingsWithdrawTransaction(savingsTransaction);
		} else {
			throw new TransactionRejectedException("Please select a valid account.");
		}
	}

	private static void requirePositive(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			throw new InvalidAmountException("Amount must be greater than zero.");
		}
	}

	private int accountGen() {
		return ++nextAccountNumber;
	}

}
