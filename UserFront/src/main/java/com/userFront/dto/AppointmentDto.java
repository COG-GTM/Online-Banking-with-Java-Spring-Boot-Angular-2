package com.userFront.dto;

import java.util.Date;

import com.userFront.domain.Appointment;

public class AppointmentDto {

    private Long id;
    private Date date;
    private String location;
    private String description;
    private boolean confirmed;
    private String username;

    public AppointmentDto() {
    }

    public static AppointmentDto from(Appointment a) {
        if (a == null) {
            return null;
        }
        AppointmentDto dto = new AppointmentDto();
        dto.id = a.getId();
        dto.date = a.getDate();
        dto.location = a.getLocation();
        dto.description = a.getDescription();
        dto.confirmed = a.isConfirmed();
        if (a.getUser() != null) {
            dto.username = a.getUser().getUsername();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
