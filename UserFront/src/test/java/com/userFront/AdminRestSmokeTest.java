package com.userFront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.userFront.dao.AppointmentDao;
import com.userFront.dao.PrimaryAccountDao;
import com.userFront.dao.PrimaryTransactionDao;
import com.userFront.dao.RecipientDao;
import com.userFront.dao.RoleDao;
import com.userFront.dao.SavingsAccountDao;
import com.userFront.dao.SavingsTransactionDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.Appointment;
import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.Recipient;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.golden.GoldenFixtures;
import com.userFront.golden.JsonParseEquivalence;

/**
 * Regression contract for the admin REST API consumed by the AdminPortal, captured on Boot 1.5.4.
 *
 * <p>Responses are compared with {@link JsonParseEquivalence} against the committed
 * {@code golden/boot15/*.json} fixtures; regenerate them with {@code -Dgolden.update=true}. The
 * fixtures hold the response bodies as served, restricted to the {@value #PREFIX}* rows this class
 * seeds (other smoke tests share the JVM, context and database) and pretty-printed. Generated ids
 * are compared for presence and type only; everything else, including account numbers, balances,
 * dates and the exposed password hash, is seeded with fixed values.
 *
 * <p>Rows are written directly through the DAOs with fixed account numbers rather than through
 * signup, whose static account-number counter restarts per JVM (COG-1179).
 */
public class AdminRestSmokeTest extends AbstractIntegrationTest {

	private static final String PREFIX = "adminsmoke-";

	private static final String ALICE = PREFIX + "alice";

	private static final String BOB = PREFIX + "bob";

	/** Only used by the enable/disable test, so its {@code enabled} flag never leaks into a fixture. */
	private static final String TOGGLE = PREFIX + "toggle";

	private static final String PASSWORD_HASH = "adminsmoke-fixed-password-hash";

	/**
	 * {@code User.recipientList} has no {@code @JsonIgnore}, so {@code /api/user/all} (and the
	 * nested {@code user} of {@code /api/appointment/all}) currently serialises every user's
	 * recipients. COG-1150 adds the annotation: flip this to {@code false} and regenerate
	 * {@code user-all.json} and {@code appointment-all.json} in that PR.
	 */
	private static final boolean RECIPIENT_LIST_IN_USER_ALL = true;

	private static final JsonParseEquivalence EQUIVALENCE = JsonParseEquivalence.create()
			.ignoringValuesOf("id", "userId");

	private static final RequestPostProcessor ADMIN = user(PREFIX + "admin").roles("ADMIN");

	private static final RequestPostProcessor CUSTOMER = user(ALICE).roles("USER");

	@Autowired
	private UserDao userDao;

	@Autowired
	private RoleDao roleDao;

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

	@Autowired
	private AppointmentDao appointmentDao;

	/**
	 * The enable/disable request. Both endpoints are plain {@code @RequestMapping}s reached with GET
	 * today; COG-1154 moves them to POST, which only needs this method to change.
	 */
	private static MockHttpServletRequestBuilder toggleUser(String username, String action) {
		return get("/api/user/{username}/{action}", username, action);
	}

	@Before
	public void seed() {
		if (userDao.findByUsername(ALICE) != null) {
			return;
		}
		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");
		role = roleDao.save(role);

		User alice = saveUser(ALICE, "Alice", "Admin-Smoke", "5550001001", 91145001, "1500.00", 91145002, "250.75",
				role);
		User bob = saveUser(BOB, "Bob", "Admin-Smoke", "5550001002", 91145003, "0.00", 91145004, "0.00", role);
		saveUser(TOGGLE, "Toggle", "Admin-Smoke", "5550001003", 91145005, "0.00", 91145006, "0.00", role);

		Recipient recipient = new Recipient();
		recipient.setName(PREFIX + "landlord");
		recipient.setEmail("adminsmoke-landlord@example.com");
		recipient.setPhone("5550002001");
		recipient.setAccountNumber("99887766");
		recipient.setDescription("Rent");
		recipient.setUser(alice);
		recipientDao.save(recipient);

		primaryTransactionDao.save(new PrimaryTransaction(at("2017-06-15T10:15:30Z"), "Deposit to Primary Account",
				"Account", "Finished", 2000.0, new BigDecimal("2000.00"), alice.getPrimaryAccount()));
		primaryTransactionDao.save(new PrimaryTransaction(at("2017-06-16T09:00:00Z"),
				"Transfer to recipient " + recipient.getName(), "Transfer", "Finished", 500.0,
				new BigDecimal("1500.00"), alice.getPrimaryAccount()));
		savingsTransactionDao.save(new SavingsTransaction(at("2017-06-17T14:30:00Z"), "Deposit to savings Account",
				"Account", "Finished", 250.75, new BigDecimal("250.75"), alice.getSavingsAccount()));

		saveAppointment(alice, "2017-07-01T15:00:00Z", "Chicago branch", "Mortgage consultation", false);
		saveAppointment(bob, "2017-07-02T16:30:00Z", "New York branch", "Open a savings account", true);
	}

	@Test
	public void userAllMatchesBoot15Fixture() throws Exception {
		JsonNode users = getJsonAsAdmin("/api/user/all");
		assertTrue(users.isArray());

		GoldenFixtures.assertMatches(GoldenFixtures.BOOT15, "user-all", onlyUsers(users, ALICE, BOB), EQUIVALENCE);
	}

