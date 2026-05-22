package com.userFront.appointment.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userFront.appointment.domain.Appointment;

public interface AppointmentDao extends CrudRepository<Appointment, Long> {

    List<Appointment> findAll();
}
