package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
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
import com.userFront.service.UserServiceImpl.AccountServiceImpl;

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
		primaryAccount.setAccountBalance(new BigDecimal("100.00"));

		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountBalance(new BigDecimal("100.00"));

		user = new User();
		user.setUsername("john");
		user.setPrimaryAccount(primaryAccount);
		user.setSavingsAccount(savingsAccount);

		principal = new Principal() {
			public String getName() {
				return "john";
			}
		};
	}

	@Test
	public void deposit_primaryAccount_increasesBalance() {
		when(userService.findByUsername("john")).thenReturn(user);

		accountService.deposit("Primary", 50.0, principal);

		assertEquals(new BigDecimal("150.00"), primaryAccount.getAccountBalance());
		verify(primaryAccountDao).save(primaryAccount);
		verify(transactionService).savePrimaryDepositTransaction(any(PrimaryTransaction.class));
	}

	@Test
	public void deposit_savingsAccount_increasesBalance() {
		when(userService.findByUsername("john")).thenReturn(user);

		accountService.deposit("Savings", 50.0, principal);

		assertEquals(new BigDecimal("150.00"), savingsAccount.getAccountBalance());
		verify(savingsAccountDao).save(savingsAccount);
		verify(transactionService).saveSavingsDepositTransaction(any(SavingsTransaction.class));
	}

	@Test
	public void withdraw_primaryAccount_decreasesBalance() {
		when(userService.findByUsername("john")).thenReturn(user);

		accountService.withdraw("Primary", 40.0, principal);

		assertEquals(new BigDecimal("60.00"), primaryAccount.getAccountBalance());
		verify(primaryAccountDao).save(primaryAccount);
		verify(transactionService).savePrimaryWithdrawTransaction(any(PrimaryTransaction.class));
	}

	@Test
	public void withdraw_savingsAccount_decreasesBalance() {
		when(userService.findByUsername("john")).thenReturn(user);

		accountService.withdraw("Savings", 40.0, principal);

		assertEquals(new BigDecimal("60.00"), savingsAccount.getAccountBalance());
		verify(savingsAccountDao).save(savingsAccount);
		verify(transactionService).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
	}

	@Test
	public void createPrimaryAccount_returnsNewAccount() {
		PrimaryAccount saved = new PrimaryAccount();
		saved.setAccountBalance(new BigDecimal("0.0"));
		when(primaryAccountDao.findByAccountNumber(org.mockito.Matchers.anyInt())).thenReturn(saved);

		PrimaryAccount result = accountService.createPrimaryAccount();

		assertNotNull(result);
		assertEquals(saved, result);

		ArgumentCaptor<PrimaryAccount> captor = ArgumentCaptor.forClass(PrimaryAccount.class);
		verify(primaryAccountDao).save(captor.capture());
		assertEquals(0, captor.getValue().getAccountBalance().compareTo(BigDecimal.ZERO));
	}

	@Test
	public void createSavingsAccount_returnsNewAccount() {
		SavingsAccount saved = new SavingsAccount();
		saved.setAccountBalance(new BigDecimal("0.0"));
		when(savingsAccountDao.findByAccountNumber(org.mockito.Matchers.anyInt())).thenReturn(saved);

		SavingsAccount result = accountService.createSavingsAccount();

		assertNotNull(result);
		assertEquals(saved, result);

		ArgumentCaptor<SavingsAccount> captor = ArgumentCaptor.forClass(SavingsAccount.class);
		verify(savingsAccountDao).save(captor.capture());
		assertEquals(0, captor.getValue().getAccountBalance().compareTo(BigDecimal.ZERO));
	}
}
