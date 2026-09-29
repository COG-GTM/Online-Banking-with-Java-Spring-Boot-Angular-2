package com.userFront.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConcurrentBalanceUpdateTests {

	private static final int THREADS = 20;
	private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

	@Autowired
	private AccountService accountService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	private User user;

	@Before
	public void setUp() {
		PrimaryAccount primaryAccount = accountService.createPrimaryAccount();
		primaryAccount.setAccountBalance(INITIAL_BALANCE);
		primaryAccountDao.save(primaryAccount);

		SavingsAccount savingsAccount = accountService.createSavingsAccount();
		savingsAccount.setAccountBalance(INITIAL_BALANCE);
		savingsAccountDao.save(savingsAccount);

		String username = "concurrency-" + UUID.randomUUID();
		User newUser = new User();
		newUser.setUsername(username);
		newUser.setPassword("password");
		newUser.setEmail(username + "@example.com");
		newUser.setPrimaryAccount(primaryAccount);
		newUser.setSavingsAccount(savingsAccount);
		user = userDao.save(newUser);
	}

	@After
	public void tearDown() {
		Long primaryId = user.getPrimaryAccount().getId();
		Long savingsId = user.getSavingsAccount().getId();
		userDao.delete(user.getUserId());
		primaryAccountDao.delete(primaryId);
		savingsAccountDao.delete(savingsId);
	}

	@Test
	public void concurrentWithdrawalsAreAllApplied() throws Exception {
		Principal principal = user::getUsername;

		runConcurrently(() -> accountService.withdraw("Primary", 10, principal));

		assertEquals(0, new BigDecimal("800.00").compareTo(primaryBalance()));
	}

	@Test
	public void concurrentDepositsAreAllApplied() throws Exception {
		Principal principal = user::getUsername;

		runConcurrently(() -> accountService.deposit("Savings", 10, principal));

		assertEquals(0, new BigDecimal("1200.00").compareTo(savingsBalance()));
	}

	@Test
	public void concurrentBetweenAccountTransfersConserveFunds() throws Exception {
		runConcurrently(() -> {
			User current = userService.findByUsername(user.getUsername());
			transactionService.betweenAccountsTransfer("Primary", "Savings", "10", current.getPrimaryAccount(),
					current.getSavingsAccount());
		});

		assertEquals(0, new BigDecimal("800.00").compareTo(primaryBalance()));
		assertEquals(0, new BigDecimal("1200.00").compareTo(savingsBalance()));
	}

	private BigDecimal primaryBalance() {
		return primaryAccountDao.findOne(user.getPrimaryAccount().getId()).getAccountBalance();
	}

	private BigDecimal savingsBalance() {
		return savingsAccountDao.findOne(user.getSavingsAccount().getId()).getAccountBalance();
	}

	private interface BalanceOperation {
		void run() throws Exception;
	}

	private void runConcurrently(BalanceOperation operation) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Void>> futures = new ArrayList<>();
		try {
			for (int i = 0; i < THREADS; i++) {
				Callable<Void> task = () -> {
					start.await();
					operation.run();
					return null;
				};
				futures.add(executor.submit(task));
			}
			start.countDown();
			for (Future<Void> future : futures) {
				future.get();
			}
		} finally {
			executor.shutdownNow();
		}
	}
}
