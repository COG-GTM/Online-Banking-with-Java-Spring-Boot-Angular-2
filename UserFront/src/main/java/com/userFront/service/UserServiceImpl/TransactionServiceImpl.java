package com.userFront.service.UserServiceImpl;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.userFront.service.InvalidTransactionException;
import com.userFront.service.TransactionAmounts;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

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

	@PersistenceContext
	private EntityManager entityManager;

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
	public void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        BigDecimal value = TransactionAmounts.parse(amount);

        if (transferFrom.equalsIgnoreCase("Primary") && transferTo.equalsIgnoreCase("Savings")) {
            primaryAccount = lockForUpdate(primaryAccount, PrimaryAccount.class);
            savingsAccount = lockForUpdate(savingsAccount, SavingsAccount.class);
            TransactionAmounts.requireSufficientFunds(primaryAccount.getAccountBalance(), value);

            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(value));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(value));
            primaryAccountDao.save(primaryAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Account", "Finished", value.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionDao.save(primaryTransaction);
        } else if (transferFrom.equalsIgnoreCase("Savings") && transferTo.equalsIgnoreCase("Primary")) {
            primaryAccount = lockForUpdate(primaryAccount, PrimaryAccount.class);
            savingsAccount = lockForUpdate(savingsAccount, SavingsAccount.class);
            TransactionAmounts.requireSufficientFunds(savingsAccount.getAccountBalance(), value);

            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(value));
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(value));
            primaryAccountDao.save(primaryAccount);
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Transfer", "Finished", value.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
        } else {
            throw new InvalidTransactionException("Please choose two different accounts to transfer between.");
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
    
    public Recipient findRecipientByNameForUser(String recipientName, String username) {
        Recipient recipient = recipientDao.findByName(recipientName);
        if (recipient == null || recipient.getUser() == null || !username.equals(recipient.getUser().getUsername())) {
            return null;
        }
        return recipient;
    }

    @Transactional
    public void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        if (recipient == null) {
            throw new InvalidTransactionException("Please choose one of your saved recipients.");
        }
        BigDecimal value = TransactionAmounts.parse(amount);

        if (accountType.equalsIgnoreCase("Primary")) {
            primaryAccount = lockForUpdate(primaryAccount, PrimaryAccount.class);
            TransactionAmounts.requireSufficientFunds(primaryAccount.getAccountBalance(), value);

            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(value));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", value.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionDao.save(primaryTransaction);
        } else if (accountType.equalsIgnoreCase("Savings")) {
            savingsAccount = lockForUpdate(savingsAccount, SavingsAccount.class);
            TransactionAmounts.requireSufficientFunds(savingsAccount.getAccountBalance(), value);

            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(value));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", value.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
        } else {
            throw new InvalidTransactionException("Please select the account to transfer from.");
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
}
