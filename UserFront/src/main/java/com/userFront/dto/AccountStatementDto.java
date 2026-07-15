package com.userFront.dto;

import java.util.List;

/**
 * Balance + transaction list for a single account, returned by the customer
 * account endpoints ({@code /api/account/primary}, {@code /api/account/savings}).
 */
public class AccountStatementDto {

    private AccountDto account;
    private List<TransactionDto> transactions;

    public AccountStatementDto() {
    }

    public AccountStatementDto(AccountDto account, List<TransactionDto> transactions) {
        this.account = account;
        this.transactions = transactions;
    }

    public AccountDto getAccount() {
        return account;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }
}
