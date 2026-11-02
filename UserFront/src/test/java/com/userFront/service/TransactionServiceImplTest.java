package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.userFront.dao.RecipientDao;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

public class TransactionServiceImplTest {

	@Mock
	private RecipientDao recipientDao;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private User alice;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		alice = new User();
		alice.setUsername("alice");
	}

	@Test
	public void findRecipientByNameQueriesByNameAndOwner() {
		Recipient recipient = new Recipient();
		when(recipientDao.findFirstByNameAndUserOrderByIdAsc("Bob Smith", alice)).thenReturn(recipient);

		assertSame(recipient, transactionService.findRecipientByName("Bob Smith", alice));
	}

	@Test
	public void findRecipientByIdAndUserQueriesByIdAndOwner() {
		Recipient recipient = new Recipient();
		when(recipientDao.findByIdAndUser(7L, alice)).thenReturn(recipient);

		assertSame(recipient, transactionService.findRecipientByIdAndUser(7L, alice));
	}

	@Test
	public void deleteRecipientByNameDeletesOnlyOwnersRowsAndReturnsCount() {
		when(recipientDao.deleteByNameAndUser("Bob Smith", alice)).thenReturn(2L);

		assertEquals(2L, transactionService.deleteRecipientByName("Bob Smith", alice));
		verify(recipientDao).deleteByNameAndUser("Bob Smith", alice);
	}

	@Test
	public void deleteRecipientByNameReturnsZeroWhenNothingMatched() {
		assertEquals(0L, transactionService.deleteRecipientByName("Bob Smith", alice));
	}
}
