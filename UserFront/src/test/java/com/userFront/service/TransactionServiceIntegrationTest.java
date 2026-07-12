package com.userFront.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.userFront.AbstractBankingIntegrationTest;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.exception.InsufficientFundsException;

public class TransactionServiceIntegrationTest extends AbstractBankingIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    private Principal principal;

    @Before
    public void setUp() {
        createUser("txnUser", "txn@example.com", "secret");
        principal = () -> "txnUser";
        accountService.deposit("Primary", new BigDecimal("100.0"), principal);
        accountService.deposit("Savings", new BigDecimal("100.0"), principal);
    }

    @Test
    public void betweenAccountsTransferPrimaryToSavings() throws Exception {
        User user = userService.findByUsername("txnUser");
        transactionService.betweenAccountsTransfer("Primary", "Savings", "30",
                user.getPrimaryAccount(), user.getSavingsAccount());

        User updated = userService.findByUsername("txnUser");
        assertEquals(0, new BigDecimal("70.0").compareTo(updated.getPrimaryAccount().getAccountBalance()));
        assertEquals(0, new BigDecimal("130.0").compareTo(updated.getSavingsAccount().getAccountBalance()));
    }

    @Test
    public void betweenAccountsTransferSavingsToPrimary() throws Exception {
        User user = userService.findByUsername("txnUser");
        transactionService.betweenAccountsTransfer("Savings", "Primary", "40",
                user.getPrimaryAccount(), user.getSavingsAccount());

        User updated = userService.findByUsername("txnUser");
        assertEquals(0, new BigDecimal("140.0").compareTo(updated.getPrimaryAccount().getAccountBalance()));
        assertEquals(0, new BigDecimal("60.0").compareTo(updated.getSavingsAccount().getAccountBalance()));
    }

    @Test
    public void betweenAccountsTransferWithInvalidDirectionThrows() {
        User user = userService.findByUsername("txnUser");
        try {
            transactionService.betweenAccountsTransfer("Primary", "Primary", "10",
                    user.getPrimaryAccount(), user.getSavingsAccount());
            fail("Expected an exception for an invalid transfer direction");
        } catch (Exception expected) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void betweenAccountsTransferRejectsNonPositiveAmount() throws Exception {
        User user = userService.findByUsername("txnUser");
        transactionService.betweenAccountsTransfer("Primary", "Savings", "-10",
                user.getPrimaryAccount(), user.getSavingsAccount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void betweenAccountsTransferRejectsNonNumericAmount() throws Exception {
        User user = userService.findByUsername("txnUser");
        transactionService.betweenAccountsTransfer("Primary", "Savings", "abc",
                user.getPrimaryAccount(), user.getSavingsAccount());
    }

    @Test(expected = InsufficientFundsException.class)
    public void betweenAccountsTransferRejectsInsufficientFunds() throws Exception {
        User user = userService.findByUsername("txnUser");
        transactionService.betweenAccountsTransfer("Primary", "Savings", "1000",
                user.getPrimaryAccount(), user.getSavingsAccount());
    }

    @Test
    public void toSomeoneElseTransferFromPrimaryDeductsBalance() {
        Recipient recipient = saveRecipient("txnUser", "Jane Doe");

        User user = userService.findByUsername("txnUser");
        transactionService.toSomeoneElseTransfer(recipient, "Primary", "25",
                user.getPrimaryAccount(), user.getSavingsAccount());

        User updated = userService.findByUsername("txnUser");
        assertEquals(0, new BigDecimal("75.0").compareTo(updated.getPrimaryAccount().getAccountBalance()));
    }

    @Test
    public void recipientCrudLifecycle() {
        saveRecipient("txnUser", "Jane Doe");
        saveRecipient("txnUser", "John Smith");

        List<Recipient> recipients = transactionService.findRecipientList(principal);
        assertEquals(2, recipients.size());

        Recipient found = transactionService.findRecipientByName("Jane Doe");
        assertNotNull(found);
        assertEquals("Jane Doe", found.getName());

        transactionService.deleteRecipientByName("Jane Doe");
        assertNull(transactionService.findRecipientByName("Jane Doe"));
        assertEquals(1, transactionService.findRecipientList(principal).size());
    }

    @Test
    public void findRecipientListIsScopedToOwner() {
        saveRecipient("txnUser", "Jane Doe");

        createUser("otherUser", "other@example.com", "secret");
        saveRecipient("otherUser", "Someone Else");

        List<Recipient> recipients = transactionService.findRecipientList(principal);
        assertEquals(1, recipients.size());
        assertTrue(recipients.stream().allMatch(r -> "txnUser".equals(r.getUser().getUsername())));
    }

    private Recipient saveRecipient(String username, String name) {
        User user = userService.findByUsername(username);
        Recipient recipient = new Recipient();
        recipient.setName(name);
        recipient.setEmail(name.replace(" ", ".").toLowerCase() + "@example.com");
        recipient.setPhone("1112223333");
        recipient.setAccountNumber("123456789");
        recipient.setDescription("Friend");
        recipient.setUser(user);
        return transactionService.saveRecipient(recipient);
    }
}
