package com.userFront.dto;

import java.util.Date;

public class AppointmentDto {

	private Long id;
	private Date date;
	private String location;
	private String description;
	private boolean confirmed;
	private AppointmentUserDto user;

	public AppointmentDto(Long id, Date date, String location, String description, boolean confirmed,
			Long userId, String username, String firstName, String lastName) {
		this.id = id;
		this.date = date;
		this.location = location;
		this.description = description;
		this.confirmed = confirmed;
		this.user = userId == null ? null : new AppointmentUserDto(userId, username, firstName, lastName);
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

	public AppointmentUserDto getUser() {
		return user;
	}

	public static class AppointmentUserDto {

		private Long userId;
		private String username;
		private String firstName;
		private String lastName;

		public AppointmentUserDto(Long userId, String username, String firstName, String lastName) {
			this.userId = userId;
			this.username = username;
			this.firstName = firstName;
			this.lastName = lastName;
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
	}
}
