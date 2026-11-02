package com.userFront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.dao.AccountLockDao;
import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.RecipientDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.MoneyAmounts;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.exception.InvalidAmountException;
import com.userFront.service.exception.TransactionRejectedException;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private UserService userService;

	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;

	@Autowired
	private SavingsTransactionDao savingsTransactionDao;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;
	
	@Autowired
	private RecipientDao recipientDao;

	@Autowired
	private AccountLockDao accountLockDao;

	public List<PrimaryTransaction> findPrimaryTransactionList(String username) {
		User user = userService.findByUsername(username);
		List<PrimaryTransaction> primaryTransactionList = user.getPrimaryAccount().getPrimaryTransactionList();

		return primaryTransactionList;
	}

	public List<SavingsTransaction> findSavingsTransactionList(String username) {
		User user = userService.findByUsername(username);
		List<SavingsTransaction> savingsTransactionList = user.getSavingsAccount().getSavingsTransactionList();

		return savingsTransactionList;
	}

	public void savePrimaryDepositTransaction(PrimaryTransaction primaryTransaction) {
		primaryTransactionDao.save(primaryTransaction);
	}

	public void saveSavingsDepositTransaction(SavingsTransaction savingsTransaction) {
		savingsTransactionDao.save(savingsTransaction);
	}

	public void savePrimaryWithdrawTransaction(PrimaryTransaction primaryTransaction) {
		primaryTransactionDao.save(primaryTransaction);
	}

	public void saveSavingsWithdrawTransaction(SavingsTransaction savingsTransaction) {
		savingsTransactionDao.save(savingsTransaction);
	}
	
	@Transactional
	public void betweenAccountsTransfer(String transferFrom, String transferTo, BigDecimal amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
		requirePositive(amount);
		boolean primaryToSavings = transferFrom.equalsIgnoreCase("Primary") && transferTo.equalsIgnoreCase("Savings");
		boolean savingsToPrimary = transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Primary");
		if (!primaryToSavings && !savingsToPrimary) {
			throw new TransactionRejectedException("Invalid Transfer");
		}

		// Always lock primary before savings so concurrent transfers in opposite directions cannot deadlock.
		PrimaryAccount lockedPrimary = accountLockDao.lockPrimaryAccount(primaryAccount.getId());
		SavingsAccount lockedSavings = accountLockDao.lockSavingsAccount(savingsAccount.getId());

		if (primaryToSavings) {
			MoneyAmounts.requireFunds(lockedPrimary.getAccountBalance(), amount);
			lockedPrimary.setAccountBalance(lockedPrimary.getAccountBalance().subtract(amount));
			lockedSavings.setAccountBalance(lockedSavings.getAccountBalance().add(amount));
			primaryAccountDao.save(lockedPrimary);
			savingsAccountDao.save(lockedSavings);

			Date date = new Date();

			PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Account", "Finished", amount.doubleValue(), lockedPrimary.getAccountBalance(), lockedPrimary);
			primaryTransactionDao.save(primaryTransaction);
		} else {
			MoneyAmounts.requireFunds(lockedSavings.getAccountBalance(), amount);
			lockedPrimary.setAccountBalance(lockedPrimary.getAccountBalance().add(amount));
			lockedSavings.setAccountBalance(lockedSavings.getAccountBalance().subtract(amount));
			primaryAccountDao.save(lockedPrimary);
			savingsAccountDao.save(lockedSavings);

			Date date = new Date();

			SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Transfer", "Finished", amount.doubleValue(), lockedSavings.getAccountBalance(), lockedSavings);
			savingsTransactionDao.save(savingsTransaction);
		}
	}

	public List<Recipient> findRecipientList(Principal principal) {
        String username = principal.getName();
        List<Recipient> recipientList = recipientDao.findAll().stream() 			//convert list to stream
                .filter(recipient -> username.equals(recipient.getUser().getUsername()))	//filters the line, equals to username
                .collect(Collectors.toList());

        return recipientList;
    }

    public Recipient saveRecipient(Recipient recipient) {
        return recipientDao.save(recipient);
    }

    public Recipient findRecipientByName(String recipientName) {
        return recipientDao.findByName(recipientName);
    }

    public void deleteRecipientByName(String recipientName) {
        recipientDao.deleteByName(recipientName);
    }
    
	@Transactional
	public void toSomeoneElseTransfer(Recipient recipient, String accountType, BigDecimal amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
		requirePositive(amount);
		if (recipient == null) {
			throw new TransactionRejectedException("Please select a valid recipient.");
		}

		if (accountType.equalsIgnoreCase("Primary")) {
			PrimaryAccount lockedPrimary = accountLockDao.lockPrimaryAccount(primaryAccount.getId());
			MoneyAmounts.requireFunds(lockedPrimary.getAccountBalance(), amount);
			lockedPrimary.setAccountBalance(lockedPrimary.getAccountBalance().subtract(amount));
			primaryAccountDao.save(lockedPrimary);

			Date date = new Date();

			PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", amount.doubleValue(), lockedPrimary.getAccountBalance(), lockedPrimary);
			primaryTransactionDao.save(primaryTransaction);
		} else if (accountType.equalsIgnoreCase("Savings")) {
			SavingsAccount lockedSavings = accountLockDao.lockSavingsAccount(savingsAccount.getId());
			MoneyAmounts.requireFunds(lockedSavings.getAccountBalance(), amount);
			lockedSavings.setAccountBalance(lockedSavings.getAccountBalance().subtract(amount));
			savingsAccountDao.save(lockedSavings);

			Date date = new Date();

			SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", amount.doubleValue(), lockedSavings.getAccountBalance(), lockedSavings);
			savingsTransactionDao.save(savingsTransaction);
		} else {
			throw new TransactionRejectedException("Please select a valid account.");
		}
	}

	private static void requirePositive(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			throw new InvalidAmountException("Amount must be greater than zero.");
		}
	}
}
