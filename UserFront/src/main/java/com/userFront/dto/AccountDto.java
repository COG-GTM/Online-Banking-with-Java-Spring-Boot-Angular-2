package com.userFront.dto;

import java.math.BigDecimal;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

/**
 * JSON view of an account (Primary or Savings) for the React customer portal.
 */
public class AccountDto {

    private Long id;
    private int accountNumber;
    private BigDecimal accountBalance;

    public AccountDto() {
    }

    public AccountDto(Long id, int accountNumber, BigDecimal accountBalance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountBalance = accountBalance;
    }

    public static AccountDto from(PrimaryAccount account) {
        if (account == null) {
            return null;
        }
        return new AccountDto(account.getId(), account.getAccountNumber(), account.getAccountBalance());
    }

    public static AccountDto from(SavingsAccount account) {
        if (account == null) {
            return null;
        }
        return new AccountDto(account.getId(), account.getAccountNumber(), account.getAccountBalance());
    }

    public Long getId() {
        return id;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }
}
