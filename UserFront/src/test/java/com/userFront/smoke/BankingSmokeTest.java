package com.userFront.smoke;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

@SpringBootTest
class BankingSmokeTest {

	private static final String PASSWORD = "p@ssw0rd";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private UserService userService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		Filter springSecurityFilterChain = context.getBean("springSecurityFilterChain", Filter.class);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
		ensureRole(1, "ROLE_USER");
		ensureRole(2, "ROLE_ADMIN");
	}

	@Test
	void signupLoginAndLogout() throws Exception {
		String username = uniqueUsername();

		mockMvc.perform(post("/signup")
				.param("username", username)
				.param("password", PASSWORD)
				.param("email", username + "@example.com")
				.param("firstName", "Smoke")
				.param("lastName", "Test"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));

		MockHttpSession session = login(username);

		mockMvc.perform(get("/userFront").session(session))
				.andExpect(status().isOk());

		mockMvc.perform(get("/logout").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/index?logout"));

		mockMvc.perform(get("/userFront").session(session))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/index"));
	}

	@Test
	void loginWithWrongPasswordIsRejected() throws Exception {
		String username = uniqueUsername();
		createUser(username, "ROLE_USER");

		mockMvc.perform(post("/index").param("username", username).param("password", "wrong"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/index?error"));
	}

	@Test
	void depositAndTransferBetweenAccounts() throws Exception {
		String username = uniqueUsername();
		createUser(username, "ROLE_USER");
		MockHttpSession session = login(username);

		mockMvc.perform(post("/account/deposit").session(session)
				.param("accountType", "Primary")
				.param("amount", "100"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		mockMvc.perform(post("/transfer/betweenAccounts").session(session)
				.param("transferFrom", "Primary")
				.param("transferTo", "Savings")
				.param("amount", "40"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"));

		User user = userService.findByUsername(username);
		assertThat(user.getPrimaryAccount().getAccountBalance()).isEqualByComparingTo(new BigDecimal("60"));
		assertThat(user.getSavingsAccount().getAccountBalance()).isEqualByComparingTo(new BigDecimal("40"));
	}

	@Test
	void adminApiRequiresAdminRole() throws Exception {
		String customer = uniqueUsername();
		createUser(customer, "ROLE_USER");
		String admin = uniqueUsername();
		createUser(admin, "ROLE_ADMIN");

		mockMvc.perform(get("/api/user/all").session(login(customer)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/user/all").session(login(admin)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(customer)));

		mockMvc.perform(get("/api/appointment/all").session(login(admin)))
				.andExpect(status().isOk());
	}

	private MockHttpSession login(String username) throws Exception {
		return (MockHttpSession) mockMvc.perform(post("/index")
				.param("username", username)
				.param("password", PASSWORD))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/userFront"))
				.andReturn()
				.getRequest()
				.getSession();
	}

	private void createUser(String username, String roleName) {
		User user = new User();
		user.setUsername(username);
		user.setPassword(PASSWORD);
		user.setEmail(username + "@example.com");
		user.setFirstName("Smoke");
		user.setLastName("Test");
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, roleDao.findByName(roleName)));
		userService.createUser(user, userRoles);
	}

	private void ensureRole(int roleId, String name) {
		if (roleDao.findByName(name) == null) {
			Role role = new Role();
			role.setRoleId(roleId);
			role.setName(name);
			roleDao.save(role);
		}
	}

	private static String uniqueUsername() {
		return "smoke" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
