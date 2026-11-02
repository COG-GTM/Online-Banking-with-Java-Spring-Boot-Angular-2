package com.userFront.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.security.Principal;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

public class TransferControllerTest {

	private static final Principal MALLORY = new Principal() {
		@Override
		public String getName() {
			return "mallory";
		}
	};

	@Mock
	private TransactionService transactionService;

	@Mock
	private UserService userService;

	@InjectMocks
	private TransferController transferController;

	private MockMvc mockMvc;

	private User mallory;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(transferController)
				.setViewResolvers(new InternalResourceViewResolver("/templates/", ".html")).build();
		mallory = new User();
		mallory.setUsername("mallory");
		when(userService.findByUsername("mallory")).thenReturn(mallory);
		when(transactionService.findRecipientList(any(Principal.class)))
				.thenReturn(Collections.<Recipient>emptyList());
	}

	@Test
	public void editLooksUpRecipientForCallerAndReturns404WhenNotOwned() throws Exception {
		mockMvc.perform(get("/transfer/recipient/edit").param("recipientName", "Bob Smith").principal(MALLORY))
				.andExpect(status().isNotFound());

		verify(transactionService).findRecipientByName("Bob Smith", mallory);
	}

	@Test
	public void editRendersCallersOwnRecipient() throws Exception {
		Recipient own = new Recipient();
		own.setName("Bob Smith");
		when(transactionService.findRecipientByName("Bob Smith", mallory)).thenReturn(own);

		mockMvc.perform(get("/transfer/recipient/edit").param("recipientName", "Bob Smith").principal(MALLORY))
				.andExpect(status().isOk()).andExpect(view().name("recipient"))
				.andExpect(model().attribute("recipient", own));
	}

	@Test
	public void deleteIsScopedToCallerAndReturns404WhenNothingDeleted() throws Exception {
		when(transactionService.deleteRecipientByName("Bob Smith", mallory)).thenReturn(0L);

		mockMvc.perform(get("/transfer/recipient/delete").param("recipientName", "Bob Smith").principal(MALLORY))
				.andExpect(status().isNotFound());

		verify(transactionService).deleteRecipientByName("Bob Smith", mallory);
	}

	@Test
	public void deleteOfOwnRecipientRendersList() throws Exception {
		when(transactionService.deleteRecipientByName("Bob Smith", mallory)).thenReturn(1L);

		mockMvc.perform(get("/transfer/recipient/delete").param("recipientName", "Bob Smith").principal(MALLORY))
				.andExpect(status().isOk()).andExpect(view().name("recipient"));
	}

	@Test
	public void transferToRecipientNotOwnedReturns404AndMovesNoMoney() throws Exception {
		mockMvc.perform(post("/transfer/toSomeoneElse").param("recipientName", "Bob Smith")
				.param("accountType", "Primary").param("amount", "10").principal(MALLORY))
				.andExpect(status().isNotFound());

		verify(transactionService).findRecipientByName("Bob Smith", mallory);
		verify(transactionService, never()).toSomeoneElseTransfer(any(Recipient.class), anyString(), anyString(),
				any(com.userFront.domain.PrimaryAccount.class), any(com.userFront.domain.SavingsAccount.class));
	}

	@Test
	public void saveWithIdOwnedByAnotherUserReturns404() throws Exception {
		mockMvc.perform(post("/transfer/recipient/save").param("id", "42").param("name", "Bob Smith")
				.principal(MALLORY)).andExpect(status().isNotFound());

		verify(transactionService).findRecipientByIdAndUser(42L, mallory);
		verify(transactionService, never()).saveRecipient(any(Recipient.class));
	}

	@Test
	public void saveWithOwnIdUpdatesRecipient() throws Exception {
		when(transactionService.findRecipientByIdAndUser(eq(42L), eq(mallory))).thenReturn(new Recipient());

		mockMvc.perform(post("/transfer/recipient/save").param("id", "42").param("name", "Bob Smith")
				.principal(MALLORY)).andExpect(redirectedUrl("/transfer/recipient"));

		verify(transactionService).saveRecipient(any(Recipient.class));
	}

	@Test
	public void saveNewRecipientSkipsOwnershipLookup() throws Exception {
		mockMvc.perform(post("/transfer/recipient/save").param("name", "Bob Smith").principal(MALLORY))
				.andExpect(redirectedUrl("/transfer/recipient"));

		verify(transactionService, never()).findRecipientByIdAndUser(any(Long.class), any(User.class));
		verify(transactionService).saveRecipient(any(Recipient.class));
	}
}
