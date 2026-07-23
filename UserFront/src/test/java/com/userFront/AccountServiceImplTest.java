package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.AccountServiceImpl;

/**
 * Unit tests for {@link AccountServiceImpl} money-movement operations
 * (deposit / withdraw). DAOs and collaborating services are mocked so the
 * balance arithmetic and transaction-record creation can be asserted in
 * isolation, without a database.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

	@Mock
	private PrimaryAccountDao primaryAccountDao;

	@Mock
	private SavingsAccountDao savingsAccountDao;

	@Mock
	private UserService userService;

	@Mock
	private TransactionService transactionService;

	@InjectMocks
	private AccountServiceImpl accountService;

	private User user;
	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;
	private Principal principal;

	@Before
	public void setUp() {
		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountNumber(11223301);
		primaryAccount.setAccountBalance(new BigDecimal("500.00"));

		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountNumber(11223302);
		savingsAccount.setAccountBalance(new BigDecimal("300.00"));

		user = new User();
		user.setUsername("john");
		user.setPrimaryAccount(primaryAccount);
		user.setSavingsAccount(savingsAccount);

		principal = new Principal() {
			@Override
			public String getName() {
				return "john";
			}
		};

		when(userService.findByUsername("john")).thenReturn(user);
	}

	@Test
	public void depositToPrimaryIncreasesBalanceAndRecordsTransaction() {
		accountService.deposit("Primary", 200.0, principal);

		assertEquals(0, primaryAccount.getAccountBalance().compareTo(new BigDecimal("700.00")));
		verify(primaryAccountDao).save(primaryAccount);

		ArgumentCaptor<PrimaryTransaction> captor = ArgumentCaptor.forClass(PrimaryTransaction.class);
		verify(transactionService).savePrimaryDepositTransaction(captor.capture());

		PrimaryTransaction tx = captor.getValue();
		assertEquals(200.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("700.00")));
		assertEquals(primaryAccount, tx.getPrimaryAccount());
	}

	@Test
	public void depositToSavingsIncreasesBalanceAndRecordsTransaction() {
		accountService.deposit("Savings", 150.0, principal);

		assertEquals(0, savingsAccount.getAccountBalance().compareTo(new BigDecimal("450.00")));
		verify(savingsAccountDao).save(savingsAccount);

		ArgumentCaptor<SavingsTransaction> captor = ArgumentCaptor.forClass(SavingsTransaction.class);
		verify(transactionService).saveSavingsDepositTransaction(captor.capture());

		SavingsTransaction tx = captor.getValue();
		assertEquals(150.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("450.00")));
		assertEquals(savingsAccount, tx.getSavingsAccount());
	}

	@Test
	public void withdrawFromPrimaryDecreasesBalanceAndRecordsTransaction() {
		accountService.withdraw("Primary", 120.0, principal);

		assertEquals(0, primaryAccount.getAccountBalance().compareTo(new BigDecimal("380.00")));
		verify(primaryAccountDao).save(primaryAccount);

		ArgumentCaptor<PrimaryTransaction> captor = ArgumentCaptor.forClass(PrimaryTransaction.class);
		verify(transactionService).savePrimaryWithdrawTransaction(captor.capture());

		PrimaryTransaction tx = captor.getValue();
		assertEquals(120.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("380.00")));
	}

	@Test
	public void withdrawFromSavingsDecreasesBalanceAndRecordsTransaction() {
		accountService.withdraw("Savings", 100.0, principal);

		assertEquals(0, savingsAccount.getAccountBalance().compareTo(new BigDecimal("200.00")));
		verify(savingsAccountDao).save(savingsAccount);

		ArgumentCaptor<SavingsTransaction> captor = ArgumentCaptor.forClass(SavingsTransaction.class);
		verify(transactionService).saveSavingsWithdrawTransaction(captor.capture());

		SavingsTransaction tx = captor.getValue();
		assertEquals(100.0, tx.getAmount(), 0.0001);
		assertEquals(0, tx.getAvailableBalance().compareTo(new BigDecimal("200.00")));
	}
}
