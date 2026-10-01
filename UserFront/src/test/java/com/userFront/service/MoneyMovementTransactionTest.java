package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
import com.userFront.service.UserServiceImpl.AccountServiceImpl;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

public class MoneyMovementTransactionTest {

	private AnnotationConfigApplicationContext context;
	private CountingTransactionManager txManager;
	private List<String> saves;
	private TransactionService transactionService;
	private AccountService accountService;
	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;

	@Before
	public void setUp() {
		context = new AnnotationConfigApplicationContext(Config.class);
		txManager = context.getBean(CountingTransactionManager.class);
		transactionService = context.getBean(TransactionService.class);
		accountService = context.getBean(AccountService.class);
		saves = new ArrayList<String>();

		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountBalance(new BigDecimal("100.00"));
		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountBalance(new BigDecimal("100.00"));
		User user = new User();
		user.setPrimaryAccount(primaryAccount);
		user.setSavingsAccount(savingsAccount);
		when(context.getBean(UserService.class).findByUsername("alice")).thenReturn(user);

		recordSave(context.getBean(PrimaryAccountDao.class).save(any(PrimaryAccount.class)), "primaryAccount");
		recordSave(context.getBean(SavingsAccountDao.class).save(any(SavingsAccount.class)), "savingsAccount");
		recordSave(context.getBean(PrimaryTransactionDao.class).save(any(PrimaryTransaction.class)), "primaryTransaction");
		recordSave(context.getBean(SavingsTransactionDao.class).save(any(SavingsTransaction.class)), "savingsTransaction");
	}

	@After
	public void tearDown() {
		context.close();
	}

	@Test
	public void betweenAccountsTransferCommitsAllWritesOnce() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "25", primaryAccount, savingsAccount);

		assertEquals(3, saves.size());
		assertSingleCommit();
	}

	@Test
	public void toSomeoneElseTransferCommitsAllWritesOnce() {
		Recipient recipient = new Recipient();
		recipient.setName("bob");

		transactionService.toSomeoneElseTransfer(recipient, "Savings", "25", primaryAccount, savingsAccount);

		assertEquals(2, saves.size());
		assertSingleCommit();
	}

	@Test
	public void depositAndWithdrawJoinOneTransactionAcrossServices() {
		accountService.deposit("Primary", 10, principal("alice"));
		assertEquals(2, saves.size());
		assertSingleCommit();

		accountService.withdraw("Savings", 10, principal("alice"));
		assertEquals(4, saves.size());
		assertEquals(2, txManager.begins);
		assertEquals(2, txManager.commits);
	}

	@Test
	public void failedLedgerInsertRollsBackBalanceUpdates() throws Exception {
		doThrow(new IllegalStateException("insert failed"))
				.when(context.getBean(SavingsTransactionDao.class)).save(any(SavingsTransaction.class));

		try {
			transactionService.betweenAccountsTransfer("Savings", "Primary", "25", primaryAccount, savingsAccount);
			fail("expected failure");
		} catch (IllegalStateException expected) {
		}

		assertEquals(1, txManager.begins);
		assertEquals(0, txManager.commits);
		assertEquals(1, txManager.rollbacks);
	}

	private void assertSingleCommit() {
		assertEquals(1, txManager.begins);
		assertEquals(1, txManager.commits);
		assertEquals(0, txManager.rollbacks);
	}

	private <T> void recordSave(T stubbedCall, final String name) {
		when(stubbedCall).thenAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				assertTrue(name + " saved outside a transaction",
						TransactionSynchronizationManager.isActualTransactionActive());
				saves.add(name);
				return invocation.getArguments()[0];
			}
		});
	}

	private static Principal principal(final String name) {
		return new Principal() {
			public String getName() {
				return name;
			}
		};
	}

	@Configuration
	@EnableTransactionManagement
	static class Config {

		@Bean
		public CountingTransactionManager transactionManager() {
			return new CountingTransactionManager();
		}

		@Bean
		public TransactionService transactionService() {
			return new TransactionServiceImpl();
		}

		@Bean
		public AccountService accountService() {
			return new AccountServiceImpl();
		}

		@Bean
		public UserService userService() {
			return mock(UserService.class);
		}

		@Bean
		public PrimaryAccountDao primaryAccountDao() {
			return mock(PrimaryAccountDao.class);
		}

		@Bean
		public SavingsAccountDao savingsAccountDao() {
			return mock(SavingsAccountDao.class);
		}

		@Bean
		public PrimaryTransactionDao primaryTransactionDao() {
			return mock(PrimaryTransactionDao.class);
		}

		@Bean
		public SavingsTransactionDao savingsTransactionDao() {
			return mock(SavingsTransactionDao.class);
		}

		@Bean
		public RecipientDao recipientDao() {
			return mock(RecipientDao.class);
		}
	}

	static class CountingTransactionManager extends AbstractPlatformTransactionManager implements PlatformTransactionManager {

		int begins;
		int commits;
		int rollbacks;

		@Override
		protected Object doGetTransaction() {
			return new Object();
		}

		@Override
		protected boolean isExistingTransaction(Object transaction) {
			return TransactionSynchronizationManager.isActualTransactionActive();
		}

		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) {
			begins++;
		}

		@Override
		protected void doCommit(DefaultTransactionStatus status) {
			commits++;
		}

		@Override
		protected void doRollback(DefaultTransactionStatus status) {
			rollbacks++;
		}
	}
}
