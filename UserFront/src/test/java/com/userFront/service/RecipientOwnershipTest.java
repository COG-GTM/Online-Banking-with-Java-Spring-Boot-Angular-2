package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.userFront.dao.RecipientDao;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.exception.RecipientNotFoundException;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class RecipientOwnershipTest {

	@Mock
	private UserService userService;

	@Mock
	private RecipientDao recipientDao;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private final Principal mallory = () -> "mallory";
	private User malloryUser;
	private User aliceUser;

	@Before
	public void setUp() {
		malloryUser = new User();
		malloryUser.setUsername("mallory");
		aliceUser = new User();
		aliceUser.setUsername("alice");
		when(userService.findByUsername("mallory")).thenReturn(malloryUser);
		when(recipientDao.save(any(Recipient.class))).thenAnswer(inv -> inv.getArguments()[0]);
	}

	@Test
	public void findIsScopedToCaller() {
		Recipient own = recipient(7L, "Bob Smith", malloryUser);
		when(recipientDao.findFirstByNameAndUserOrderByIdAsc("Bob Smith", malloryUser)).thenReturn(own);

		assertSame(own, transactionService.findRecipientByName("Bob Smith", mallory));
	}

	@Test(expected = RecipientNotFoundException.class)
	public void findOtherUsersRecipientIsNotFound() {
		when(recipientDao.findFirstByNameAndUserOrderByIdAsc("Bob Smith", malloryUser)).thenReturn(null);

		transactionService.findRecipientByName("Bob Smith", mallory);
	}

	@Test
	public void deleteIsScopedToCaller() {
		when(recipientDao.deleteByNameAndUser("Bob Smith", malloryUser)).thenReturn(1L);

		transactionService.deleteRecipientByName("Bob Smith", mallory);

		verify(recipientDao).deleteByNameAndUser("Bob Smith", malloryUser);
	}

	@Test(expected = RecipientNotFoundException.class)
	public void deleteOtherUsersRecipientIsNotFound() {
		when(recipientDao.deleteByNameAndUser("Bob Smith", malloryUser)).thenReturn(0L);

		transactionService.deleteRecipientByName("Bob Smith", mallory);
	}

	@Test
	public void saveNewRecipientIsOwnedByCaller() {
		Recipient form = recipient(null, "Carol", null);
		form.setUser(aliceUser);

		Recipient saved = transactionService.saveRecipient(form, mallory);

		assertNotSame(form, saved);
		assertNull(saved.getId());
		assertSame(malloryUser, saved.getUser());
		assertEquals("Carol", saved.getName());
	}

	@Test
	public void saveUpdatesOnlyCallersOwnRecipient() {
		Recipient existing = recipient(7L, "Bob", malloryUser);
		when(recipientDao.findByIdAndUser(7L, malloryUser)).thenReturn(existing);
		Recipient form = recipient(7L, "Bob Jr", aliceUser);

		Recipient saved = transactionService.saveRecipient(form, mallory);

		assertSame(existing, saved);
		assertSame(malloryUser, saved.getUser());
		assertEquals("Bob Jr", saved.getName());
	}

	@Test
	public void saveWithOtherUsersIdIsRejected() {
		when(recipientDao.findByIdAndUser(42L, malloryUser)).thenReturn(null);
		Recipient form = recipient(42L, "Bob Smith", null);

		try {
			transactionService.saveRecipient(form, mallory);
		} catch (RecipientNotFoundException expected) {
			verify(recipientDao, never()).save(any(Recipient.class));
			return;
		}
		throw new AssertionError("expected RecipientNotFoundException");
	}

	private static Recipient recipient(Long id, String name, User user) {
		Recipient recipient = new Recipient();
		recipient.setId(id);
		recipient.setName(name);
		recipient.setAccountNumber("12345678");
		recipient.setUser(user);
		return recipient;
	}
}
