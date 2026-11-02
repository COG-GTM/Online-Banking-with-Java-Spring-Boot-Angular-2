package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.userFront.dao.AccountLockDao;
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
import com.userFront.service.UserService;
import com.userFront.service.exception.InsufficientFundsException;
import com.userFront.service.exception.InvalidAmountException;
import com.userFront.service.exception.TransactionRejectedException;

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

	@Mock
	private AccountLockDao accountLockDao;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private PrimaryAccount primary;
	private SavingsAccount savings;
	private Recipient recipient;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		primary = new PrimaryAccount();
		primary.setId(1L);
		primary.setAccountBalance(new BigDecimal("50.00"));
		savings = new SavingsAccount();
		savings.setId(2L);
		savings.setAccountBalance(new BigDecimal("50.00"));
		recipient = new Recipient();
		recipient.setName("alice");

		when(accountLockDao.lockPrimaryAccount(1L)).thenReturn(primary);
		when(accountLockDao.lockSavingsAccount(2L)).thenReturn(savings);
	}

	@Test
	public void betweenAccountsMovesFundsAndLocksPrimaryFirst() {
		transactionService.betweenAccountsTransfer("Savings", "Primary", new BigDecimal("20.00"), primary, savings);

		assertEquals(new BigDecimal("70.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("30.00"), savings.getAccountBalance());
		InOrder order = inOrder(accountLockDao);
		order.verify(accountLockDao).lockPrimaryAccount(1L);
		order.verify(accountLockDao).lockSavingsAccount(2L);
		verify(savingsTransactionDao).save(Mockito.any(SavingsTransaction.class));
	}

	@Test
	public void betweenAccountsRejectsNegativeAmount() {
		try {
			transactionService.betweenAccountsTransfer("Primary", "Savings", new BigDecimal("-100"), primary, savings);
			fail("expected InvalidAmountException");
		} catch (InvalidAmountException expected) {
		}

		assertBalancesUnchanged();
	}

	@Test
	public void betweenAccountsRejectsOverdraft() {
		try {
			transactionService.betweenAccountsTransfer("Primary", "Savings", new BigDecimal("50.01"), primary, savings);
			fail("expected InsufficientFundsException");
		} catch (InsufficientFundsException expected) {
		}

		assertBalancesUnchanged();
	}

	@Test(expected = TransactionRejectedException.class)
	public void betweenAccountsRejectsSameAccount() {
		transactionService.betweenAccountsTransfer("Primary", "Primary", new BigDecimal("1.00"), primary, savings);
	}

	@Test
	public void toSomeoneElseDebitsWithinBalance() {
		transactionService.toSomeoneElseTransfer(recipient, "Primary", new BigDecimal("50.00"), primary, savings);

		assertEquals(new BigDecimal("0.00"), primary.getAccountBalance());
		verify(primaryTransactionDao).save(Mockito.any(PrimaryTransaction.class));
	}

	@Test
	public void toSomeoneElseRejectsNegativeAmount() {
		try {
			transactionService.toSomeoneElseTransfer(recipient, "Primary", new BigDecimal("-1000000"), primary, savings);
			fail("expected InvalidAmountException");
		} catch (InvalidAmountException expected) {
		}

		assertBalancesUnchanged();
	}

	@Test
	public void toSomeoneElseRejectsOverdraft() {
		for (String type : new String[] { "Primary", "Savings" }) {
			try {
				transactionService.toSomeoneElseTransfer(recipient, type, new BigDecimal("1000000"), primary, savings);
				fail("expected InsufficientFundsException for " + type);
			} catch (InsufficientFundsException expected) {
			}
		}

		assertBalancesUnchanged();
	}

	@Test(expected = TransactionRejectedException.class)
	public void toSomeoneElseRejectsUnknownRecipient() {
		transactionService.toSomeoneElseTransfer(null, "Primary", new BigDecimal("1.00"), primary, savings);
	}

	private void assertBalancesUnchanged() {
		assertEquals(new BigDecimal("50.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("50.00"), savings.getAccountBalance());
		verify(primaryAccountDao, never()).save(Mockito.any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(Mockito.any(SavingsAccount.class));
		verifyNoMoreInteractions(primaryTransactionDao, savingsTransactionDao);
	}
}
