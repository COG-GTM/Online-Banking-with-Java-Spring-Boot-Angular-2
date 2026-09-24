package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.RecipientDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

/**
 * Regression contract for money movement on Boot 1.5.4: deposit, withdraw,
 * between-accounts transfer, the recipient lifecycle and recipient transfer.
 * Asserts on the persisted rows, and records today's behaviour as-is.
 */
public class MoneyMovementSmokeTest extends AbstractIntegrationTest {

	private static final AtomicInteger SEQ = new AtomicInteger();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserService userService;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;

	@Autowired
	private SavingsTransactionDao savingsTransactionDao;

	@Autowired
	private RecipientDao recipientDao;

	private String username;

	@Before
	public void createUser() {
		int n = SEQ.incrementAndGet();
		username = "smoke" + System.nanoTime() + n;

		User user = new User();
		user.setUsername(username);
		user.setPassword("password");
		user.setFirstName("Smoke");
		user.setLastName("Test");
		user.setEmail(username + "@example.com");
		user.setPhone("5551234567");

		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");

		Set<UserRole> roles = new HashSet<>();
		roles.add(new UserRole(user, role));

		userService.createUser(user, roles);
	}

	@Test
	public void depositCreditsPrimaryAccountAndWritesLedgerRow() throws Exception {
		mockMvc.perform(post("/account/deposit").with(user(username)).param("accountType", "Primary").param("amount",
				"250.00")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/userFront"));

		assertEquals(0, new BigDecimal("250.00").compareTo(primaryBalance()));

		PrimaryTransaction transaction = onlyPrimaryTransaction();
		assertEquals("Deposit to Primary Account", transaction.getDescription());
		assertEquals("Account", transaction.getType());
		assertEquals("Finished", transaction.getStatus());
		assertEquals(250.00, transaction.getAmount(), 0.001);
		assertEquals(0, new BigDecimal("250.00").compareTo(transaction.getAvailableBalance()));
	}

	@Test
	public void depositCreditsSavingsAccountAndWritesLedgerRow() throws Exception {
		mockMvc.perform(post("/account/deposit").with(user(username)).param("accountType", "Savings").param("amount",
				"75.50")).andExpect(status().is3xxRedirection());

		assertEquals(0, new BigDecimal("75.50").compareTo(savingsBalance()));

		SavingsTransaction transaction = onlySavingsTransaction();
		assertEquals("Deposit to savings Account", transaction.getDescription());
		assertEquals(75.50, transaction.getAmount(), 0.001);
		assertEquals(0, new BigDecimal("75.50").compareTo(transaction.getAvailableBalance()));
	}

	@Test
	public void withdrawDebitsPrimaryAccountAndWritesLedgerRow() throws Exception {
		deposit("Primary", "400.00");

		mockMvc.perform(post("/account/withdraw").with(user(username)).param("accountType", "Primary").param("amount",
				"150.00")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/userFront"));

		assertEquals(0, new BigDecimal("250.00").compareTo(primaryBalance()));

		PrimaryTransaction withdrawal = lastPrimaryTransaction();
		assertEquals("Withdraw from Primary Account", withdrawal.getDescription());
		// The withdrawal is recorded as a positive amount; only availableBalance moves down.
		assertEquals(150.00, withdrawal.getAmount(), 0.001);
		assertEquals(0, new BigDecimal("250.00").compareTo(withdrawal.getAvailableBalance()));
	}

	@Test
	public void withdrawDebitsSavingsAccountAndWritesLedgerRow() throws Exception {
		deposit("Savings", "300.00");

		mockMvc.perform(post("/account/withdraw").with(user(username)).param("accountType", "Savings").param("amount",
				"120.00")).andExpect(status().is3xxRedirection());

		assertEquals(0, new BigDecimal("180.00").compareTo(savingsBalance()));

		SavingsTransaction withdrawal = lastSavingsTransaction();
		assertEquals("Withdraw from savings Account", withdrawal.getDescription());
		assertEquals(120.00, withdrawal.getAmount(), 0.001);
		assertEquals(0, new BigDecimal("180.00").compareTo(withdrawal.getAvailableBalance()));
	}

	@Test
	public void betweenAccountsTransferPrimaryToSavingsMovesBalancesAndConservesTotal() throws Exception {
		deposit("Primary", "500.00");
		BigDecimal totalBefore = primaryBalance().add(savingsBalance());

		mockMvc.perform(post("/transfer/betweenAccounts").with(user(username)).param("transferFrom", "Primary")
				.param("transferTo", "Savings").param("amount", "200.00")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		assertEquals(0, new BigDecimal("300.00").compareTo(primaryBalance()));
		assertEquals(0, new BigDecimal("200.00").compareTo(savingsBalance()));
		assertEquals(0, totalBefore.compareTo(primaryBalance().add(savingsBalance())));

		PrimaryTransaction transfer = lastPrimaryTransaction();
		assertEquals("Between account transfer from Primary to Savings", transfer.getDescription());
		assertEquals("Account", transfer.getType());
		assertEquals(200.00, transfer.getAmount(), 0.001);
		// Only the sending ledger is written in this direction.
		assertTrue(savingsTransactions().isEmpty());
	}

	@Test
	public void betweenAccountsTransferSavingsToPrimaryMovesBalancesAndConservesTotal() throws Exception {
		deposit("Savings", "500.00");
		List<PrimaryTransaction> primaryBefore = primaryTransactions();
		BigDecimal totalBefore = primaryBalance().add(savingsBalance());

		mockMvc.perform(post("/transfer/betweenAccounts").with(user(username)).param("transferFrom", "Savings")
				.param("transferTo", "Primary").param("amount", "125.00")).andExpect(status().is3xxRedirection());

		assertEquals(0, new BigDecimal("125.00").compareTo(primaryBalance()));
		assertEquals(0, new BigDecimal("375.00").compareTo(savingsBalance()));
		assertEquals(0, totalBefore.compareTo(primaryBalance().add(savingsBalance())));

		SavingsTransaction transfer = lastSavingsTransaction();
		assertEquals("Between account transfer from Savings to Primary", transfer.getDescription());
		assertEquals("Transfer", transfer.getType());
		assertEquals(125.00, transfer.getAmount(), 0.001);
		// Only the sending ledger is written in this direction.
		assertEquals(primaryBefore.size(), primaryTransactions().size());
	}

	@Test
	public void recipientLifecycleSavesListsAndDeletes() throws Exception {
		String recipientName = "Payee" + System.nanoTime();

		mockMvc.perform(post("/transfer/recipient/save").with(user(username)).param("name", recipientName)
				.param("email", "payee@example.com").param("phone", "5559876543").param("accountNumber", "987654321")
				.param("description", "Rent")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/transfer/recipient"));

		Recipient saved = recipientDao.findByName(recipientName);
		assertNotNull(saved);
		assertEquals("payee@example.com", saved.getEmail());
		assertEquals("987654321", saved.getAccountNumber());
		assertEquals(username, saved.getUser().getUsername());

		MvcResult listed = mockMvc.perform(get("/transfer/recipient").with(user(username))).andExpect(status().isOk())
				.andExpect(view().name("recipient")).andExpect(model().attributeExists("recipientList")).andReturn();
		assertEquals(Collections.singletonList(recipientName), recipientNames(listed));

		// Delete is a GET today; COG-1155 converts it to a POST and updates this test.
		mockMvc.perform(get("/transfer/recipient/delete").with(user(username)).param("recipientName", recipientName))
				.andExpect(status().isOk()).andExpect(view().name("recipient"));

		assertNull(recipientDao.findByName(recipientName));
	}

	@Test
	public void transferToSomeoneElseDebitsTheSendingAccount() throws Exception {
		deposit("Primary", "600.00");
		String recipientName = "Payee" + System.nanoTime();
		saveRecipient(recipientName);

		mockMvc.perform(post("/transfer/toSomeoneElse").with(user(username)).param("recipientName", recipientName)
				.param("accountType", "Primary").param("amount", "175.00")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		assertEquals(0, new BigDecimal("425.00").compareTo(primaryBalance()));

		PrimaryTransaction debit = lastPrimaryTransaction();
		assertEquals("Transfer to recipient " + recipientName, debit.getDescription());
		assertEquals("Transfer", debit.getType());
		assertEquals("Finished", debit.getStatus());
		assertEquals(175.00, debit.getAmount(), 0.001);
		assertEquals(0, new BigDecimal("425.00").compareTo(debit.getAvailableBalance()));
	}

	@Test
	public void transferToSomeoneElseDebitsTheSendingSavingsAccount() throws Exception {
		deposit("Savings", "600.00");
		String recipientName = "Payee" + System.nanoTime();
		saveRecipient(recipientName);

		mockMvc.perform(post("/transfer/toSomeoneElse").with(user(username)).param("recipientName", recipientName)
				.param("accountType", "Savings").param("amount", "175.00")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		assertEquals(0, new BigDecimal("425.00").compareTo(savingsBalance()));

		SavingsTransaction debit = lastSavingsTransaction();
		assertEquals("Transfer to recipient " + recipientName, debit.getDescription());
		assertEquals("Transfer", debit.getType());
		assertEquals("Finished", debit.getStatus());
		assertEquals(175.00, debit.getAmount(), 0.001);
		assertEquals(0, new BigDecimal("425.00").compareTo(debit.getAvailableBalance()));
	}

	@SuppressWarnings("unchecked")
	private List<String> recipientNames(MvcResult result) {
		List<Recipient> recipients = (List<Recipient>) result.getModelAndView().getModel().get("recipientList");
		List<String> names = new ArrayList<>();
		for (Recipient recipient : recipients) {
			names.add(recipient.getName());
		}
		return names;
	}

	private void deposit(String accountType, String amount) throws Exception {
		mockMvc.perform(post("/account/deposit").with(user(username)).param("accountType", accountType).param("amount",
				amount)).andExpect(status().is3xxRedirection());
	}

	private void saveRecipient(String recipientName) throws Exception {
		mockMvc.perform(post("/transfer/recipient/save").with(user(username)).param("name", recipientName)
				.param("email", "payee@example.com").param("phone", "5559876543").param("accountNumber", "987654321")
				.param("description", "Rent")).andExpect(status().is3xxRedirection());
	}

	private BigDecimal primaryBalance() {
		return reloadPrimaryAccount().getAccountBalance();
	}

	private BigDecimal savingsBalance() {
		return reloadSavingsAccount().getAccountBalance();
	}

	private PrimaryAccount reloadPrimaryAccount() {
		return primaryAccountDao.findByAccountNumber(userService.findByUsername(username).getPrimaryAccount()
				.getAccountNumber());
	}

	private SavingsAccount reloadSavingsAccount() {
		return savingsAccountDao.findByAccountNumber(userService.findByUsername(username).getSavingsAccount()
				.getAccountNumber());
	}

	private List<PrimaryTransaction> primaryTransactions() {
		Long accountId = reloadPrimaryAccount().getId();
		List<PrimaryTransaction> transactions = new ArrayList<>();
		for (PrimaryTransaction transaction : primaryTransactionDao.findAll()) {
			if (accountId.equals(transaction.getPrimaryAccount().getId())) {
				transactions.add(transaction);
			}
		}
		Collections.sort(transactions, (a, b) -> a.getId().compareTo(b.getId()));
		return transactions;
	}

	private List<SavingsTransaction> savingsTransactions() {
		Long accountId = reloadSavingsAccount().getId();
		List<SavingsTransaction> transactions = new ArrayList<>();
		for (SavingsTransaction transaction : savingsTransactionDao.findAll()) {
			if (accountId.equals(transaction.getSavingsAccount().getId())) {
				transactions.add(transaction);
			}
		}
		Collections.sort(transactions, (a, b) -> a.getId().compareTo(b.getId()));
		return transactions;
	}

	private PrimaryTransaction onlyPrimaryTransaction() {
		List<PrimaryTransaction> transactions = primaryTransactions();
		assertEquals(1, transactions.size());
		return transactions.get(0);
	}

	private SavingsTransaction onlySavingsTransaction() {
		List<SavingsTransaction> transactions = savingsTransactions();
		assertEquals(1, transactions.size());
		return transactions.get(0);
	}

	private PrimaryTransaction lastPrimaryTransaction() {
		List<PrimaryTransaction> transactions = primaryTransactions();
		return transactions.get(transactions.size() - 1);
	}

	private SavingsTransaction lastSavingsTransaction() {
		List<SavingsTransaction> transactions = savingsTransactions();
		return transactions.get(transactions.size() - 1);
	}
}
