package com.userFront.transaction.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userFront.account.dao.PrimaryAccountDao;
import com.userFront.account.dao.SavingsAccountDao;
import com.userFront.account.domain.PrimaryAccount;
import com.userFront.account.domain.SavingsAccount;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.transaction.dao.PrimaryTransactionDao;
import com.userFront.transaction.dao.SavingsTransactionDao;
import com.userFront.transaction.domain.PrimaryTransaction;
import com.userFront.transaction.domain.Recipient;
import com.userFront.transaction.domain.SavingsTransaction;

@Service
public class LedgerServiceImpl implements LedgerService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;

	@Autowired
	private SavingsTransactionDao savingsTransactionDao;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	public List<PrimaryTransaction> findPrimaryTransactionList(String username) {
		User user = userDao.findByUsername(username);
		PrimaryAccount primaryAccount = primaryAccountDao.findOne(user.getPrimaryAccountId());
		List<PrimaryTransaction> primaryTransactionList = primaryAccount.getPrimaryTransactionList();

		return primaryTransactionList;
	}

	public List<SavingsTransaction> findSavingsTransactionList(String username) {
		User user = userDao.findByUsername(username);
		SavingsAccount savingsAccount = savingsAccountDao.findOne(user.getSavingsAccountId());
		List<SavingsTransaction> savingsTransactionList = savingsAccount.getSavingsTransactionList();

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
	
	public void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) throws Exception {
        if (transferFrom.equalsIgnoreCase("Primary") && transferTo.equalsIgnoreCase("Savings")) {
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(new BigDecimal(amount)));
            primaryAccountDao.save(primaryAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Account", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionDao.save(primaryTransaction);
        } else if (transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Primary")) {
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(new BigDecimal(amount)));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            primaryAccountDao.save(primaryAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
        } else {
            throw new Exception("Invalid Transfer");
        }
    }

    public void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        if (accountType.equalsIgnoreCase("Primary")) {
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount), primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionDao.save(primaryTransaction);
        } else if (accountType.equalsIgnoreCase("Savings")) {
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(new BigDecimal(amount)));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", Double.parseDouble(amount), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
        }
    }
}
