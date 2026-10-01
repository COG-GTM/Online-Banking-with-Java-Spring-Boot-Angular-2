package com.userFront.domain;

import java.util.Date;

public class AppointmentSummary {

    private final Long id;
    private final Date date;
    private final String location;
    private final String description;
    private final boolean confirmed;
    private final UserRef user;

    public AppointmentSummary(Long id, Date date, String location, String description, boolean confirmed, String username) {
        this.id = id;
        this.date = date;
        this.location = location;
        this.description = description;
        this.confirmed = confirmed;
        this.user = username == null ? null : new UserRef(username);
    }

    public Long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public UserRef getUser() {
        return user;
    }

    public static class UserRef {

        private final String username;

        public UserRef(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}
