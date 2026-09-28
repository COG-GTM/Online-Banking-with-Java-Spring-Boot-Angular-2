package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.InvalidAmountException;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class MoneyMovementTest {

	@Mock
	private UserService userService;
	@Mock
	private PrimaryAccountDao primaryAccountDao;
	@Mock
	private SavingsAccountDao savingsAccountDao;
	@Mock
	private PrimaryTransactionDao primaryTransactionDao;
	@Mock
	private SavingsTransactionDao savingsTransactionDao;
	@Mock
	private TransactionService transactionService;
	@Mock
	private AccountLocker accountLocker;
	@Mock
	private Principal principal;

	@InjectMocks
	private AccountServiceImpl accountService;

	@InjectMocks
	private TransactionServiceImpl transferService;

	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;

	@Before
	public void setUp() {
		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountBalance(new BigDecimal("100.00"));
		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountBalance(new BigDecimal("50.00"));

		User user = new User();
		user.setPrimaryAccount(primaryAccount);
		user.setSavingsAccount(savingsAccount);
		when(principal.getName()).thenReturn("alice");
		when(userService.findByUsername("alice")).thenReturn(user);

		when(accountLocker.lock(any(PrimaryAccount.class))).thenAnswer(returnFirstArgument());
		when(accountLocker.lock(any(SavingsAccount.class))).thenAnswer(returnFirstArgument());
	}

	private static <T> Answer<T> returnFirstArgument() {
		return new Answer<T>() {
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocation) {
				return (T) invocation.getArguments()[0];
			}
		};
	}

	private void assertBalances(String primary, String savings) {
		assertEquals(0, new BigDecimal(primary).compareTo(primaryAccount.getAccountBalance()));
		assertEquals(0, new BigDecimal(savings).compareTo(savingsAccount.getAccountBalance()));
	}

	private void expectRejected(Runnable action) {
		try {
			action.run();
			fail("Expected InvalidAmountException");
		} catch (InvalidAmountException expected) {
		}
		assertBalances("100.00", "50.00");
		verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(any(SavingsAccount.class));
	}

	@Test
	public void negativeWithdrawIsRejected() {
		expectRejected(() -> accountService.withdraw("Primary", "-1000000", principal));
	}

	@Test
	public void overdraftWithdrawIsRejected() {
		expectRejected(() -> accountService.withdraw("Savings", "50.01", principal));
	}

	@Test
	public void negativeDepositIsRejected() {
		expectRejected(() -> accountService.deposit("Primary", "-5", principal));
	}

	@Test
	public void validWithdrawDebitsBalance() {
		accountService.withdraw("Primary", "40", principal);
		assertBalances("60.00", "50.00");
		verify(transactionService).savePrimaryWithdrawTransaction(any(PrimaryTransaction.class));
	}

	@Test
	public void validDepositCreditsBalance() {
		accountService.deposit("Savings", "25.25", principal);
		assertBalances("100.00", "75.25");
		verify(transactionService).saveSavingsDepositTransaction(any(SavingsTransaction.class));
	}

	@Test
	public void negativeBetweenAccountsTransferIsRejected() {
		expectRejected(() -> {
			try {
				transferService.betweenAccountsTransfer("Primary", "Savings", "-1000", primaryAccount, savingsAccount);
			} catch (InvalidAmountException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	public void overdraftBetweenAccountsTransferIsRejected() {
		expectRejected(() -> {
			try {
				transferService.betweenAccountsTransfer("Savings", "Primary", "50.01", primaryAccount, savingsAccount);
			} catch (InvalidAmountException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	public void validBetweenAccountsTransferMovesFunds() throws Exception {
		transferService.betweenAccountsTransfer("Primary", "Savings", "30", primaryAccount, savingsAccount);
		assertBalances("70.00", "80.00");
		verify(primaryTransactionDao).save(any(PrimaryTransaction.class));
	}

	@Test
	public void negativeTransferToSomeoneElseIsRejected() {
		Recipient recipient = new Recipient();
		expectRejected(() -> transferService.toSomeoneElseTransfer(recipient, "Primary", "-1000000", primaryAccount, savingsAccount));
	}

	@Test
	public void overdraftTransferToSomeoneElseIsRejected() {
		Recipient recipient = new Recipient();
		expectRejected(() -> transferService.toSomeoneElseTransfer(recipient, "Savings", "1000", primaryAccount, savingsAccount));
	}

	@Test
	public void validTransferToSomeoneElseDebitsBalance() {
		Recipient recipient = new Recipient();
		recipient.setName("bob");
		transferService.toSomeoneElseTransfer(recipient, "Savings", "20", primaryAccount, savingsAccount);
		assertBalances("100.00", "30.00");
		verify(savingsTransactionDao).save(any(SavingsTransaction.class));
	}
}
