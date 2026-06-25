package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

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

	@Mock
	private Principal principal;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;

	@Before
	public void setUp() {
		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountNumber(11);
		primaryAccount.setAccountBalance(new BigDecimal("1000.00"));

		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountNumber(22);
		savingsAccount.setAccountBalance(new BigDecimal("500.00"));
	}

	private static Recipient recipientOwnedBy(String username) {
		User owner = new User();
		owner.setUsername(username);
		Recipient recipient = new Recipient();
		recipient.setName("Bob");
		recipient.setUser(owner);
		return recipient;
	}

	// ---------- betweenAccountsTransfer ----------

	@Test
	public void betweenAccountsTransfer_PrimaryToSavings_movesFunds() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "200.00", primaryAccount, savingsAccount);

		assertEquals(0, primaryAccount.getAccountBalance().compareTo(new BigDecimal("800.00")));
		assertEquals(0, savingsAccount.getAccountBalance().compareTo(new BigDecimal("700.00")));
		verify(primaryAccountDao).save(primaryAccount);
		verify(savingsAccountDao).save(savingsAccount);
		verify(primaryTransactionDao, times(1)).save(any(PrimaryTransaction.class));
	}

	@Test
	public void betweenAccountsTransfer_SavingsToPrimary_movesFunds() throws Exception {
		savingsAccount.setAccountBalance(new BigDecimal("1000.00"));
		primaryAccount.setAccountBalance(new BigDecimal("500.00"));

		transactionService.betweenAccountsTransfer("Savings", "Primary", "300.00", primaryAccount, savingsAccount);

		assertEquals(0, primaryAccount.getAccountBalance().compareTo(new BigDecimal("800.00")));
		assertEquals(0, savingsAccount.getAccountBalance().compareTo(new BigDecimal("700.00")));
		verify(primaryAccountDao).save(primaryAccount);
		verify(savingsAccountDao).save(savingsAccount);
		verify(savingsTransactionDao, times(1)).save(any(SavingsTransaction.class));
	}

	@Test(expected = IllegalStateException.class)
	public void betweenAccountsTransfer_insufficientBalance_throwsAndDoesNotPersist() throws Exception {
		primaryAccount.setAccountBalance(new BigDecimal("100.00"));
		try {
			transactionService.betweenAccountsTransfer("Primary", "Savings", "200.00", primaryAccount, savingsAccount);
		} finally {
			verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
			verify(savingsAccountDao, never()).save(any(SavingsAccount.class));
			verify(primaryTransactionDao, never()).save(any(PrimaryTransaction.class));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void betweenAccountsTransfer_negativeAmount_throws() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "-50.00", primaryAccount, savingsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public void betweenAccountsTransfer_zeroAmount_throws() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "0", primaryAccount, savingsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public void betweenAccountsTransfer_nonNumericAmount_throws() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "abc", primaryAccount, savingsAccount);
	}

	// ---------- toSomeoneElseTransfer ----------

	@Test
	public void toSomeoneElseTransfer_primary_movesFunds() {
		Recipient recipient = recipientOwnedBy("alice");

		transactionService.toSomeoneElseTransfer(recipient, "Primary", "100.00", primaryAccount, savingsAccount);

		assertEquals(0, primaryAccount.getAccountBalance().compareTo(new BigDecimal("900.00")));
		verify(primaryAccountDao).save(primaryAccount);
		verify(primaryTransactionDao, times(1)).save(any(PrimaryTransaction.class));
	}

	@Test(expected = IllegalStateException.class)
	public void toSomeoneElseTransfer_insufficientBalance_throwsAndDoesNotPersist() {
		Recipient recipient = recipientOwnedBy("alice");
		primaryAccount.setAccountBalance(new BigDecimal("50.00"));
		try {
			transactionService.toSomeoneElseTransfer(recipient, "Primary", "100.00", primaryAccount, savingsAccount);
		} finally {
			verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
			verify(primaryTransactionDao, never()).save(any(PrimaryTransaction.class));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void toSomeoneElseTransfer_negativeAmount_throws() {
		Recipient recipient = recipientOwnedBy("alice");
		transactionService.toSomeoneElseTransfer(recipient, "Primary", "-10.00", primaryAccount, savingsAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public void toSomeoneElseTransfer_invalidAccountType_throws() {
		Recipient recipient = recipientOwnedBy("alice");
		transactionService.toSomeoneElseTransfer(recipient, "Checking", "10.00", primaryAccount, savingsAccount);
	}

	// ---------- recipient ownership (IDOR) ----------

	@Test
	public void findRecipientByName_owned_returnsRecipient() {
		Recipient recipient = recipientOwnedBy("alice");
		when(principal.getName()).thenReturn("alice");
		when(recipientDao.findByNameAndUser_Username("Bob", "alice")).thenReturn(recipient);

		Recipient result = transactionService.findRecipientByName("Bob", principal);

		assertSame(recipient, result);
	}

	@Test(expected = AccessDeniedException.class)
	public void findRecipientByName_notOwned_throws() {
		// User-scoped lookup returns nothing for a recipient the caller does not own.
		when(principal.getName()).thenReturn("alice");
		when(recipientDao.findByNameAndUser_Username("Bob", "alice")).thenReturn(null);

		transactionService.findRecipientByName("Bob", principal);
	}

	@Test(expected = AccessDeniedException.class)
	public void deleteRecipientByName_notOwned_throwsAndDoesNotDelete() {
		when(principal.getName()).thenReturn("alice");
		when(recipientDao.findByNameAndUser_Username("Bob", "alice")).thenReturn(null);
		try {
			transactionService.deleteRecipientByName("Bob", principal);
		} finally {
			verify(recipientDao, never()).delete(any(Recipient.class));
			verify(recipientDao, never()).deleteByName(any(String.class));
		}
	}

	@Test
	public void deleteRecipientByName_owned_deletes() {
		Recipient recipient = recipientOwnedBy("alice");
		when(principal.getName()).thenReturn("alice");
		when(recipientDao.findByNameAndUser_Username("Bob", "alice")).thenReturn(recipient);

		transactionService.deleteRecipientByName("Bob", principal);

		verify(recipientDao).delete(recipient);
	}
}
