package com.userFront.resource.dto;

import java.util.Date;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;

public class AppointmentSummary {

    private final Long id;
    private final Date date;
    private final String location;
    private final String description;
    private final boolean confirmed;
    private final UserRef user;

    private AppointmentSummary(Appointment appointment) {
        this.id = appointment.getId();
        this.date = appointment.getDate();
        this.location = appointment.getLocation();
        this.description = appointment.getDescription();
        this.confirmed = appointment.isConfirmed();
        this.user = UserRef.from(appointment.getUser());
    }

    public static AppointmentSummary from(Appointment appointment) {
        return appointment == null ? null : new AppointmentSummary(appointment);
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

        private UserRef(String username) {
            this.username = username;
        }

        static UserRef from(User user) {
            return user == null ? null : new UserRef(user.getUsername());
        }

        public String getUsername() {
            return username;
        }
    }
}
