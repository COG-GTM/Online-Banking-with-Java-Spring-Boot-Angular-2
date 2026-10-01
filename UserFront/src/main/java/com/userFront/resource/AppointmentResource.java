package com.userFront.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.dto.AppointmentDto;
import com.userFront.service.AppointmentService;

@RestController
@RequestMapping("/api/appointment")
@PreAuthorize("hasRole('ADMIN')")
public class AppointmentResource {

    @Autowired
    private AppointmentService appointmentService;

    @RequestMapping("/all")
    public List<AppointmentDto> findAppointmentList(@RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        if (page == null || size == null) {
            return appointmentService.findAllAppointmentDtos();
        }
        return appointmentService.findAllAppointmentDtos(page, size);
    }

    @RequestMapping("/{id}/confirm")
    public void confirmAppointment(@PathVariable("id") Long id) {
        appointmentService.confirmAppointment(id);
    }
}
