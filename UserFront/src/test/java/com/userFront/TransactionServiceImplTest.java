package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

/**
 * Repository-backed tests for {@link TransactionServiceImpl} transfer logic.
 *
 * Uses {@code @DataJpaTest} with an in-memory H2 database (test scope only) so
 * the real Spring Data repositories persist entities. The service under test is
 * instantiated directly and its DAO dependencies are injected via reflection,
 * letting us assert both the resulting account balances and that the correct
 * PrimaryTransaction / SavingsTransaction ledger rows are actually written.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
public class TransactionServiceImplTest {

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;

	@Autowired
	private SavingsTransactionDao savingsTransactionDao;

	private TransactionServiceImpl transactionService;

	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;

	@Before
	public void setUp() {
		transactionService = new TransactionServiceImpl();
		ReflectionTestUtils.setField(transactionService, "primaryAccountDao", primaryAccountDao);
		ReflectionTestUtils.setField(transactionService, "savingsAccountDao", savingsAccountDao);
		ReflectionTestUtils.setField(transactionService, "primaryTransactionDao", primaryTransactionDao);
		ReflectionTestUtils.setField(transactionService, "savingsTransactionDao", savingsTransactionDao);

		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountNumber(11223401);
		primaryAccount.setAccountBalance(new BigDecimal("1000.00"));
		primaryAccount = primaryAccountDao.save(primaryAccount);

		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountNumber(11223402);
		savingsAccount.setAccountBalance(new BigDecimal("400.00"));
		savingsAccount = savingsAccountDao.save(savingsAccount);
	}

	@Test
	public void betweenAccountsTransferPrimaryToSavingsMovesAmountAndRecordsPrimaryTransaction() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "250", primaryAccount, savingsAccount);

		assertEquals(0, primaryAccountDao.findByAccountNumber(11223401).getAccountBalance()
				.compareTo(new BigDecimal("750.00")));
		assertEquals(0, savingsAccountDao.findByAccountNumber(11223402).getAccountBalance()
				.compareTo(new BigDecimal("650.00")));

		List<PrimaryTransaction> primaryTransactions = primaryTransactionDao.findAll();
		assertEquals(1, primaryTransactions.size());
		PrimaryTransaction tx = primaryTransactions.get(0);
		assertEquals(250.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("750.00")));
		assertTrue(tx.getDescription().contains("Primary"));
		assertTrue(tx.getDescription().contains("Savings"));

		assertTrue(savingsTransactionDao.findAll().isEmpty());
	}

	@Test
	public void betweenAccountsTransferSavingsToPrimaryMovesAmountAndRecordsSavingsTransaction() throws Exception {
		transactionService.betweenAccountsTransfer("Savings", "Primary", "100", primaryAccount, savingsAccount);

		assertEquals(0, primaryAccountDao.findByAccountNumber(11223401).getAccountBalance()
				.compareTo(new BigDecimal("1100.00")));
		assertEquals(0, savingsAccountDao.findByAccountNumber(11223402).getAccountBalance()
				.compareTo(new BigDecimal("300.00")));

		List<SavingsTransaction> savingsTransactions = savingsTransactionDao.findAll();
		assertEquals(1, savingsTransactions.size());
		SavingsTransaction tx = savingsTransactions.get(0);
		assertEquals(100.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("300.00")));

		assertTrue(primaryTransactionDao.findAll().isEmpty());
	}

	@Test
	public void betweenAccountsTransferWithInvalidDirectionThrowsAndMovesNoMoney() {
		try {
			transactionService.betweenAccountsTransfer("Primary", "Primary", "50", primaryAccount, savingsAccount);
			fail("Expected an exception for an invalid transfer direction");
		} catch (Exception e) {
			assertEquals("Invalid Transfer", e.getMessage());
		}

		assertEquals(0, primaryAccountDao.findByAccountNumber(11223401).getAccountBalance()
				.compareTo(new BigDecimal("1000.00")));
		assertEquals(0, savingsAccountDao.findByAccountNumber(11223402).getAccountBalance()
				.compareTo(new BigDecimal("400.00")));
		assertTrue(primaryTransactionDao.findAll().isEmpty());
		assertTrue(savingsTransactionDao.findAll().isEmpty());
	}

	@Test
	public void toSomeoneElseTransferFromPrimaryDecreasesBalanceAndRecordsPrimaryTransaction() {
		Recipient recipient = new Recipient();
		recipient.setName("Alice");

		transactionService.toSomeoneElseTransfer(recipient, "Primary", "300", primaryAccount, savingsAccount);

		assertEquals(0, primaryAccountDao.findByAccountNumber(11223401).getAccountBalance()
				.compareTo(new BigDecimal("700.00")));

		List<PrimaryTransaction> primaryTransactions = primaryTransactionDao.findAll();
		assertEquals(1, primaryTransactions.size());
		PrimaryTransaction tx = primaryTransactions.get(0);
		assertEquals(300.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("700.00")));
		assertTrue(tx.getDescription().contains("Alice"));

		assertEquals(0, savingsAccountDao.findByAccountNumber(11223402).getAccountBalance()
				.compareTo(new BigDecimal("400.00")));
		assertTrue(savingsTransactionDao.findAll().isEmpty());
	}

	@Test
	public void toSomeoneElseTransferFromSavingsDecreasesBalanceAndRecordsSavingsTransaction() {
		Recipient recipient = new Recipient();
		recipient.setName("Bob");

		transactionService.toSomeoneElseTransfer(recipient, "Savings", "120", primaryAccount, savingsAccount);

		assertEquals(0, savingsAccountDao.findByAccountNumber(11223402).getAccountBalance()
				.compareTo(new BigDecimal("280.00")));

		List<SavingsTransaction> savingsTransactions = savingsTransactionDao.findAll();
		assertEquals(1, savingsTransactions.size());
		SavingsTransaction tx = savingsTransactions.get(0);
		assertEquals(120.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("280.00")));
		assertTrue(tx.getDescription().contains("Bob"));

		assertEquals(0, primaryAccountDao.findByAccountNumber(11223401).getAccountBalance()
				.compareTo(new BigDecimal("1000.00")));
		assertTrue(primaryTransactionDao.findAll().isEmpty());
	}
}
