package com.userFront.resource;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    public List<PrimaryTransaction> getPrimaryTransactionList(@RequestParam("username") String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "" + TransactionService.DEFAULT_PAGE_SIZE) int size,
            HttpServletResponse response) {
        return withPageHeaders(transactionService.findPrimaryTransactionPage(username, page, size), response);
    }

    @RequestMapping(value = "/user/savings/transaction", method = RequestMethod.GET)
    public List<SavingsTransaction> getSavingsTransactionList(@RequestParam("username") String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "" + TransactionService.DEFAULT_PAGE_SIZE) int size,
            HttpServletResponse response) {
        return withPageHeaders(transactionService.findSavingsTransactionPage(username, page, size), response);
    }

    private static <T> List<T> withPageHeaders(Page<T> page, HttpServletResponse response) {
        response.setHeader("X-Total-Count", String.valueOf(page.getTotalElements()));
        response.setHeader("X-Total-Pages", String.valueOf(page.getTotalPages()));
        response.setHeader("X-Page", String.valueOf(page.getNumber()));
        response.setHeader("X-Page-Size", String.valueOf(page.getSize()));
        return page.getContent();
    }

    @RequestMapping("/user/{username}/enable")
    public void enableUser(@PathVariable("username") String username) {
        userService.enableUser(username);
    }

    @RequestMapping("/user/{username}/disable")
    public void diableUser(@PathVariable("username") String username) {
        userService.disableUser(username);
    }
}
