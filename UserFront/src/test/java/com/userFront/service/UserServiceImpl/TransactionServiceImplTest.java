package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

	@Mock
	private UserService userService;

	@Mock
	private PrimaryTransactionDao primaryTransactionDao;

	@Mock
	private SavingsTransactionDao savingsTransactionDao;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private PrimaryAccount primaryAccount;

	private SavingsAccount savingsAccount;

	@Before
	public void setUp() {
		primaryAccount = new PrimaryAccount();
		savingsAccount = new SavingsAccount();
		User user = new User();
		user.setPrimaryAccount(primaryAccount);
		user.setSavingsAccount(savingsAccount);
		when(userService.findByUsername("alice")).thenReturn(user);
	}

	@Test
	public void primaryTransactionsAreReadOnePageAtATimeFromTheUsersAccount() {
		Page<PrimaryTransaction> page = new PageImpl<PrimaryTransaction>(
				Collections.singletonList(new PrimaryTransaction()));
		when(primaryTransactionDao.findByPrimaryAccountOrderByDateDescIdDesc(eq(primaryAccount), any(Pageable.class)))
				.thenReturn(page);

		assertSame(page, transactionService.findPrimaryTransactionPage("alice", 2, 25));

		Pageable requested = capturePrimaryPageable();
		assertEquals(2, requested.getPageNumber());
		assertEquals(25, requested.getPageSize());
	}

	@Test
	public void savingsTransactionsAreReadOnePageAtATimeFromTheUsersAccount() {
		Page<SavingsTransaction> page = new PageImpl<SavingsTransaction>(
				Collections.singletonList(new SavingsTransaction()));
		when(savingsTransactionDao.findBySavingsAccountOrderByDateDescIdDesc(eq(savingsAccount), any(Pageable.class)))
				.thenReturn(page);

		assertSame(page, transactionService.findSavingsTransactionPage("alice", 0, 10));

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(savingsTransactionDao).findBySavingsAccountOrderByDateDescIdDesc(eq(savingsAccount), captor.capture());
		assertEquals(0, captor.getValue().getPageNumber());
		assertEquals(10, captor.getValue().getPageSize());
	}

	@Test
	public void pageSizeIsCappedAtTheMaximum() {
		transactionService.findPrimaryTransactionPage("alice", 0, 1000000);

		assertEquals(TransactionService.MAX_PAGE_SIZE, capturePrimaryPageable().getPageSize());
	}

	@Test
	public void nonPositiveSizeFallsBackToTheDefaultAndNegativePageToTheFirst() {
		transactionService.findPrimaryTransactionPage("alice", -3, 0);

		Pageable requested = capturePrimaryPageable();
		assertEquals(0, requested.getPageNumber());
		assertEquals(TransactionService.DEFAULT_PAGE_SIZE, requested.getPageSize());
	}

	private Pageable capturePrimaryPageable() {
		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(primaryTransactionDao).findByPrimaryAccountOrderByDateDescIdDesc(eq(primaryAccount), captor.capture());
		return captor.getValue();
	}
}
