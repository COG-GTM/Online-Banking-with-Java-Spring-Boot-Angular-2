package com.userFront.dto;

import java.util.Date;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;

public class AppointmentDto {

	private final Long id;
	private final Date date;
	private final String location;
	private final String description;
	private final boolean confirmed;
	private final UserSummary user;

	public AppointmentDto(Appointment appointment) {
		this.id = appointment.getId();
		this.date = appointment.getDate();
		this.location = appointment.getLocation();
		this.description = appointment.getDescription();
		this.confirmed = appointment.isConfirmed();
		this.user = appointment.getUser() == null ? null : new UserSummary(appointment.getUser());
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

	public UserSummary getUser() {
		return user;
	}

	public static class UserSummary {

		private final Long userId;
		private final String username;

		public UserSummary(User user) {
			this.userId = user.getUserId();
			this.username = user.getUsername();
		}

		public Long getUserId() {
			return userId;
		}

		public String getUsername() {
			return username;
		}
	}
}
