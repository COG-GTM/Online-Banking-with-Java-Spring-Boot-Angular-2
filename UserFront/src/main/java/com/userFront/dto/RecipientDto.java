package com.userFront.dto;

import com.userFront.domain.Recipient;

public class RecipientDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String accountNumber;
    private String description;

    public RecipientDto() {
    }

    public static RecipientDto from(Recipient r) {
        if (r == null) {
            return null;
        }
        RecipientDto dto = new RecipientDto();
        dto.id = r.getId();
        dto.name = r.getName();
        dto.email = r.getEmail();
        dto.phone = r.getPhone();
        dto.accountNumber = r.getAccountNumber();
        dto.description = r.getDescription();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
