package com.userFront.resource;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.domain.User;
import com.userFront.dto.ProfileDto;
import com.userFront.dto.ProfileUpdateRequest;
import com.userFront.service.UserService;

@RestController
@PreAuthorize("hasRole('USER')")
public class ProfileResource {

    @Autowired
    private UserService userService;

    @GetMapping("/api/user/profile")
    public ProfileDto getProfile(Principal principal) {
        return ProfileDto.from(userService.findByUsername(principal.getName()));
    }

    @PutMapping("/api/user/profile")
    public ProfileDto updateProfile(@RequestBody ProfileUpdateRequest request, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return ProfileDto.from(userService.saveUser(user));
    }
}
