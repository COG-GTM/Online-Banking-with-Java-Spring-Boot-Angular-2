package com.userFront.golden;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Golden JSON fixtures under {@code src/test/resources/golden/<set>/<name>.json}.
 *
 * <p>{@link #assertMatches} compares a response against the committed fixture with a
 * {@link JsonParseEquivalence}. Run the tests with {@code -D}{@value #UPDATE_PROPERTY}{@code =true}
 * to (re)write the fixtures from the current responses instead, then commit them.
 */
public final class GoldenFixtures {

	/** Responses captured on Spring Boot 1.5.4 (Jackson 2.8, dates as epoch milliseconds). */
	public static final String BOOT15 = "boot15";

	public static final String UPDATE_PROPERTY = "golden.update";

	private static final Path SOURCE_ROOT = Paths.get("src", "test", "resources", "golden");

	private GoldenFixtures() {
	}

	public static void assertMatches(String set, String name, JsonNode actual, JsonParseEquivalence equivalence) {
		if (Boolean.getBoolean(UPDATE_PROPERTY)) {
			write(set, name, actual);
			return;
		}
		equivalence.assertEquivalent(read(set, name), actual);
	}

	public static JsonNode read(String set, String name) {
		String resource = "/golden/" + set + "/" + name + ".json";
		try (InputStream in = GoldenFixtures.class.getResourceAsStream(resource)) {
			assertNotNull("Missing golden fixture " + resource + "; generate it with -D" + UPDATE_PROPERTY + "=true",
					in);
			try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
				return JsonParseEquivalence.parse(scanner.hasNext() ? scanner.next() : "");
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void write(String set, String name, JsonNode actual) {
		Path file = SOURCE_ROOT.resolve(set).resolve(name + ".json");
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, JsonParseEquivalence.write(actual).getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
