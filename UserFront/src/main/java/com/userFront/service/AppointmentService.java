package com.userFront.service;

import java.util.List;

import com.userFront.domain.Appointment;
import com.userFront.dto.AppointmentDto;

public interface AppointmentService {
	Appointment createAppointment(Appointment appointment);

    List<Appointment> findAll();

    List<AppointmentDto> findAllAppointmentDtos();

    List<AppointmentDto> findAllAppointmentDtos(int page, int size);

    Appointment findAppointment(Long id);

    void confirmAppointment(Long id);
}
