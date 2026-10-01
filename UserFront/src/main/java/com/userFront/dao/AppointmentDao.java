package com.userFront.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.Appointment;
import com.userFront.dto.AppointmentDto;

public interface AppointmentDao extends CrudRepository<Appointment, Long> {

    String APPOINTMENT_DTO_QUERY = "select new com.userFront.dto.AppointmentDto("
            + "a.id, a.date, a.location, a.description, a.confirmed, "
            + "u.userId, u.username, u.firstName, u.lastName) "
            + "from Appointment a left join a.user u";

    List<Appointment> findAll();

    @Query(APPOINTMENT_DTO_QUERY)
    List<AppointmentDto> findAllAppointmentDtos();

    @Query(APPOINTMENT_DTO_QUERY)
    List<AppointmentDto> findAllAppointmentDtos(Pageable pageable);
}
