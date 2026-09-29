package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

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
import com.userFront.service.InvalidTransferException;
import com.userFront.service.UserService;

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
	private EntityManager entityManager;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private PrimaryAccount primary;
	private SavingsAccount savings;
	private Recipient recipient;

	@Before
	public void setUp() {
		primary = new PrimaryAccount();
		primary.setId(1L);
		primary.setAccountBalance(new BigDecimal("100.00"));
		savings = new SavingsAccount();
		savings.setId(2L);
		savings.setAccountBalance(new BigDecimal("50.00"));
		recipient = new Recipient();
		recipient.setName("alice");
		when(entityManager.contains(any())).thenReturn(true);
	}

	@Test
	public void primaryToSavingsMovesFundsAndLocksBothAccounts() throws Exception {
		transactionService.betweenAccountsTransfer("Primary", "Savings", "40.25", primary, savings);

		assertEquals(new BigDecimal("59.75"), primary.getAccountBalance());
		assertEquals(new BigDecimal("90.25"), savings.getAccountBalance());
		verify(entityManager).refresh(primary, LockModeType.PESSIMISTIC_WRITE);
		verify(entityManager).refresh(savings, LockModeType.PESSIMISTIC_WRITE);
		verify(primaryTransactionDao).save(any(PrimaryTransaction.class));
	}

	@Test
	public void savingsToPrimaryAllowsEntireBalance() throws Exception {
		transactionService.betweenAccountsTransfer("Savings", "Primary", "50", primary, savings);

		assertEquals(new BigDecimal("150.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("0.00"), savings.getAccountBalance());
		verify(savingsTransactionDao).save(any(SavingsTransaction.class));
	}

	@Test
	public void negativeBetweenAccountsAmountIsRejectedWithoutSideEffects() throws Exception {
		assertBetweenAccountsRejected("Primary", "Savings", "-1000000");
	}

	@Test
	public void betweenAccountsOverdraftIsRejected() throws Exception {
		assertBetweenAccountsRejected("Primary", "Savings", "100.01");
		assertBetweenAccountsRejected("Savings", "Primary", "50.01");
	}

	@Test
	public void unknownDirectionIsRejected() throws Exception {
		assertBetweenAccountsRejected("Primary", "Primary", "10");
	}

	@Test
	public void toSomeoneElseDebitsSourceAccount() {
		transactionService.toSomeoneElseTransfer(recipient, "Savings", "20", primary, savings);

		assertEquals(new BigDecimal("30.00"), savings.getAccountBalance());
		assertEquals(new BigDecimal("100.00"), primary.getAccountBalance());
		verify(savingsTransactionDao).save(any(SavingsTransaction.class));
	}

	@Test
	public void negativeToSomeoneElseAmountIsRejected() {
		assertToSomeoneElseRejected(recipient, "Primary", "-500");
	}

	@Test
	public void toSomeoneElseOverdraftIsRejected() {
		assertToSomeoneElseRejected(recipient, "Primary", "100.01");
	}

	@Test
	public void toSomeoneElseRequiresRecipientAndAccountType() {
		assertToSomeoneElseRejected(null, "Primary", "10");
		assertToSomeoneElseRejected(recipient, "Checking", "10");
	}

	private void assertBetweenAccountsRejected(String from, String to, String amount) throws Exception {
		try {
			transactionService.betweenAccountsTransfer(from, to, amount, primary, savings);
			fail("Expected InvalidTransferException for " + from + "->" + to + " " + amount);
		} catch (InvalidTransferException expected) {
		}
		assertUnchanged();
	}

	private void assertToSomeoneElseRejected(Recipient target, String accountType, String amount) {
		try {
			transactionService.toSomeoneElseTransfer(target, accountType, amount, primary, savings);
			fail("Expected InvalidTransferException for " + accountType + " " + amount);
		} catch (InvalidTransferException expected) {
		}
		assertUnchanged();
	}

	private void assertUnchanged() {
		assertEquals(new BigDecimal("100.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("50.00"), savings.getAccountBalance());
		verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(any(SavingsAccount.class));
		verify(primaryTransactionDao, never()).save(any(PrimaryTransaction.class));
		verify(savingsTransactionDao, never()).save(any(SavingsTransaction.class));
	}
}
