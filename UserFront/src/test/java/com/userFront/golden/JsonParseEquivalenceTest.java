package com.userFront.golden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class JsonParseEquivalenceTest {

	private static final String EPOCH = "{\"date\":1497521730000}";

	private final JsonParseEquivalence equivalence = JsonParseEquivalence.create();

	@Test
	public void epochMillisAndIsoOffsetsDenotingTheSameInstantAreEquivalent() {
		equivalence.assertEquivalent(EPOCH, "{\"date\":\"2017-06-15T10:15:30.000+0000\"}");
		equivalence.assertEquivalent(EPOCH, "{\"date\":\"2017-06-15T10:15:30.000+00:00\"}");
		equivalence.assertEquivalent(EPOCH, "{\"date\":\"2017-06-15T10:15:30Z\"}");
		equivalence.assertEquivalent(EPOCH, "{\"date\":\"2017-06-15T12:15:30+02:00\"}");
		equivalence.assertEquivalent("{\"date\":\"2017-06-15T10:15:30.000+0000\"}",
				"{\"date\":\"2017-06-15T10:15:30.000+00:00\"}");
	}

	@Test
	public void differentInstantsAreReported() {
		assertEquals(1, diff(EPOCH, "{\"date\":\"2017-06-15T10:15:31Z\"}").size());
		assertEquals(1, diff(EPOCH, "{\"date\":1497521730001}").size());
	}

	@Test
	public void unparseableOrOffsetlessDatesAreReported() {
		assertEquals(1, diff(EPOCH, "{\"date\":\"2017-06-15T10:15:30\"}").size());
		assertEquals(1, diff(EPOCH, "{\"date\":\"not a date\"}").size());
	}

	@Test
	public void datesAreOnlyLenientInDateFields() {
		assertEquals(1, diff("{\"created\":1497521730000}", "{\"created\":\"2017-06-15T10:15:30Z\"}").size());
		JsonParseEquivalence.create().withDateFields("created")
				.assertEquivalent("{\"created\":1497521730000}", "{\"created\":\"2017-06-15T10:15:30Z\"}");
	}

	@Test
	public void numbersCompareByValue() {
		equivalence.assertEquivalent("{\"balance\":1000.00}", "{\"balance\":1000}");
		assertEquals(1, diff("{\"balance\":1000.00}", "{\"balance\":1000.01}").size());
		assertEquals(1, diff("{\"balance\":1000}", "{\"balance\":\"1000\"}").size());
	}

	@Test
	public void missingAndUnexpectedFieldsAreReported() {
		List<String> differences = diff("{\"a\":1,\"recipientList\":[]}", "{\"a\":1,\"b\":2}");
		assertEquals(2, differences.size());
		assertTrue(differences.get(0), differences.get(0).startsWith("$.recipientList: missing"));
		assertTrue(differences.get(1), differences.get(1).startsWith("$.b: unexpected"));
	}

	@Test
	public void arraysCompareByLengthAndOrder() {
		assertEquals(1, diff("[1,2]", "[1,2,3]").size());
		assertEquals(2, diff("[1,2]", "[2,1]").size());
	}

	@Test
	public void presenceOnlyFieldsIgnoreValuesButNotTypeOrPresence() {
		JsonParseEquivalence ids = JsonParseEquivalence.create().ignoringValuesOf("id");
		ids.assertEquivalent("{\"id\":1,\"nested\":{\"id\":7}}", "{\"id\":42,\"nested\":{\"id\":99}}");
		assertEquals(1, ids.differences(JsonParseEquivalence.parse("{\"id\":1}"),
				JsonParseEquivalence.parse("{\"id\":\"1\"}")).size());
		assertEquals(1, ids.differences(JsonParseEquivalence.parse("{\"id\":1}"),
				JsonParseEquivalence.parse("{}")).size());
	}

	private List<String> diff(String expected, String actual) {
		return equivalence.differences(JsonParseEquivalence.parse(expected), JsonParseEquivalence.parse(actual));
	}
}
