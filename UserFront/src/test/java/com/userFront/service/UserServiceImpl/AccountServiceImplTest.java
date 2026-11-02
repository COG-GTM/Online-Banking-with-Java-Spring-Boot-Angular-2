package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.userFront.dao.AccountLockDao;
import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;
import com.userFront.service.exception.InsufficientFundsException;
import com.userFront.service.exception.InvalidAmountException;
import com.userFront.service.exception.TransactionRejectedException;

public class AccountServiceImplTest {

	@Mock
	private PrimaryAccountDao primaryAccountDao;

	@Mock
	private SavingsAccountDao savingsAccountDao;

	@Mock
	private AccountLockDao accountLockDao;

	@Mock
	private UserService userService;

	@Mock
	private TransactionService transactionService;

	@InjectMocks
	private AccountServiceImpl accountService;

	private PrimaryAccount primary;
	private SavingsAccount savings;
	private Principal principal;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		primary = new PrimaryAccount();
		primary.setId(1L);
		primary.setAccountBalance(new BigDecimal("50.00"));
		savings = new SavingsAccount();
		savings.setId(2L);
		savings.setAccountBalance(new BigDecimal("50.00"));

		User user = new User();
		user.setUsername("mallory");
		user.setPrimaryAccount(primary);
		user.setSavingsAccount(savings);

		principal = mock(Principal.class);
		when(principal.getName()).thenReturn("mallory");
		when(userService.findByUsername("mallory")).thenReturn(user);
		when(accountLockDao.lockPrimaryAccount(1L)).thenReturn(primary);
		when(accountLockDao.lockSavingsAccount(2L)).thenReturn(savings);
	}

	@Test
	public void withdrawWithinBalanceDebitsLockedAccount() {
		accountService.withdraw("Primary", new BigDecimal("20.00"), principal);

		assertEquals(new BigDecimal("30.00"), primary.getAccountBalance());
		verify(accountLockDao).lockPrimaryAccount(1L);
		verify(primaryAccountDao).save(primary);
		verify(transactionService).savePrimaryWithdrawTransaction(Mockito.any(PrimaryTransaction.class));
	}

	@Test
	public void withdrawOfEntireBalanceIsAllowed() {
		accountService.withdraw("Savings", new BigDecimal("50.00"), principal);

		assertEquals(new BigDecimal("0.00"), savings.getAccountBalance());
		verify(transactionService).saveSavingsWithdrawTransaction(Mockito.any(SavingsTransaction.class));
	}

	@Test
	public void negativeWithdrawIsRejectedAndBalanceUnchanged() {
		try {
			accountService.withdraw("Primary", new BigDecimal("-1000000"), principal);
			org.junit.Assert.fail("expected InvalidAmountException");
		} catch (InvalidAmountException expected) {
		}

		assertEquals(new BigDecimal("50.00"), primary.getAccountBalance());
		verifyNoMoreInteractions(primaryAccountDao, transactionService);
	}

	@Test
	public void overdraftWithdrawIsRejectedAndBalanceUnchanged() {
		for (String type : new String[] { "Primary", "Savings" }) {
			try {
				accountService.withdraw(type, new BigDecimal("1000000"), principal);
				org.junit.Assert.fail("expected InsufficientFundsException for " + type);
			} catch (InsufficientFundsException expected) {
			}
		}

		assertEquals(new BigDecimal("50.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("50.00"), savings.getAccountBalance());
		verify(primaryAccountDao, never()).save(Mockito.any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(Mockito.any(SavingsAccount.class));
		verifyNoMoreInteractions(transactionService);
	}

	@Test
	public void negativeOrZeroDepositIsRejected() {
		for (String raw : new String[] { "-5", "0" }) {
			try {
				accountService.deposit("Primary", new BigDecimal(raw), principal);
				org.junit.Assert.fail("expected InvalidAmountException for " + raw);
			} catch (InvalidAmountException expected) {
			}
		}

		assertEquals(new BigDecimal("50.00"), primary.getAccountBalance());
		verifyNoMoreInteractions(primaryAccountDao, transactionService);
	}

	@Test
	public void depositCreditsLockedAccount() {
		accountService.deposit("Primary", new BigDecimal("10.25"), principal);

		assertEquals(new BigDecimal("60.25"), primary.getAccountBalance());
		verify(accountLockDao).lockPrimaryAccount(1L);
		verify(transactionService).savePrimaryDepositTransaction(Mockito.any(PrimaryTransaction.class));
	}

	@Test(expected = TransactionRejectedException.class)
	public void unknownAccountTypeIsRejected() {
		accountService.withdraw("Checking", new BigDecimal("1.00"), principal);
	}
}