	@Test
	public void userAllSerialisesRecipientList() throws Exception {
		JsonNode alice = onlyUsers(getJsonAsAdmin("/api/user/all"), ALICE).get(0);

		assertEquals(RECIPIENT_LIST_IN_USER_ALL, alice.has("recipientList"));
		if (RECIPIENT_LIST_IN_USER_ALL) {
			assertEquals(1, alice.get("recipientList").size());
			assertEquals(PREFIX + "landlord", alice.get("recipientList").get(0).get("name").asText());
		}
	}

	@Test
	public void primaryTransactionsMatchBoot15Fixture() throws Exception {
		JsonNode transactions = getJsonAsAdmin("/api/user/primary/transaction?username=" + ALICE);

		GoldenFixtures.assertMatches(GoldenFixtures.BOOT15, "user-primary-transaction", sortedByDate(transactions),
				EQUIVALENCE);
	}

	@Test
	public void savingsTransactionsMatchBoot15Fixture() throws Exception {
		JsonNode transactions = getJsonAsAdmin("/api/user/savings/transaction?username=" + ALICE);

		GoldenFixtures.assertMatches(GoldenFixtures.BOOT15, "user-savings-transaction", sortedByDate(transactions),
				EQUIVALENCE);
	}

	@Test
	public void appointmentAllMatchesBoot15Fixture() throws Exception {
		JsonNode appointments = getJsonAsAdmin("/api/appointment/all");
		assertTrue(appointments.isArray());

		List<JsonNode> seeded = new ArrayList<>();
		for (JsonNode appointment : appointments) {
			if (appointment.path("user").path("username").asText().startsWith(PREFIX)) {
				seeded.add(appointment);
			}
		}
		GoldenFixtures.assertMatches(GoldenFixtures.BOOT15, "appointment-all", sortedByDate(seeded), EQUIVALENCE);
	}

	@Test
	public void disableAndEnableFlipTheEnabledFlag() throws Exception {
		mockMvc.perform(toggleUser(TOGGLE, "disable").with(ADMIN))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
		assertFalse(userDao.findByUsername(TOGGLE).isEnabled());

		mockMvc.perform(toggleUser(TOGGLE, "enable").with(ADMIN))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
		assertTrue(userDao.findByUsername(TOGGLE).isEnabled());
	}

	@Test
	public void customerRoleIsForbiddenFromAdminEndpoints() throws Exception {
		List<MockHttpServletRequestBuilder> adminRequests = Arrays.asList(
				get("/api/user/all"),
				get("/api/user/primary/transaction").param("username", ALICE),
				get("/api/user/savings/transaction").param("username", ALICE),
				get("/api/appointment/all"),
				toggleUser(TOGGLE, "disable"),
				toggleUser(TOGGLE, "enable"));

		for (MockHttpServletRequestBuilder request : adminRequests) {
			mockMvc.perform(request.with(CUSTOMER)).andExpect(status().isForbidden());
		}
		assertTrue(userDao.findByUsername(TOGGLE).isEnabled());
	}

	/** Reads and parses the whole body, so a lazy-loading failure anywhere in it fails the test. */
	private JsonNode getJsonAsAdmin(String url) throws Exception {
		String body = mockMvc.perform(get(url).with(ADMIN))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();
		return JsonParseEquivalence.parse(body);
	}

	/** The endpoints query without ORDER BY, so rows are compared in date order rather than as returned. */
	private static ArrayNode sortedByDate(Iterable<JsonNode> rows) {
		List<JsonNode> sorted = new ArrayList<>();
		rows.forEach(sorted::add);
		sorted.sort(Comparator.comparing(row -> JsonParseEquivalence.parseInstant(row.get("date"))));
		return JsonNodeFactory.instance.arrayNode().addAll(sorted);
	}

	private static ArrayNode onlyUsers(JsonNode users, String... usernames) {
		ArrayNode selected = JsonNodeFactory.instance.arrayNode();
		for (String username : usernames) {
			JsonNode match = null;
			for (JsonNode user : users) {
				if (username.equals(user.path("username").asText())) {
					match = user;
				}
			}
			assertNotNull("No " + username + " in " + users, match);
			selected.add(match);
		}
		return selected;
	}

	private User saveUser(String username, String firstName, String lastName, String phone, int primaryNumber,
			String primaryBalance, int savingsNumber, String savingsBalance, Role role) {
		PrimaryAccount primary = new PrimaryAccount();
		primary.setAccountNumber(primaryNumber);
		primary.setAccountBalance(new BigDecimal(primaryBalance));
		SavingsAccount savings = new SavingsAccount();
		savings.setAccountNumber(savingsNumber);
		savings.setAccountBalance(new BigDecimal(savingsBalance));

		User user = new User();
		user.setUsername(username);
		user.setPassword(PASSWORD_HASH);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(username + "@example.com");
		user.setPhone(phone);
		user.setPrimaryAccount(primaryAccountDao.save(primary));
		user.setSavingsAccount(savingsAccountDao.save(savings));
		user.getUserRoles().add(new UserRole(user, role));
		return userDao.save(user);
	}

	private void saveAppointment(User user, String date, String location, String description, boolean confirmed) {
		Appointment appointment = new Appointment();
		appointment.setUser(user);
		appointment.setDate(at(date));
		appointment.setLocation(location);
		appointment.setDescription(description);
		appointment.setConfirmed(confirmed);
		appointmentDao.save(appointment);
	}

	private static Date at(String isoInstant) {
		return Date.from(Instant.parse(isoInstant));
	}
}
