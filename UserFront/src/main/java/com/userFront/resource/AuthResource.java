package com.userFront.resource;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.dao.RoleDao;
import com.userFront.domain.User;
import com.userFront.domain.security.UserRole;
import com.userFront.dto.ApiErrorResponse;
import com.userFront.dto.LoginRequest;
import com.userFront.dto.SignupRequest;
import com.userFront.dto.UserDto;
import com.userFront.service.UserService;

/**
 * JSON authentication endpoints for the React SPA. Mirrors the legacy
 * {@code HomeController} signup logic and exposes session-based login/logout.
 */
@RestController
public class AuthResource {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @RequestMapping(value = "/api/auth/signup", method = RequestMethod.POST)
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userService.checkUserExists(request.getUsername(), request.getEmail())) {
            ApiErrorResponse error = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(),
                    "User already exists");
            error.addDetail("usernameExists", userService.checkUsernameExists(request.getUsername()));
            error.addDetail("emailExists", userService.checkEmailExists(request.getEmail()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user, roleDao.findByName("ROLE_USER")));

        User createdUser = userService.createUser(user, userRoles);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(createdUser));
    }

    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(auth);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            return ResponseEntity.ok(UserDto.from(userService.findByUsername(request.getUsername())));
        } catch (AuthenticationException ex) {
            ApiErrorResponse error = new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @RequestMapping(value = "/api/auth/logout", method = RequestMethod.POST)
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/user/me", method = RequestMethod.GET)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> currentUser(Principal principal) {
        return ResponseEntity.ok(UserDto.from(userService.findByUsername(principal.getName())));
    }
}
