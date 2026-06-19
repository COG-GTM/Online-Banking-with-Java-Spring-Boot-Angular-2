package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

	@Mock
	private UserService userService;

	@Mock
	private PrimaryTransactionDao primaryTransactionDao;

	@Mock
	private SavingsTransactionDao savingsTransactionDao;

	@Mock
	private PrimaryAccountDao primaryAccountDao;

	@Mock
	private SavingsAccountDao savingsAccountDao;

	@Mock
	private RecipientDao recipientDao;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;

	@Before
	public void setUp() {
		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountBalance(new BigDecimal("100.00"));

		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountBalance(new BigDecimal("100.00"));
	}

	@Test
	public void betweenAccountsTransfer_primaryToSavings_movesMoneyCorrectly() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "30", primaryAccount, savingsAccount);

		assertEquals(new BigDecimal("70.00"), primaryAccount.getAccountBalance());
		assertEquals(new BigDecimal("130.00"), savingsAccount.getAccountBalance());
		verify(primaryAccountDao).save(primaryAccount);
		verify(savingsAccountDao).save(savingsAccount);
		verify(primaryTransactionDao).save(any(PrimaryTransaction.class));
	}

	@Test
	public void betweenAccountsTransfer_savingsToPrimary_movesMoneyCorrectly() throws Exception {
		transactionService.betweenAccountsTransfer("Savings", "Primary", "30", primaryAccount, savingsAccount);

		assertEquals(new BigDecimal("130.00"), primaryAccount.getAccountBalance());
		assertEquals(new BigDecimal("70.00"), savingsAccount.getAccountBalance());
		verify(primaryAccountDao).save(primaryAccount);
		verify(savingsAccountDao).save(savingsAccount);
		verify(savingsTransactionDao).save(any(SavingsTransaction.class));
	}

	@Test
	public void betweenAccountsTransfer_invalidDirection_throwsException() {
		try {
			transactionService.betweenAccountsTransfer("Primary", "Primary", "30", primaryAccount, savingsAccount);
			fail("Expected an Exception for invalid transfer direction");
		} catch (Exception e) {
			assertEquals("Invalid Transfer", e.getMessage());
		}
	}

	@Test
	public void toSomeoneElseTransfer_deductsFromCorrectAccount() {
		Recipient recipient = new Recipient();
		recipient.setName("Jane");

		transactionService.toSomeoneElseTransfer(recipient, "Primary", "25", primaryAccount, savingsAccount);

		assertEquals(new BigDecimal("75.00"), primaryAccount.getAccountBalance());
		assertEquals(new BigDecimal("100.00"), savingsAccount.getAccountBalance());
		verify(primaryAccountDao).save(primaryAccount);
	}

	@Test
	public void toSomeoneElseTransfer_createsTransactionRecord() {
		Recipient recipient = new Recipient();
		recipient.setName("Jane");

		transactionService.toSomeoneElseTransfer(recipient, "Savings", "25", primaryAccount, savingsAccount);

		assertEquals(new BigDecimal("75.00"), savingsAccount.getAccountBalance());
		verify(savingsAccountDao).save(savingsAccount);
		verify(savingsTransactionDao).save(any(SavingsTransaction.class));
	}

	@Test
	public void saveRecipient_persistsRecipient() {
		Recipient recipient = new Recipient();
		recipient.setName("Jane");
		when(recipientDao.save(recipient)).thenReturn(recipient);

		Recipient result = transactionService.saveRecipient(recipient);

		assertSame(recipient, result);
		verify(recipientDao).save(recipient);
	}

	@Test
	public void deleteRecipientByName_removesRecipient() {
		transactionService.deleteRecipientByName("Jane");

		verify(recipientDao).deleteByName("Jane");
	}

	@Test
	public void findPrimaryTransactionList_returnsCorrectData() {
		List<PrimaryTransaction> transactions = new ArrayList<PrimaryTransaction>();
		transactions.add(new PrimaryTransaction());
		primaryAccount.setPrimaryTransactionList(transactions);

		User user = new User();
		user.setPrimaryAccount(primaryAccount);
		when(userService.findByUsername("john")).thenReturn(user);

		List<PrimaryTransaction> result = transactionService.findPrimaryTransactionList("john");

		assertEquals(transactions, result);
	}

	@Test
	public void findSavingsTransactionList_returnsCorrectData() {
		List<SavingsTransaction> transactions = new ArrayList<SavingsTransaction>();
		transactions.add(new SavingsTransaction());
		savingsAccount.setSavingsTransactionList(transactions);

		User user = new User();
		user.setSavingsAccount(savingsAccount);
		when(userService.findByUsername("john")).thenReturn(user);

		List<SavingsTransaction> result = transactionService.findSavingsTransactionList("john");

		assertEquals(transactions, result);
	}

	@Test
	public void findRecipientList_filtersByPrincipalUsername() {
		User john = new User();
		john.setUsername("john");
		User jane = new User();
		jane.setUsername("jane");

		Recipient r1 = new Recipient();
		r1.setName("R1");
		r1.setUser(john);
		Recipient r2 = new Recipient();
		r2.setName("R2");
		r2.setUser(jane);

		when(recipientDao.findAll()).thenReturn(Arrays.asList(r1, r2));

		Principal principal = new Principal() {
			public String getName() {
				return "john";
			}
		};

		List<Recipient> result = transactionService.findRecipientList(principal);

		assertEquals(1, result.size());
		assertSame(r1, result.get(0));
	}
}
