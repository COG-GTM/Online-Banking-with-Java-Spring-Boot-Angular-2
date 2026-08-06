package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import com.userFront.dao.RecipientDao;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.service.UserService;

public class TransactionServiceImplTest {

	@Mock
	private UserService userService;

	@Mock
	private RecipientDao recipientDao;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	private User attacker;

	private Principal principal;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		attacker = new User();
		attacker.setUsername("attacker");

		principal = new Principal() {
			public String getName() {
				return "attacker";
			}
		};

		when(userService.findByUsername("attacker")).thenReturn(attacker);
	}

	@Test
	public void findRecipientListIsScopedToTheAuthenticatedUser() {
		List<Recipient> owned = Arrays.asList(new Recipient());
		when(recipientDao.findByUser(attacker)).thenReturn(owned);

		assertEquals(owned, transactionService.findRecipientList(principal));
	}

	@Test
	public void findRecipientByNameIsScopedToTheAuthenticatedUser() {
		Recipient owned = new Recipient();
		when(recipientDao.findByNameAndUser("payee", attacker)).thenReturn(owned);

		assertSame(owned, transactionService.findRecipientByName("payee", principal));
		verify(recipientDao, never()).findAll();
	}

	@Test
	public void findRecipientByNameReturnsNullForAnotherUsersRecipient() {
		when(recipientDao.findByNameAndUser("victimPayee", attacker)).thenReturn(null);

		assertEquals(null, transactionService.findRecipientByName("victimPayee", principal));
	}

	@Test
	public void deleteRecipientByNameIsScopedToTheAuthenticatedUser() {
		transactionService.deleteRecipientByName("victimPayee", principal);

		verify(recipientDao).deleteByNameAndUser("victimPayee", attacker);
	}

	@Test
	public void saveRecipientRejectsAnIdOwnedByAnotherUser() {
		Recipient hijack = new Recipient();
		hijack.setId(42L);
		hijack.setName("attackerPayee");
		when(recipientDao.findByIdAndUser(42L, attacker)).thenReturn(null);

		try {
			transactionService.saveRecipient(hijack, principal);
			fail("expected AccessDeniedException");
		} catch (AccessDeniedException expected) {
			// expected
		}

		verify(recipientDao, never()).save(any(Recipient.class));
	}

	@Test
	public void saveRecipientUpdatesTheOwnedRecipientRatherThanTheSubmittedInstance() {
		Recipient existing = new Recipient();
		existing.setId(7L);
		existing.setUser(attacker);
		when(recipientDao.findByIdAndUser(7L, attacker)).thenReturn(existing);

		Recipient submitted = new Recipient();
		submitted.setId(7L);
		submitted.setName("newName");
		submitted.setAccountNumber("123456");
		submitted.setUser(new User());

		transactionService.saveRecipient(submitted, principal);

		assertEquals("newName", existing.getName());
		assertEquals("123456", existing.getAccountNumber());
		assertSame(attacker, existing.getUser());
		verify(recipientDao).save(existing);
		verify(recipientDao, never()).save(submitted);
	}

	@Test
	public void saveRecipientCreatesANewRecipientOwnedByTheAuthenticatedUser() {
		Recipient submitted = new Recipient();
		submitted.setName("newPayee");
		submitted.setUser(new User());

		transactionService.saveRecipient(submitted, principal);

		verify(recipientDao, never()).findByIdAndUser(anyLong(), any(User.class));
		verify(recipientDao, never()).findByNameAndUser(anyString(), any(User.class));
		verify(recipientDao).save(any(Recipient.class));
	}
}
