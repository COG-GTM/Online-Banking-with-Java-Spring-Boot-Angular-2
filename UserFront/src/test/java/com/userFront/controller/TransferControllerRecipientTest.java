package com.userFront.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.Recipient;
import com.userFront.exception.RecipientNotFoundException;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class TransferControllerRecipientTest {

	@Mock
	private TransactionService transactionService;

	@Mock
	private UserService userService;

	@InjectMocks
	private TransferController transferController;

	private MockMvc mockMvc;

	private final Principal mallory = () -> "mallory";

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(transferController).build();
	}

	@Test
	public void editOfUnownedRecipientReturns404() throws Exception {
		when(transactionService.findRecipientByName("Bob Smith", mallory)).thenThrow(new RecipientNotFoundException());

		mockMvc.perform(get("/transfer/recipient/edit").param("recipientName", "Bob Smith").principal(mallory))
				.andExpect(status().isNotFound());
	}

	@Test
	public void deleteOfUnownedRecipientReturns404() throws Exception {
		doThrow(new RecipientNotFoundException()).when(transactionService).deleteRecipientByName("Bob Smith", mallory);

		mockMvc.perform(get("/transfer/recipient/delete").param("recipientName", "Bob Smith").principal(mallory))
				.andExpect(status().isNotFound());
	}

	@Test
	public void transferToUnownedRecipientReturns404() throws Exception {
		when(transactionService.findRecipientByName("Bob Smith", mallory)).thenThrow(new RecipientNotFoundException());

		mockMvc.perform(post("/transfer/toSomeoneElse").param("recipientName", "Bob Smith")
				.param("accountType", "Primary").param("amount", "5").principal(mallory))
				.andExpect(status().isNotFound());

		verify(transactionService, never()).toSomeoneElseTransfer(any(), any(), any(), any(), any());
	}

	@Test
	public void saveWithUnownedIdReturns404() throws Exception {
		when(transactionService.saveRecipient(any(Recipient.class), eq(mallory))).thenThrow(new RecipientNotFoundException());

		mockMvc.perform(post("/transfer/recipient/save").param("id", "42").param("name", "Bob Smith").principal(mallory))
				.andExpect(status().isNotFound());
	}
}
