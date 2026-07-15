package com.userFront.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.userFront.domain.User;
import com.userFront.domain.security.UserRole;

/**
 * JSON view of a {@link User} for the React front end.
 *
 * <p>Deliberately omits the {@code password} field that the legacy
 * {@code /api/user/all} endpoint leaks, and flattens the user's roles into a
 * simple list of role names so the React auth guards can branch on them.
 */
public class UserDto {

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean enabled;
    private List<String> roles;
    private AccountDto primaryAccount;
    private AccountDto savingsAccount;

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
        dto.roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(role -> role.getName())
                .collect(Collectors.toList());
        dto.primaryAccount = AccountDto.from(user.getPrimaryAccount());
        dto.savingsAccount = AccountDto.from(user.getSavingsAccount());
        return dto;
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

    public List<String> getRoles() {
        return roles;
    }

    public AccountDto getPrimaryAccount() {
        return primaryAccount;
    }

    public AccountDto getSavingsAccount() {
        return savingsAccount;
    }
}
