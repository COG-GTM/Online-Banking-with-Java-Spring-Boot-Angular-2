package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.userFront.service.InvalidTransactionException;

public class TransactionServiceImplTest {

	private TransactionServiceImpl service;
	private PrimaryAccountDao primaryAccountDao;
	private SavingsAccountDao savingsAccountDao;
	private PrimaryTransactionDao primaryTransactionDao;
	private SavingsTransactionDao savingsTransactionDao;
	private RecipientDao recipientDao;
	private PrimaryAccount primary;
	private SavingsAccount savings;
	private Recipient recipient;

	@Before
	public void setUp() {
		service = new TransactionServiceImpl();
		primaryAccountDao = mock(PrimaryAccountDao.class);
		savingsAccountDao = mock(SavingsAccountDao.class);
		primaryTransactionDao = mock(PrimaryTransactionDao.class);
		savingsTransactionDao = mock(SavingsTransactionDao.class);
		recipientDao = mock(RecipientDao.class);
		EntityManager entityManager = mock(EntityManager.class);
		when(entityManager.contains(any())).thenReturn(true);

		ReflectionTestUtils.setField(service, "primaryAccountDao", primaryAccountDao);
		ReflectionTestUtils.setField(service, "savingsAccountDao", savingsAccountDao);
		ReflectionTestUtils.setField(service, "primaryTransactionDao", primaryTransactionDao);
		ReflectionTestUtils.setField(service, "savingsTransactionDao", savingsTransactionDao);
		ReflectionTestUtils.setField(service, "recipientDao", recipientDao);
		ReflectionTestUtils.setField(service, "entityManager", entityManager);

		primary = new PrimaryAccount();
		primary.setId(1L);
		primary.setAccountBalance(new BigDecimal("100.00"));
		savings = new SavingsAccount();
		savings.setId(2L);
		savings.setAccountBalance(new BigDecimal("50.00"));
		recipient = new Recipient();
		recipient.setName("bob");
	}

	@Test
	public void toSomeoneElseRejectsNegativeAmountWithoutChangingBalance() {
		expectRejected(() -> service.toSomeoneElseTransfer(recipient, "Primary", "-1000000", primary, savings));
		expectRejected(() -> service.toSomeoneElseTransfer(recipient, "Savings", "-100", primary, savings));

		assertBalances("100.00", "50.00");
		verifyNothingSaved();
	}

	@Test
	public void toSomeoneElseRejectsOverdraft() {
		expectRejected(() -> service.toSomeoneElseTransfer(recipient, "Primary", "100.01", primary, savings));
		expectRejected(() -> service.toSomeoneElseTransfer(recipient, "Savings", "50.01", primary, savings));

		assertBalances("100.00", "50.00");
		verifyNothingSaved();
	}

	@Test
	public void toSomeoneElseRejectsMissingRecipient() {
		expectRejected(() -> service.toSomeoneElseTransfer(null, "Primary", "10", primary, savings));

		assertBalances("100.00", "50.00");
		verifyNothingSaved();
	}

	@Test
	public void toSomeoneElseDebitsValidAmount() {
		service.toSomeoneElseTransfer(recipient, "Primary", "100", primary, savings);

		assertBalances("0.00", "50.00");
		verify(primaryAccountDao).save(primary);
		verify(primaryTransactionDao).save(any(PrimaryTransaction.class));
	}

	@Test
	public void betweenAccountsRejectsNegativeAndOverdraft() {
		expectRejected(() -> service.betweenAccountsTransfer("Primary", "Savings", "-500", primary, savings));
		expectRejected(() -> service.betweenAccountsTransfer("Savings", "Primary", "-500", primary, savings));
		expectRejected(() -> service.betweenAccountsTransfer("Primary", "Savings", "100.01", primary, savings));
		expectRejected(() -> service.betweenAccountsTransfer("Savings", "Primary", "50.01", primary, savings));
		expectRejected(() -> service.betweenAccountsTransfer("Primary", "Primary", "1", primary, savings));

		assertBalances("100.00", "50.00");
		verifyNothingSaved();
	}

	@Test
	public void betweenAccountsMovesValidAmount() {
		service.betweenAccountsTransfer("Savings", "Primary", "50", primary, savings);

		assertBalances("150.00", "0.00");
		verify(savingsTransactionDao).save(any(SavingsTransaction.class));
	}

	@Test
	public void recipientLookupIsScopedToOwner() {
		User owner = new User();
		owner.setUsername("alice");
		recipient.setUser(owner);
		when(recipientDao.findByName("bob")).thenReturn(recipient);

		assertEquals(recipient, service.findRecipientByNameForUser("bob", "alice"));
		assertNull(service.findRecipientByNameForUser("bob", "mallory"));
		assertNull(service.findRecipientByNameForUser("nobody", "alice"));
	}

	private void assertBalances(String expectedPrimary, String expectedSavings) {
		assertEquals(new BigDecimal(expectedPrimary), primary.getAccountBalance());
		assertEquals(new BigDecimal(expectedSavings), savings.getAccountBalance());
	}

	private void verifyNothingSaved() {
		verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(any(SavingsAccount.class));
		verify(primaryTransactionDao, never()).save(any(PrimaryTransaction.class));
		verify(savingsTransactionDao, never()).save(any(SavingsTransaction.class));
	}

	static void expectRejected(Runnable call) {
		try {
			call.run();
			fail("Expected InvalidTransactionException");
		} catch (InvalidTransactionException expected) {
		}
	}
}
