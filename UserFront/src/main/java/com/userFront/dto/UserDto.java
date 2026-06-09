package com.userFront.dto;

import java.util.ArrayList;
import java.util.List;

import com.userFront.domain.User;
import com.userFront.domain.security.UserRole;

/**
 * JSON-safe representation of a {@link User}. Never exposes the password.
 */
public class UserDto {

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean enabled;
    private List<String> roles = new ArrayList<>();
    private AccountDto primaryAccount;
    private AccountDto savingsAccount;

    public UserDto() {
    }

    public static UserDto from(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.userId = user.getUserId();
        dto.username = user.getUsername();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        dto.enabled = user.isEnabled();
        if (user.getUserRoles() != null) {
            for (UserRole ur : user.getUserRoles()) {
                if (ur.getRole() != null) {
                    dto.roles.add(ur.getRole().getName());
                }
            }
        }
        dto.primaryAccount = AccountDto.from(user.getPrimaryAccount());
        dto.savingsAccount = AccountDto.from(user.getSavingsAccount());
        return dto;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public AccountDto getPrimaryAccount() {
        return primaryAccount;
    }

    public void setPrimaryAccount(AccountDto primaryAccount) {
        this.primaryAccount = primaryAccount;
    }

    public AccountDto getSavingsAccount() {
        return savingsAccount;
    }

    public void setSavingsAccount(AccountDto savingsAccount) {
        this.savingsAccount = savingsAccount;
    }
}
