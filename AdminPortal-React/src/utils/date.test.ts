import { describe, it, expect } from "vitest";
import { formatDate } from "./date";

describe("formatDate", () => {
  it("formats an ISO date string as MM/dd/yyyy", () => {
    expect(formatDate("2023-01-05T00:00:00")).toBe("01/05/2023");
  });

  it("zero-pads month and day", () => {
    expect(formatDate("2023-12-09T12:00:00")).toBe("12/09/2023");
  });

  it("returns empty string for nullish or invalid input", () => {
    expect(formatDate(null)).toBe("");
    expect(formatDate(undefined)).toBe("");
    expect(formatDate("")).toBe("");
    expect(formatDate("not-a-date")).toBe("");
  });
});
