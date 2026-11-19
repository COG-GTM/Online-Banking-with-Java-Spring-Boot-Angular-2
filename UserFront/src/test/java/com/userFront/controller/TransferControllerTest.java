package com.userFront.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;
import static org.junit.Assert.assertSame;

import java.security.Principal;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TransferControllerTest {

	private static final Principal BOB_PRINCIPAL = () -> "bob";

	@Mock
	private TransactionService transactionService;

	@Mock
	private UserService userService;

	@InjectMocks
	private TransferController controller;

	private MockMvc mockMvc;
	private User bob;

	@Before
	public void setUp() {
		bob = new User();
		bob.setUsername("bob");
		when(userService.findByUsername("bob")).thenReturn(bob);

		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/templates/");
		viewResolver.setSuffix(".html");
		mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();
	}

	@Test
	public void editingRecipientNotOwnedByUserReturnsNotFound() throws Exception {
		when(transactionService.findRecipientByNameAndUser("Landlord", bob)).thenReturn(null);

		mockMvc.perform(get("/transfer/recipient/edit")
				.param("recipientName", "Landlord")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().isNotFound());
	}

	@Test
	public void deleteRecipientRequiresPost() throws Exception {
		mockMvc.perform(get("/transfer/recipient/delete")
				.param("recipientName", "Landlord")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().isMethodNotAllowed());
	}

	@Test
	public void deletingRecipientNotOwnedByUserReturnsNotFound() throws Exception {
		when(transactionService.findRecipientByNameAndUser("Landlord", bob)).thenReturn(null);

		mockMvc.perform(post("/transfer/recipient/delete")
				.param("recipientName", "Landlord")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().isNotFound());

		verify(transactionService, never()).deleteRecipientByNameAndUser("Landlord", bob);
	}

	@Test
	public void deletingOwnedRecipientDeletesIt() throws Exception {
		when(transactionService.findRecipientByNameAndUser("Landlord", bob)).thenReturn(new Recipient());
		when(transactionService.findRecipientList(BOB_PRINCIPAL)).thenReturn(Collections.<Recipient>emptyList());

		mockMvc.perform(post("/transfer/recipient/delete")
				.param("recipientName", "Landlord")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().isOk());

		verify(transactionService).deleteRecipientByNameAndUser("Landlord", bob);
	}

	@Test
	public void savingRecipientIdNotOwnedByUserReturnsNotFound() throws Exception {
		when(transactionService.findRecipientByIdAndUser(5L, bob)).thenReturn(null);

		mockMvc.perform(post("/transfer/recipient/save")
				.param("id", "5")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().isNotFound());

		verify(transactionService, never()).saveRecipient(org.mockito.Matchers.any(Recipient.class));
	}

	@Test
	public void savingOwnedRecipientRedirects() throws Exception {
		when(transactionService.findRecipientByIdAndUser(5L, bob)).thenReturn(new Recipient());

		mockMvc.perform(post("/transfer/recipient/save")
				.param("id", "5")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/transfer/recipient"));

		ArgumentCaptor<Recipient> recipientCaptor = ArgumentCaptor.forClass(Recipient.class);
		verify(transactionService).saveRecipient(recipientCaptor.capture());
		assertSame(bob, recipientCaptor.getValue().getUser());
	}

	@Test
	public void savingRecipientWithoutIdCreatesIt() throws Exception {
		mockMvc.perform(post("/transfer/recipient/save")
				.param("name", "Landlord")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/transfer/recipient"));

		ArgumentCaptor<Recipient> recipientCaptor = ArgumentCaptor.forClass(Recipient.class);
		verify(transactionService).saveRecipient(recipientCaptor.capture());
		assertSame(bob, recipientCaptor.getValue().getUser());
	}

	@Test
	public void transferringToRecipientNotOwnedByUserReturnsNotFound() throws Exception {
		when(transactionService.findRecipientByNameAndUser("Landlord", bob)).thenReturn(null);

		mockMvc.perform(post("/transfer/toSomeoneElse")
				.param("recipientName", "Landlord")
				.param("accountType", "Primary")
				.param("amount", "10")
				.principal(BOB_PRINCIPAL))
				.andExpect(status().isNotFound());

		verify(transactionService, never()).toSomeoneElseTransfer(
				org.mockito.Matchers.any(Recipient.class), eq("Primary"), eq("10"), eq(bob.getPrimaryAccount()),
				eq(bob.getSavingsAccount()));
	}
}
