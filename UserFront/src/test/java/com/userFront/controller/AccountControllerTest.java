package com.userFront.controller;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.AccountService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class AccountControllerTest {

	@Mock
	private UserService userService;

	@Mock
	private AccountService accountService;

	@Mock
	private TransactionService transactionService;

	@InjectMocks
	private AccountController accountController;

	private MockMvc mockMvc;
	private Principal principal;
	private User user;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(accountController)
				.setViewResolvers(new StubViewResolver()).build();

		principal = new Principal() {
			public String getName() {
				return "john";
			}
		};

		user = new User();
		user.setUsername("john");
		user.setPrimaryAccount(new PrimaryAccount());
		user.setSavingsAccount(new SavingsAccount());
	}

	@Test
	public void primaryAccount_authenticatedUser_returnsViewWithAccountData() throws Exception {
		List<PrimaryTransaction> transactions = new ArrayList<PrimaryTransaction>();
		when(transactionService.findPrimaryTransactionList("john")).thenReturn(transactions);
		when(userService.findByUsername("john")).thenReturn(user);

		mockMvc.perform(get("/account/primaryAccount").principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("primaryAccount"))
				.andExpect(model().attribute("primaryAccount", user.getPrimaryAccount()))
				.andExpect(model().attribute("primaryTransactionList", transactions));
	}

	@Test
	public void savingsAccount_authenticatedUser_returnsViewWithAccountData() throws Exception {
		List<SavingsTransaction> transactions = new ArrayList<SavingsTransaction>();
		when(transactionService.findSavingsTransactionList("john")).thenReturn(transactions);
		when(userService.findByUsername("john")).thenReturn(user);

		mockMvc.perform(get("/account/savingsAccount").principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("savingsAccount"))
				.andExpect(model().attribute("savingsAccount", user.getSavingsAccount()))
				.andExpect(model().attribute("savingsTransactionList", transactions));
	}

	@Test
	public void deposit_GET_returnsDepositView() throws Exception {
		mockMvc.perform(get("/account/deposit"))
				.andExpect(status().isOk())
				.andExpect(view().name("deposit"));
	}

	@Test
	public void deposit_POST_callsServiceAndRedirects() throws Exception {
		mockMvc.perform(post("/account/deposit")
				.param("amount", "100")
				.param("accountType", "Primary")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("redirect:/userFront"));

		verify(accountService).deposit(eq("Primary"), eq(100.0), eq(principal));
	}

	@Test
	public void withdraw_GET_returnsWithdrawView() throws Exception {
		mockMvc.perform(get("/account/withdraw"))
				.andExpect(status().isOk())
				.andExpect(view().name("withdraw"));
	}

	@Test
	public void withdraw_POST_callsServiceAndRedirects() throws Exception {
		mockMvc.perform(post("/account/withdraw")
				.param("amount", "40")
				.param("accountType", "Savings")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("redirect:/userFront"));

		verify(accountService).withdraw(eq("Savings"), eq(40.0), eq(principal));
	}
}
