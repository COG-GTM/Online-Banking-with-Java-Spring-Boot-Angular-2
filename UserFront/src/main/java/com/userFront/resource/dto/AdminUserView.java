package com.userFront.resource.dto;

import java.math.BigDecimal;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;
import com.userFront.domain.User;

public class AdminUserView {

    private final Long userId;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final boolean enabled;
    private final AccountBalanceView primaryAccount;
    private final AccountBalanceView savingsAccount;

    public AdminUserView(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.enabled = user.isEnabled();
        PrimaryAccount primary = user.getPrimaryAccount();
        SavingsAccount savings = user.getSavingsAccount();
        this.primaryAccount = primary == null ? null : new AccountBalanceView(primary.getAccountBalance());
        this.savingsAccount = savings == null ? null : new AccountBalanceView(savings.getAccountBalance());
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AccountBalanceView getPrimaryAccount() {
        return primaryAccount;
    }

    public AccountBalanceView getSavingsAccount() {
        return savingsAccount;
    }

    public static class AccountBalanceView {

        private final BigDecimal accountBalance;

        public AccountBalanceView(BigDecimal accountBalance) {
            this.accountBalance = accountBalance;
        }

        public BigDecimal getAccountBalance() {
            return accountBalance;
        }
    }
}
