package com.userFront.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.UserService;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

public class AccountControllerTest {

	private UserService userService;
	private AccountController controller;
	private Principal principal;
	private User user;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		TransactionServiceImpl transactionService = new TransactionServiceImpl();
		ReflectionTestUtils.setField(transactionService, "userService", userService);

		controller = new AccountController();
		ReflectionTestUtils.setField(controller, "userService", userService);
		ReflectionTestUtils.setField(controller, "transactionService", transactionService);

		user = new User();
		user.setUsername("alice");
		principal = () -> "alice";
		when(userService.findByUsername("alice")).thenReturn(user);
	}

	@Test
	public void primaryAccountLoadsUserOnce() {
		PrimaryAccount account = new PrimaryAccount();
		List<PrimaryTransaction> transactions = Collections.singletonList(new PrimaryTransaction());
		account.setPrimaryTransactionList(transactions);
		user.setPrimaryAccount(account);
		ExtendedModelMap model = new ExtendedModelMap();

		assertEquals("primaryAccount", controller.primaryAccount(model, principal));

		verify(userService, times(1)).findByUsername("alice");
		assertSame(account, model.get("primaryAccount"));
		assertSame(transactions, model.get("primaryTransactionList"));
	}

	@Test
	public void savingsAccountLoadsUserOnce() {
		SavingsAccount account = new SavingsAccount();
		List<SavingsTransaction> transactions = Collections.singletonList(new SavingsTransaction());
		account.setSavingsTransactionList(transactions);
		user.setSavingsAccount(account);
		ExtendedModelMap model = new ExtendedModelMap();

		assertEquals("savingsAccount", controller.savingsAccount(model, principal));

		verify(userService, times(1)).findByUsername("alice");
		assertSame(account, model.get("savingsAccount"));
		assertSame(transactions, model.get("savingsTransactionList"));
	}
}
