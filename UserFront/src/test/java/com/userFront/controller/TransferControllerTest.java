package com.userFront.controller;

import static org.mockito.Matchers.any;
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
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class TransferControllerTest {

	@Mock
	private TransactionService transactionService;

	@Mock
	private UserService userService;

	@InjectMocks
	private TransferController transferController;

	private MockMvc mockMvc;
	private Principal principal;
	private User user;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(transferController)
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
	public void betweenAccounts_GET_returnsView() throws Exception {
		mockMvc.perform(get("/transfer/betweenAccounts"))
				.andExpect(status().isOk())
				.andExpect(view().name("betweenAccounts"));
	}

	@Test
	public void betweenAccounts_POST_callsTransferAndRedirects() throws Exception {
		when(userService.findByUsername("john")).thenReturn(user);

		mockMvc.perform(post("/transfer/betweenAccounts")
				.param("transferFrom", "Primary")
				.param("transferTo", "Savings")
				.param("amount", "50")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("redirect:/userFront"));

		verify(transactionService).betweenAccountsTransfer(eq("Primary"), eq("Savings"), eq("50"),
				eq(user.getPrimaryAccount()), eq(user.getSavingsAccount()));
	}

	@Test
	public void recipient_GET_returnsRecipientListView() throws Exception {
		List<Recipient> recipientList = new ArrayList<Recipient>();
		when(transactionService.findRecipientList(principal)).thenReturn(recipientList);

		mockMvc.perform(get("/transfer/recipient").principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("recipient"))
				.andExpect(model().attribute("recipientList", recipientList))
				.andExpect(model().attributeExists("recipient"));
	}

	@Test
	public void recipientSave_POST_savesAndRedirects() throws Exception {
		when(userService.findByUsername("john")).thenReturn(user);

		mockMvc.perform(post("/transfer/recipient/save")
				.param("name", "Jane")
				.param("email", "jane@example.com")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("redirect:/transfer/recipient"));

		verify(transactionService).saveRecipient(any(Recipient.class));
	}

	@Test
	public void recipientDelete_GET_deletesAndReturnsView() throws Exception {
		List<Recipient> recipientList = new ArrayList<Recipient>();
		when(transactionService.findRecipientList(principal)).thenReturn(recipientList);

		mockMvc.perform(get("/transfer/recipient/delete")
				.param("recipientName", "Jane")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("recipient"))
				.andExpect(model().attribute("recipientList", recipientList));

		verify(transactionService).deleteRecipientByName("Jane");
	}

	@Test
	public void toSomeoneElse_POST_callsTransferAndRedirects() throws Exception {
		Recipient recipient = new Recipient();
		recipient.setName("Jane");
		when(userService.findByUsername("john")).thenReturn(user);
		when(transactionService.findRecipientByName("Jane")).thenReturn(recipient);

		mockMvc.perform(post("/transfer/toSomeoneElse")
				.param("recipientName", "Jane")
				.param("accountType", "Primary")
				.param("amount", "25")
				.principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("redirect:/userFront"));

		verify(transactionService).toSomeoneElseTransfer(eq(recipient), eq("Primary"), eq("25"),
				eq(user.getPrimaryAccount()), eq(user.getSavingsAccount()));
	}
}
