package com.userFront.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.Appointment;
import com.userFront.domain.AppointmentSummary;

public interface AppointmentDao extends CrudRepository<Appointment, Long> {

    @Query(value = "select new com.userFront.domain.AppointmentSummary(a.id, a.date, a.location, a.description, a.confirmed, u.username) "
            + "from Appointment a left join a.user u",
            countQuery = "select count(a) from Appointment a")
    Page<AppointmentSummary> findAllSummaries(Pageable pageable);
}
