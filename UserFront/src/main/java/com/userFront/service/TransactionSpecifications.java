package com.userFront.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Builds the server-side query for the account pages' transaction filters.
 * The stored {@code type} column is not consistent (deposits, withdrawals and
 * some transfers are all stored as "Account"), so the type filter matches on
 * the description the app writes for each kind of transaction.
 */
public final class TransactionSpecifications {

	private static final char ESCAPE = '\\';

	private TransactionSpecifications() {
	}

	public static <T> Specification<T> forAccount(String accountField, Object account, TransactionFilter filter) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get(accountField), account));

			if (filter == null) {
				return cb.and(predicates.toArray(new Predicate[0]));
			}

			if (filter.getFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.<Date>get("date"), startOfDay(filter.getFrom(), 0)));
			}
			if (filter.getTo() != null) {
				predicates.add(cb.lessThan(root.<Date>get("date"), startOfDay(filter.getTo(), 1)));
			}
			if (filter.getMinAmount() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.<Double>get("amount"), filter.getMinAmount()));
			}
			if (filter.getMaxAmount() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.<Double>get("amount"), filter.getMaxAmount()));
			}

			Expression<String> description = cb.lower(root.<String>get("description"));
			String typePattern = typePattern(filter.getType());
			if (typePattern != null) {
				predicates.add(cb.like(description, typePattern));
			}
			if (StringUtils.hasText(filter.getQ())) {
				String text = escapeLike(filter.getQ().trim().toLowerCase(Locale.ROOT));
				predicates.add(cb.like(description, "%" + text + "%", ESCAPE));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	static String typePattern(String type) {
		if (type == null) {
			return null;
		}
		switch (type.trim().toLowerCase(Locale.ROOT)) {
		case TransactionFilter.TYPE_DEPOSIT:
			return "deposit%";
		case TransactionFilter.TYPE_WITHDRAW:
			return "withdraw%";
		case TransactionFilter.TYPE_TRANSFER:
			return "%transfer%";
		default:
			return null;
		}
	}

	private static String escapeLike(String text) {
		return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	static Date startOfDay(LocalDate day, int plusDays) {
		return Date.from(day.plusDays(plusDays).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
