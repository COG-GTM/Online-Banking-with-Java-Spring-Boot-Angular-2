import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { getPrimaryTransactionList } from "./userService";
import type { Transaction } from "../types/transaction";

const sample: Transaction[] = [
  {
    id: 1,
    date: "2023-01-05T00:00:00",
    description: "Deposit",
    type: "Credit",
    status: "Finished",
    amount: 100,
    availableBalance: 1100,
  },
];

describe("getPrimaryTransactionList", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("calls the primary transaction endpoint with credentials and the username", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(new Response(JSON.stringify(sample), { status: 200 }));

    const result = await getPrimaryTransactionList("alice");

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe(
      "http://localhost:8080/api/user/primary/transaction?username=alice",
    );
    expect(init).toMatchObject({ method: "GET", credentials: "include" });
    expect(result).toEqual(sample);
  });

  it("url-encodes the username", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(new Response("[]", { status: 200 }));

    await getPrimaryTransactionList("a b&c");

    expect(fetchMock.mock.calls[0][0]).toBe(
      "http://localhost:8080/api/user/primary/transaction?username=a%20b%26c",
    );
  });

  it("throws on a non-ok response", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("nope", { status: 500 }),
    );

    await expect(getPrimaryTransactionList("alice")).rejects.toThrow(
      /HTTP 500/,
    );
  });
});
