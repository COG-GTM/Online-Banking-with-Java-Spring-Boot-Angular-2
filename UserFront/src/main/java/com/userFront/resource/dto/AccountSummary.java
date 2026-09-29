package com.userFront.resource.dto;

import java.math.BigDecimal;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

public class AccountSummary {

    private final BigDecimal accountBalance;

    private AccountSummary(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public static AccountSummary from(PrimaryAccount account) {
        return account == null ? null : new AccountSummary(account.getAccountBalance());
    }

    public static AccountSummary from(SavingsAccount account) {
        return account == null ? null : new AccountSummary(account.getAccountBalance());
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }
}
