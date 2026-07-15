package com.userFront.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsTransaction;

/**
 * JSON view of a ledger entry (Primary or Savings) for the React customer portal.
 *
 * <p>Excludes the back-reference to the owning account to avoid nested account
 * serialization; {@code date} is emitted as epoch milliseconds.
 */
public class TransactionDto {

    private Long id;
    private Long date;
    private String description;
    private String type;
    private String status;
    private double amount;
    private BigDecimal availableBalance;

    public TransactionDto() {
    }

    public TransactionDto(Long id, Long date, String description, String type, String status, double amount,
            BigDecimal availableBalance) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.availableBalance = availableBalance;
    }

    public static TransactionDto from(PrimaryTransaction t) {
        return new TransactionDto(t.getId(), t.getDate() == null ? null : t.getDate().getTime(), t.getDescription(),
                t.getType(), t.getStatus(), t.getAmount(), t.getAvailableBalance());
    }

    public static TransactionDto from(SavingsTransaction t) {
        return new TransactionDto(t.getId(), t.getDate() == null ? null : t.getDate().getTime(), t.getDescription(),
                t.getType(), t.getStatus(), t.getAmount(), t.getAvailableBalance());
    }

    public static List<TransactionDto> fromPrimary(List<PrimaryTransaction> list) {
        return list.stream().map(TransactionDto::from).collect(Collectors.toList());
    }

    public static List<TransactionDto> fromSavings(List<SavingsTransaction> list) {
        return list.stream().map(TransactionDto::from).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public Long getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public double getAmount() {
        return amount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}
