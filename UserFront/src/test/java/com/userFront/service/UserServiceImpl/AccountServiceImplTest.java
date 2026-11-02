package com.userFront.service.UserServiceImpl;

import static com.userFront.service.UserServiceImpl.TransactionServiceImplTest.expectRejected;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.security.Principal;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

public class AccountServiceImplTest {

	private AccountServiceImpl service;
	private PrimaryAccountDao primaryAccountDao;
	private SavingsAccountDao savingsAccountDao;
	private TransactionService transactionService;
	private PrimaryAccount primary;
	private SavingsAccount savings;
	private final Principal principal = () -> "mallory";

	@Before
	public void setUp() {
		service = new AccountServiceImpl();
		primaryAccountDao = mock(PrimaryAccountDao.class);
		savingsAccountDao = mock(SavingsAccountDao.class);
		transactionService = mock(TransactionService.class);
		UserService userService = mock(UserService.class);
		EntityManager entityManager = mock(EntityManager.class);
		when(entityManager.contains(any())).thenReturn(true);

		primary = new PrimaryAccount();
		primary.setAccountBalance(new BigDecimal("100.00"));
		savings = new SavingsAccount();
		savings.setAccountBalance(new BigDecimal("50.00"));
		User user = new User();
		user.setUsername("mallory");
		user.setPrimaryAccount(primary);
		user.setSavingsAccount(savings);
		when(userService.findByUsername("mallory")).thenReturn(user);

		ReflectionTestUtils.setField(service, "primaryAccountDao", primaryAccountDao);
		ReflectionTestUtils.setField(service, "savingsAccountDao", savingsAccountDao);
		ReflectionTestUtils.setField(service, "transactionService", transactionService);
		ReflectionTestUtils.setField(service, "userService", userService);
		ReflectionTestUtils.setField(service, "entityManager", entityManager);
	}

	@Test
	public void withdrawRejectsNegativeAmountAndOverdraft() {
		expectRejected(() -> service.withdraw("Primary", "-100", principal));
		expectRejected(() -> service.withdraw("Savings", "-100", principal));
		expectRejected(() -> service.withdraw("Primary", "100.01", principal));
		expectRejected(() -> service.withdraw("Savings", "50.01", principal));
		expectRejected(() -> service.withdraw("Primary", "abc", principal));

		assertEquals(new BigDecimal("100.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("50.00"), savings.getAccountBalance());
		verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(any(SavingsAccount.class));
		verify(transactionService, never()).savePrimaryWithdrawTransaction(any(PrimaryTransaction.class));
		verify(transactionService, never()).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
	}

	@Test
	public void withdrawDebitsValidAmount() {
		service.withdraw("Savings", "50", principal);

		assertEquals(new BigDecimal("0.00"), savings.getAccountBalance());
		verify(transactionService).saveSavingsWithdrawTransaction(any(SavingsTransaction.class));
	}

	@Test
	public void depositRejectsNegativeAmount() {
		expectRejected(() -> service.deposit("Primary", "-100", principal));
		expectRejected(() -> service.deposit("Savings", "0", principal));

		assertEquals(new BigDecimal("100.00"), primary.getAccountBalance());
		assertEquals(new BigDecimal("50.00"), savings.getAccountBalance());
		verify(primaryAccountDao, never()).save(any(PrimaryAccount.class));
		verify(savingsAccountDao, never()).save(any(SavingsAccount.class));
	}
}
