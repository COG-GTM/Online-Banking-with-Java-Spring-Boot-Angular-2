package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

	@Mock
	private PrimaryAccountDao primaryAccountDao;

	@Mock
	private SavingsAccountDao savingsAccountDao;

	@InjectMocks
	private AccountServiceImpl accountService;

	@Before
	public void setUp() {
		when(primaryAccountDao.save(any(PrimaryAccount.class))).thenAnswer(returnFirstArgument());
		when(savingsAccountDao.save(any(SavingsAccount.class))).thenAnswer(returnFirstArgument());
	}

	@Test
	public void createdAccountNumbersAreInRangeAndDistinct() {
		Set<Integer> issued = new HashSet<Integer>();
		for (int i = 0; i < 200; i++) {
			int primary = accountService.createPrimaryAccount().getAccountNumber();
			int savings = accountService.createSavingsAccount().getAccountNumber();
			assertInRange(primary);
			assertInRange(savings);
			assertTrue(issued.add(primary));
			assertTrue(issued.add(savings));
		}
	}

	@Test
	public void returnsSavedEntityWithoutLookupByAccountNumber() {
		PrimaryAccount primary = accountService.createPrimaryAccount();
		SavingsAccount savings = accountService.createSavingsAccount();

		verify(primaryAccountDao, never()).findByAccountNumber(anyInt());
		verify(savingsAccountDao, never()).findByAccountNumber(anyInt());
		assertEquals(0, primary.getAccountBalance().signum());
		assertEquals(0, savings.getAccountBalance().signum());
	}

	@Test
	public void skipsNumbersAlreadyUsedByEitherAccountType() {
		final Set<Integer> taken = new HashSet<Integer>();
		when(primaryAccountDao.countByAccountNumber(anyInt())).thenAnswer(new Answer<Long>() {
			private int calls;

			public Long answer(InvocationOnMock invocation) {
				int number = (Integer) invocation.getArguments()[0];
				if (calls++ < 3) {
					taken.add(number);
					return 1L;
				}
				return 0L;
			}
		});
		when(savingsAccountDao.countByAccountNumber(anyInt())).thenReturn(0L);

		int number = accountService.createPrimaryAccount().getAccountNumber();

		assertEquals(3, taken.size());
		assertFalse(taken.contains(number));
	}

	@Test(expected = IllegalStateException.class)
	public void failsInsteadOfIssuingDuplicateWhenNoFreeNumberFound() {
		when(savingsAccountDao.countByAccountNumber(anyInt())).thenReturn(1L);

		accountService.createSavingsAccount();
	}

	private static void assertInRange(int number) {
		assertTrue(number >= AccountServiceImpl.MIN_ACCOUNT_NUMBER);
		assertTrue(number <= AccountServiceImpl.MAX_ACCOUNT_NUMBER);
	}

	private static <T> Answer<T> returnFirstArgument() {
		return new Answer<T>() {
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocation) {
				return (T) invocation.getArguments()[0];
			}
		};
	}
}
