package com.userFront.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;
import com.userFront.service.TransactionFilter;
import com.userFront.service.TransactionSpecifications;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
public class TransactionSearchTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private PrimaryTransactionDao primaryTransactionDao;

	@Autowired
	private SavingsTransactionDao savingsTransactionDao;

	private PrimaryAccount primary;
	private PrimaryAccount otherPrimary;
	private SavingsAccount savings;

	@Before
	public void setUp() throws ParseException {
		primary = em.persist(new PrimaryAccount());
		otherPrimary = em.persist(new PrimaryAccount());
		savings = em.persist(new SavingsAccount());

		primary("2026-11-01 09:00", "Deposit to Primary Account", "Account", 500);
		primary("2026-11-03 18:30", "Withdraw from Primary Account", "Account", 40);
		primary("2026-11-05 12:00", "Between account transfer from Primary to Savings", "Account", 100);
		primary("2026-11-07 23:59", "Transfer to recipient Jane_Doe 100%", "Transfer", 75.5);
		em.persist(new PrimaryTransaction(date("2026-11-03 10:00"), "Deposit to Primary Account", "Account",
				"Finished", 999, BigDecimal.ZERO, otherPrimary));

		em.persist(new SavingsTransaction(date("2026-11-02 10:00"), "Deposit to savings Account", "Account",
				"Finished", 1000, BigDecimal.ZERO, savings));
		em.persist(new SavingsTransaction(date("2026-11-04 10:00"), "Transfer to recipient Sam", "Transfer",
				"Finished", 20, BigDecimal.ZERO, savings));
		em.flush();
	}

	@Test
	public void noFilterReturnsOnlyThisAccountsTransactions() {
		assertThat(primarySearch(new TransactionFilter())).hasSize(4);
	}

	@Test
	public void typeMatchesWhatTheTransactionDid() {
		assertThat(primarySearch(type("deposit"))).containsExactly("Deposit to Primary Account");
		assertThat(primarySearch(type("withdraw"))).containsExactly("Withdraw from Primary Account");
		assertThat(primarySearch(type("transfer"))).containsExactlyInAnyOrder(
				"Between account transfer from Primary to Savings", "Transfer to recipient Jane_Doe 100%");
	}

	@Test
	public void dateRangeIncludesTheWholeEndDay() throws ParseException {
		TransactionFilter filter = new TransactionFilter();
		filter.setFrom(day("2026-11-03"));
		filter.setTo(day("2026-11-07"));
		assertThat(primarySearch(filter)).hasSize(3).doesNotContain("Deposit to Primary Account");
	}

	@Test
	public void amountRangeIsInclusive() {
		TransactionFilter filter = new TransactionFilter();
		filter.setMinAmount(40.0);
		filter.setMaxAmount(100.0);
		assertThat(primarySearch(filter)).hasSize(3).doesNotContain("Deposit to Primary Account");
	}

	@Test
	public void freeTextIsCaseInsensitiveAndLiteral() {
		TransactionFilter filter = new TransactionFilter();
		filter.setQ("  JANE_doe 100% ");
		assertThat(primarySearch(filter)).containsExactly("Transfer to recipient Jane_Doe 100%");

		filter.setQ("_");
		assertThat(primarySearch(filter)).containsExactly("Transfer to recipient Jane_Doe 100%");
	}

	@Test
	public void filtersCombineAndCanMatchNothing() {
		TransactionFilter filter = type("transfer");
		filter.setMinAmount(80.0);
		assertThat(primarySearch(filter)).containsExactly("Between account transfer from Primary to Savings");

		filter.setQ("no such text");
		assertThat(primarySearch(filter)).isEmpty();
	}

	@Test
	public void savingsSearchUsesTheSavingsAccount() {
		List<SavingsTransaction> result = savingsTransactionDao
				.findAll(TransactionSpecifications.forAccount("savingsAccount", savings, type("transfer")));
		assertThat(result).extracting("description").containsExactly("Transfer to recipient Sam");
	}

	private List<String> primarySearch(TransactionFilter filter) {
		return primaryTransactionDao.findAll(TransactionSpecifications.forAccount("primaryAccount", primary, filter))
				.stream().map(PrimaryTransaction::getDescription).collect(Collectors.toList());
	}

	private void primary(String when, String description, String type, double amount) throws ParseException {
		em.persist(new PrimaryTransaction(date(when), description, type, "Finished", amount, BigDecimal.ZERO,
				primary));
	}

	private static TransactionFilter type(String type) {
		TransactionFilter filter = new TransactionFilter();
		filter.setType(type);
		return filter;
	}

	private static Date date(String value) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(value);
	}

	private static LocalDate day(String value) {
		return LocalDate.parse(value);
	}
}
