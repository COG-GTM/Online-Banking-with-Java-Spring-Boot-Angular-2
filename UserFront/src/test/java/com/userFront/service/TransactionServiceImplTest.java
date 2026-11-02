package com.userFront.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.userFront.dao.RecipientDao;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.service.UserServiceImpl.TransactionServiceImpl;

public class TransactionServiceImplTest {

    private final Principal carol = () -> "carol";

    private RecipientDao recipientDao;
    private TransactionServiceImpl transactionService;
    private Recipient alicesBob;
    private Recipient carolsDave;

    @Before
    public void setUp() {
        recipientDao = mock(RecipientDao.class);
        transactionService = new TransactionServiceImpl();
        ReflectionTestUtils.setField(transactionService, "recipientDao", recipientDao);

        alicesBob = recipient("bob", "alice");
        carolsDave = recipient("dave", "carol");
        when(recipientDao.findAll()).thenReturn(Arrays.asList(alicesBob, carolsDave));
    }

    @Test
    public void cannotFindOrDeleteAnotherUsersRecipient() {
        assertNull(transactionService.findRecipientByName("bob", carol));

        transactionService.deleteRecipientByName("bob", carol);

        verify(recipientDao, never()).delete(any(Recipient.class));
    }

    @Test
    public void deletesOwnRecipient() {
        assertSame(carolsDave, transactionService.findRecipientByName("dave", carol));

        transactionService.deleteRecipientByName("dave", carol);

        verify(recipientDao).delete(carolsDave);
    }

    private static Recipient recipient(String name, String owner) {
        User user = new User();
        user.setUsername(owner);
        Recipient recipient = new Recipient();
        recipient.setName(name);
        recipient.setUser(user);
        return recipient;
    }
}
