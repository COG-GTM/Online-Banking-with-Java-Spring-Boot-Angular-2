package com.userFront.resource;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.domain.Appointment;
import com.userFront.domain.User;
import com.userFront.dto.ApiErrorResponse;
import com.userFront.dto.AppointmentDto;
import com.userFront.dto.AppointmentRequest;
import com.userFront.service.AppointmentService;
import com.userFront.service.UserService;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasRole('USER')")
public class UserAppointmentResource {

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("")
    public List<AppointmentDto> getAppointments(Principal principal) {
        return appointmentService.findAll().stream()
                .filter(a -> a.getUser() != null
                        && a.getUser().getUsername().equals(principal.getName()))
                .map(AppointmentDto::from)
                .collect(Collectors.toList());
    }

    @PostMapping("")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request, Principal principal) {
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            date = format.parse(request.getDate());
        } catch (ParseException | NullPointerException e) {
            ApiErrorResponse error = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(),
                    "Invalid appointment date format. Expected 'yyyy-MM-dd hh:mm'.")
                    .addDetail("date", request.getDate());
            return ResponseEntity.badRequest().body(error);
        }

        Appointment appointment = new Appointment();
        appointment.setDate(date);
        appointment.setDescription(request.getDescription());
        appointment.setLocation(request.getLocation());

        User user = userService.findByUsername(principal.getName());
        appointment.setUser(user);

        Appointment created = appointmentService.createAppointment(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentDto.from(created));
    }
}
