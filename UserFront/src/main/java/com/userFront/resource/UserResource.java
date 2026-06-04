package com.userFront.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.domain.PrimaryTransaction;
import com.userFront.domain.SavingsTransaction;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class UserResource {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(value = "/user/all", method = RequestMethod.GET)
    public List<User> userList() {
        return userService.findUserList();
    }

    @RequestMapping(value = "/user/primary/transaction", method = RequestMethod.GET)
    public List<PrimaryTransaction> getPrimaryTransactionList(@RequestParam("username") String username) {
        validateUsername(username);
        return transactionService.findPrimaryTransactionList(username);
    }

    @RequestMapping(value = "/user/savings/transaction", method = RequestMethod.GET)
    public List<SavingsTransaction> getSavingsTransactionList(@RequestParam("username") String username) {
        validateUsername(username);
        return transactionService.findSavingsTransactionList(username);
    }

    @RequestMapping("/user/{username}/enable")
    public void enableUser(@PathVariable("username") String username) {
        validateUsername(username);
        userService.enableUser(username);
    }

    @RequestMapping("/user/{username}/disable")
    public void diableUser(@PathVariable("username") String username) {
        validateUsername(username);
        userService.disableUser(username);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUserRequestException("A non-empty username is required");
        }
        if (userService.findByUsername(username) == null) {
            throw new InvalidUserRequestException("Unknown username: " + username);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class InvalidUserRequestException extends RuntimeException {
        InvalidUserRequestException(String message) {
            super(message);
        }
    }
}
