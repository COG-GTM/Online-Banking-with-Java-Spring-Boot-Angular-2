package com.userFront.resource.dto;

import java.util.Date;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;

public class AdminAppointmentView {

    private final Long id;
    private final Date date;
    private final String location;
    private final String description;
    private final boolean confirmed;
    private final AppointmentUserView user;

    public AdminAppointmentView(Appointment appointment) {
        this.id = appointment.getId();
        this.date = appointment.getDate();
        this.location = appointment.getLocation();
        this.description = appointment.getDescription();
        this.confirmed = appointment.isConfirmed();
        User owner = appointment.getUser();
        this.user = owner == null ? null : new AppointmentUserView(owner.getUsername());
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

    public AppointmentUserView getUser() {
        return user;
    }

    public static class AppointmentUserView {

        private final String username;

        public AppointmentUserView(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}
