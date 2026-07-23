package com.userFront;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.userFront.controller.TransferController;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

/**
 * MockMvc tests for {@link TransferController}. The service layer is mocked so
 * we can assert the controller wires the request parameters and the
 * authenticated user's accounts into the transfer service and issues the
 * expected redirect. Security is disabled for the slice so the tests focus on
 * MVC behaviour.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TransferController.class, secure = false)
public class TransferControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TransactionService transactionService;

	@MockBean
	private UserService userService;

	private User user;
	private PrimaryAccount primaryAccount;
	private SavingsAccount savingsAccount;
	private Principal principal;

	@Before
	public void setUp() {
		primaryAccount = new PrimaryAccount();
		primaryAccount.setAccountNumber(11223501);
		primaryAccount.setAccountBalance(new BigDecimal("1000.00"));

		savingsAccount = new SavingsAccount();
		savingsAccount.setAccountNumber(11223502);
		savingsAccount.setAccountBalance(new BigDecimal("500.00"));

		user = new User();
		user.setUsername("john");
		user.setPrimaryAccount(primaryAccount);
		user.setSavingsAccount(savingsAccount);

		principal = new Principal() {
			@Override
			public String getName() {
				return "john";
			}
		};

		when(userService.findByUsername("john")).thenReturn(user);
	}

	@Test
	public void betweenAccountsGetReturnsView() throws Exception {
		mockMvc.perform(get("/transfer/betweenAccounts").principal(principal))
				.andExpect(status().isOk())
				.andExpect(view().name("betweenAccounts"));
	}

	@Test
	public void betweenAccountsPostInvokesTransferAndRedirects() throws Exception {
		mockMvc.perform(post("/transfer/betweenAccounts")
				.principal(principal)
				.param("transferFrom", "Primary")
				.param("transferTo", "Savings")
				.param("amount", "250"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		verify(transactionService).betweenAccountsTransfer(eq("Primary"), eq("Savings"), eq("250"),
				eq(primaryAccount), eq(savingsAccount));
	}

	@Test
	public void toSomeoneElsePostInvokesTransferAndRedirects() throws Exception {
		Recipient recipient = new Recipient();
		recipient.setName("Alice");
		when(transactionService.findRecipientByName("Alice")).thenReturn(recipient);

		mockMvc.perform(post("/transfer/toSomeoneElse")
				.principal(principal)
				.param("recipientName", "Alice")
				.param("accountType", "Primary")
				.param("amount", "300"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		verify(transactionService).toSomeoneElseTransfer(eq(recipient), eq("Primary"), eq("300"),
				eq(primaryAccount), eq(savingsAccount));
	}
}
