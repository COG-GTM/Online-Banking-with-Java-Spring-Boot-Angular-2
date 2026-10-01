package com.userFront.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.userFront.domain.Appointment;
import com.userFront.domain.AppointmentSummary;

public interface AppointmentService {
	Appointment createAppointment(Appointment appointment);

    Page<AppointmentSummary> findAllSummaries(Pageable pageable);

    Appointment findAppointment(Long id);

    void confirmAppointment(Long id);
}
