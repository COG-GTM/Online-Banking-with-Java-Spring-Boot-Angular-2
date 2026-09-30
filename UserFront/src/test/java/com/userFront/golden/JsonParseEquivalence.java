package com.userFront.golden;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Asserts that two JSON documents are <em>parse-equivalent</em>: same structure, same field names,
 * same values after parsing, rather than the same string.
 *
 * <ul>
 * <li>Objects must have exactly the same field names (missing and unexpected fields both fail),
 * arrays the same length and order.</li>
 * <li>Numbers compare by value ({@code 1000}, {@code 1000.0} and {@code 1000.00} are equal).</li>
 * <li>Values of {@linkplain #withDateFields date fields} (default {@value #DEFAULT_DATE_FIELD})
 * compare as instants: epoch milliseconds (Jackson 2 / Boot 1.5 default) and ISO-8601 strings with
 * a {@code +0000}, {@code +00:00} or {@code Z} offset are equal when they denote the same
 * instant.</li>
 * <li>Values of {@linkplain #ignoringValuesOf presence-only fields} (e.g. generated ids) must be
 * present with the same JSON type on both sides, but their values are not compared.</li>
 * </ul>
 *
 * <p>Public API for the migration waves: later waves assert live responses against the
 * {@code golden/boot15} fixtures with this class (see {@link GoldenFixtures}).
 */
public final class JsonParseEquivalence {

	public static final String DEFAULT_DATE_FIELD = "date";

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));

	private static final DateTimeFormatter ISO_WITH_OFFSET = new DateTimeFormatterBuilder()
			.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			.optionalStart().appendOffset("+HH:MM", "Z").optionalEnd()
			.optionalStart().appendOffset("+HHMM", "Z").optionalEnd()
			.optionalStart().appendOffset("+HH", "Z").optionalEnd()
			.toFormatter();

	private final Set<String> dateFields;

	private final Set<String> presenceOnlyFields;

	private JsonParseEquivalence(Set<String> dateFields, Set<String> presenceOnlyFields) {
		this.dateFields = Collections.unmodifiableSet(dateFields);
		this.presenceOnlyFields = Collections.unmodifiableSet(presenceOnlyFields);
	}

	/** Treats {@value #DEFAULT_DATE_FIELD} as a date field and compares every other value. */
	public static JsonParseEquivalence create() {
		return new JsonParseEquivalence(new LinkedHashSet<>(Collections.singleton(DEFAULT_DATE_FIELD)),
				new LinkedHashSet<>());
	}

	/** Adds field names whose values are compared as instants. */
	public JsonParseEquivalence withDateFields(String... fieldNames) {
		Set<String> dates = new LinkedHashSet<>(dateFields);
		dates.addAll(Arrays.asList(fieldNames));
		return new JsonParseEquivalence(dates, presenceOnlyFields);
	}

	/** Adds field names that must be present with the same JSON type, but whose values are not compared. */
	public JsonParseEquivalence ignoringValuesOf(String... fieldNames) {
		Set<String> ignored = new LinkedHashSet<>(presenceOnlyFields);
		ignored.addAll(Arrays.asList(fieldNames));
		return new JsonParseEquivalence(dateFields, ignored);
	}

	public void assertEquivalent(String expectedJson, String actualJson) {
		assertEquivalent(parse(expectedJson), parse(actualJson));
	}

	public void assertEquivalent(JsonNode expected, JsonNode actual) {
		List<String> differences = differences(expected, actual);
		if (!differences.isEmpty()) {
			fail("JSON documents are not parse-equivalent:\n  " + String.join("\n  ", differences));
		}
	}

	/** Every difference between the two documents, as {@code $.path: description}; empty when equivalent. */
	public List<String> differences(JsonNode expected, JsonNode actual) {
		List<String> differences = new ArrayList<>();
		compare("$", null, expected, actual, differences);
		return differences;
	}

	/** Parses JSON keeping decimals exact, so {@code 1000.00} round-trips unchanged. */
	public static JsonNode parse(String json) {
		try {
			return MAPPER.readTree(json);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Not valid JSON: " + json, e);
		}
	}

	/** Pretty-prints a node with the same exact-decimal settings used by {@link #parse}. */
	public static String write(JsonNode node) {
		try {
			return MAPPER.writeValueAsString(node) + "\n";
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Reads a serialised {@code java.util.Date}: a number is epoch milliseconds, a string must be
	 * ISO-8601 with an offset ({@code Z}, {@code +00}, {@code +0000} or {@code +00:00}).
	 */
	public static Instant parseInstant(JsonNode node) {
		if (node.isIntegralNumber()) {
			return Instant.ofEpochMilli(node.longValue());
		}
		if (node.isTextual()) {
			return OffsetDateTime.parse(node.textValue(), ISO_WITH_OFFSET).toInstant();
		}
		throw new IllegalArgumentException("Not a date value: " + node);
	}

	private void compare(String path, String fieldName, JsonNode expected, JsonNode actual, List<String> out) {
		if (fieldName != null && presenceOnlyFields.contains(fieldName)) {
			if (expected.getNodeType() != actual.getNodeType()) {
				out.add(path + ": expected a " + expected.getNodeType() + " but was " + actual);
			}
			return;
		}
		if (fieldName != null && dateFields.contains(fieldName) && !expected.isNull() && !actual.isNull()) {
			compareInstants(path, expected, actual, out);
			return;
		}
		if (expected.isObject() && actual.isObject()) {
			compareObjects(path, expected, actual, out);
		}
		else if (expected.isArray() && actual.isArray()) {
			if (expected.size() != actual.size()) {
				out.add(path + ": expected " + expected.size() + " elements but was " + actual.size());
				return;
			}
			for (int i = 0; i < expected.size(); i++) {
				compare(path + "[" + i + "]", null, expected.get(i), actual.get(i), out);
			}
		}
		else if (expected.isNumber() && actual.isNumber()) {
			if (expected.decimalValue().compareTo(actual.decimalValue()) != 0) {
				out.add(path + ": expected " + expected + " but was " + actual);
			}
		}
		else if (!expected.equals(actual)) {
			out.add(path + ": expected " + expected + " but was " + actual);
		}
	}

	private void compareObjects(String path, JsonNode expected, JsonNode actual, List<String> out) {
		Set<String> expectedNames = fieldNames(expected);
		Set<String> actualNames = fieldNames(actual);
		for (String name : expectedNames) {
			if (!actualNames.contains(name)) {
				out.add(path + "." + name + ": missing");
			}
			else {
				compare(path + "." + name, name, expected.get(name), actual.get(name), out);
			}
		}
		for (String name : actualNames) {
			if (!expectedNames.contains(name)) {
				out.add(path + "." + name + ": unexpected field with value " + actual.get(name));
			}
		}
	}

	private static void compareInstants(String path, JsonNode expected, JsonNode actual, List<String> out) {
		Instant expectedInstant;
		Instant actualInstant;
		try {
			expectedInstant = parseInstant(expected);
			actualInstant = parseInstant(actual);
		}
		catch (IllegalArgumentException | DateTimeParseException e) {
			out.add(path + ": expected date " + expected + " but was " + actual + " (" + e.getMessage() + ")");
			return;
		}
		if (!expectedInstant.equals(actualInstant)) {
			out.add(path + ": expected instant " + expectedInstant + " (" + expected + ") but was " + actualInstant
					+ " (" + actual + ")");
		}
	}

	private static Set<String> fieldNames(JsonNode node) {
		Set<String> names = new TreeSet<>();
		for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
			names.add(it.next());
		}
		return names;
	}
}
