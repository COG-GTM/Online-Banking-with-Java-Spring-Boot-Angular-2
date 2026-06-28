import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import PrimaryTransaction from "./PrimaryTransaction";
import type { Transaction } from "../types/transaction";

const sample: Transaction[] = [
  {
    id: 1,
    date: "2023-01-05T00:00:00",
    description: "Initial Deposit",
    type: "Credit",
    status: "Finished",
    amount: 1000,
    availableBalance: 1000,
  },
];

function renderAt(username: string) {
  return render(
    <MemoryRouter initialEntries={[`/primaryTransaction/${username}`]}>
      <Routes>
        <Route
          path="/primaryTransaction/:username"
          element={<PrimaryTransaction />}
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe("PrimaryTransaction page", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders the fetched transactions in a table", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(new Response(JSON.stringify(sample), { status: 200 }));

    renderAt("alice");

    expect(
      screen.getByRole("heading", { name: /Primary Account Transaction List/i }),
    ).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.getByText("Initial Deposit")).toBeInTheDocument(),
    );

    expect(screen.getByText("01/05/2023")).toBeInTheDocument();
    expect(fetchMock.mock.calls[0][0]).toContain("username=alice");
  });

  it("shows an empty state when there are no transactions", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("[]", { status: 200 }),
    );

    renderAt("bob");

    await waitFor(() =>
      expect(screen.getByText(/No transactions found/i)).toBeInTheDocument(),
    );
  });

  it("shows an error state when the request fails", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("err", { status: 500 }),
    );

    renderAt("carol");

    await waitFor(() =>
      expect(screen.getByRole("alert")).toHaveTextContent(/HTTP 500/),
    );
  });
});
