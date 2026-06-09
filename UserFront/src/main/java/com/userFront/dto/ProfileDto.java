package com.userFront.dto;

import com.userFront.domain.User;

public class ProfileDto {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public ProfileDto() {
    }

    public static ProfileDto from(User user) {
        if (user == null) {
            return null;
        }
        ProfileDto dto = new ProfileDto();
        dto.username = user.getUsername();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        return dto;
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
}
