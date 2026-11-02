package com.userFront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.AccountService;
import com.userFront.service.InvalidTransactionException;
import com.userFront.service.TransactionAmounts;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@Service
public class AccountServiceImpl implements AccountService {

	private static int nextAccountNumber = 11223145;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@Autowired
	private UserService userService;
	
	@Autowired
	private TransactionService transactionService;

	@PersistenceContext
	private EntityManager entityManager;

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
	public void deposit(String accountType, String amount, Principal principal) {
        BigDecimal value = TransactionAmounts.parse(amount);
        User user = userService.findByUsername(principal.getName());

        if (accountType.equalsIgnoreCase("Primary")) {
            PrimaryAccount primaryAccount = lockForUpdate(user.getPrimaryAccount(), PrimaryAccount.class);
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(value));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Deposit to Primary Account", "Account", "Finished", value.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
            transactionService.savePrimaryDepositTransaction(primaryTransaction);
            
        } else if (accountType.equalsIgnoreCase("Savings")) {
            SavingsAccount savingsAccount = lockForUpdate(user.getSavingsAccount(), SavingsAccount.class);
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(value));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();
            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Deposit to savings Account", "Account", "Finished", value.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsDepositTransaction(savingsTransaction);
        } else {
            throw new InvalidTransactionException("Please select the account to deposit to.");
        }
    }
    
	@Transactional
    public void withdraw(String accountType, String amount, Principal principal) {
        BigDecimal value = TransactionAmounts.parse(amount);
        User user = userService.findByUsername(principal.getName());

        if (accountType.equalsIgnoreCase("Primary")) {
            PrimaryAccount primaryAccount = lockForUpdate(user.getPrimaryAccount(), PrimaryAccount.class);
            TransactionAmounts.requireSufficientFunds(primaryAccount.getAccountBalance(), value);
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(value));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Withdraw from Primary Account", "Account", "Finished", value.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
            transactionService.savePrimaryWithdrawTransaction(primaryTransaction);
        } else if (accountType.equalsIgnoreCase("Savings")) {
            SavingsAccount savingsAccount = lockForUpdate(user.getSavingsAccount(), SavingsAccount.class);
            TransactionAmounts.requireSufficientFunds(savingsAccount.getAccountBalance(), value);
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(value));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();
            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Withdraw from savings Account", "Account", "Finished", value.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
            transactionService.saveSavingsWithdrawTransaction(savingsTransaction);
        } else {
            throw new InvalidTransactionException("Please select the account to withdraw from.");
        }
    }

	private <T> T lockForUpdate(T account, Class<T> type) {
		if (entityManager.contains(account)) {
			entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
			return account;
		}
		Object id = entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(account);
		return entityManager.find(type, id, LockModeType.PESSIMISTIC_WRITE);
	}

	private int accountGen() {
		return ++nextAccountNumber;
	}

}
