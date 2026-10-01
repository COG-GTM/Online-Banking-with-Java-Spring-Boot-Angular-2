package com.userFront.service.UserServiceImpl;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.userFront.dao.RecipientDao;
import com.userFront.domain.Recipient;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

    @Mock
    private RecipientDao recipientDao;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    public void findRecipientListQueriesOnlyThePrincipalsRecipients() {
        List<Recipient> owned = Arrays.asList(new Recipient(), new Recipient());
        when(recipientDao.findByUser_Username("alice")).thenReturn(owned);
        Principal principal = () -> "alice";

        List<Recipient> result = transactionService.findRecipientList(principal);

        assertSame(owned, result);
        verify(recipientDao).findByUser_Username("alice");
        verifyNoMoreInteractions(recipientDao);
    }
}
