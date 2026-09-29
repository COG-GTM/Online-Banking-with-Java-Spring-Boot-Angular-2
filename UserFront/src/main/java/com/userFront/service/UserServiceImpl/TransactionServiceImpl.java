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
import com.userFront.service.InvalidTransferException;
import com.userFront.service.TransactionService;
import com.userFront.service.TransferAmount;
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
	
	@Transactional(rollbackFor = Exception.class)
	public void betweenAccountsTransfer(String transferFrom, String transferTo, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) throws Exception {
		BigDecimal transferAmount = TransferAmount.parse(amount);
		boolean primaryToSavings = "Primary".equalsIgnoreCase(transferFrom) && "Savings".equalsIgnoreCase(transferTo);
		boolean savingsToPrimary = "Savings".equalsIgnoreCase(transferFrom) && "Primary".equalsIgnoreCase(transferTo);
		if (!primaryToSavings && !savingsToPrimary) {
			throw new InvalidTransferException("Invalid Transfer");
		}

		primaryAccount = lockForUpdate(PrimaryAccount.class, primaryAccount, primaryAccount.getId());
		savingsAccount = lockForUpdate(SavingsAccount.class, savingsAccount, savingsAccount.getId());

		if (primaryToSavings) {
			TransferAmount.requireSufficientFunds(primaryAccount.getAccountBalance(), transferAmount);
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(transferAmount));
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().add(transferAmount));
			primaryAccountDao.save(primaryAccount);
			savingsAccountDao.save(savingsAccount);

			Date date = new Date();

			PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Account", "Finished", transferAmount.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
			primaryTransactionDao.save(primaryTransaction);
		} else {
			TransferAmount.requireSufficientFunds(savingsAccount.getAccountBalance(), transferAmount);
			primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().add(transferAmount));
			savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(transferAmount));
			primaryAccountDao.save(primaryAccount);
			savingsAccountDao.save(savingsAccount);

			Date date = new Date();

			SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Between account transfer from "+transferFrom+" to "+transferTo, "Transfer", "Finished", transferAmount.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
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
    public void toSomeoneElseTransfer(Recipient recipient, String accountType, String amount, PrimaryAccount primaryAccount, SavingsAccount savingsAccount) {
        BigDecimal transferAmount = TransferAmount.parse(amount);
        if (recipient == null) {
            throw new InvalidTransferException("Please choose a valid recipient.");
        }

        if ("Primary".equalsIgnoreCase(accountType)) {
            primaryAccount = lockForUpdate(PrimaryAccount.class, primaryAccount, primaryAccount.getId());
            TransferAmount.requireSufficientFunds(primaryAccount.getAccountBalance(), transferAmount);
            primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(transferAmount));
            primaryAccountDao.save(primaryAccount);

            Date date = new Date();

            PrimaryTransaction primaryTransaction = new PrimaryTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", transferAmount.doubleValue(), primaryAccount.getAccountBalance(), primaryAccount);
            primaryTransactionDao.save(primaryTransaction);
        } else if ("Savings".equalsIgnoreCase(accountType)) {
            savingsAccount = lockForUpdate(SavingsAccount.class, savingsAccount, savingsAccount.getId());
            TransferAmount.requireSufficientFunds(savingsAccount.getAccountBalance(), transferAmount);
            savingsAccount.setAccountBalance(savingsAccount.getAccountBalance().subtract(transferAmount));
            savingsAccountDao.save(savingsAccount);

            Date date = new Date();

            SavingsTransaction savingsTransaction = new SavingsTransaction(date, "Transfer to recipient "+recipient.getName(), "Transfer", "Finished", transferAmount.doubleValue(), savingsAccount.getAccountBalance(), savingsAccount);
            savingsTransactionDao.save(savingsTransaction);
        } else {
            throw new InvalidTransferException("Please select the account to transfer from.");
        }
    }

    private <T> T lockForUpdate(Class<T> type, T account, Long id) {
        if (entityManager.contains(account)) {
            entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
            return account;
        }
        T locked = entityManager.find(type, id, LockModeType.PESSIMISTIC_WRITE);
        if (locked == null) {
            throw new InvalidTransferException("Account not found.");
        }
        return locked;
    }
}
