package com.userFront.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.DataBinder;

public class TransactionFilterDateTest {

	private TimeZone defaultZone;

	@Before
	public void westOfUtc() {
		defaultZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
	}

	@After
	public void restoreZone() {
		TimeZone.setDefault(defaultZone);
	}

	@Test
	public void boundDatesKeepTheSelectedDayOnServersWestOfUtc() {
		TransactionFilter filter = bind("2026-11-03", "2026-11-03");

		SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		assertThat(local.format(TransactionSpecifications.startOfDay(filter.getFrom(), 0))).isEqualTo("2026-11-03 00:00");
		assertThat(local.format(TransactionSpecifications.startOfDay(filter.getTo(), 1))).isEqualTo("2026-11-04 00:00");
	}

	private static TransactionFilter bind(String from, String to) {
		TransactionFilter filter = new TransactionFilter();
		DataBinder binder = new DataBinder(filter, "filter");
		binder.setConversionService(new DefaultFormattingConversionService());
		MutablePropertyValues values = new MutablePropertyValues();
		values.add("from", from);
		values.add("to", to);
		binder.bind(values);
		assertThat(binder.getBindingResult().hasErrors()).isFalse();
		return filter;
	}
}
