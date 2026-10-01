package com.userFront.service.UserServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.userFront.domain.Appointment;
import com.userFront.service.AppointmentService;
import com.userFront.dao.AppointmentDao;
import com.userFront.dto.AppointmentDto;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentDao appointmentDao;

    public Appointment createAppointment(Appointment appointment) {
       return appointmentDao.save(appointment);
    }

    public List<Appointment> findAll() {
        return appointmentDao.findAll();
    }

    public List<AppointmentDto> findAllAppointmentDtos() {
        return appointmentDao.findAllAppointmentDtos();
    }

    public List<AppointmentDto> findAllAppointmentDtos(int page, int size) {
        return appointmentDao.findAllAppointmentDtos(new PageRequest(page, size, new Sort("id")));
    }

    public Appointment findAppointment(Long id) {
        return appointmentDao.findOne(id);
    }

    public void confirmAppointment(Long id) {
        Appointment appointment = findAppointment(id);
        appointment.setConfirmed(true);
        appointmentDao.save(appointment);
    }
}
