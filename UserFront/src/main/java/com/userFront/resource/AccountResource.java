package com.userFront.resource;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.domain.User;
import com.userFront.dto.AccountDto;
import com.userFront.dto.AccountStatementDto;
import com.userFront.dto.TransactionDto;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

/**
 * JSON account API for the authenticated customer ({@code ROLE_USER}).
 *
 * <p>Mirrors the read side of the legacy {@code AccountController} Thymeleaf
 * pages ({@code /account/primaryAccount}, {@code /account/savingsAccount}) but
 * returns typed JSON scoped to the current principal.
 */
@RestController
@RequestMapping("/api/account")
@PreAuthorize("hasRole('USER')")
public class AccountResource {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/primary")
    public ResponseEntity<AccountStatementDto> primary(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        AccountDto account = AccountDto.from(user.getPrimaryAccount());
        return ResponseEntity.ok(new AccountStatementDto(account,
                TransactionDto.fromPrimary(transactionService.findPrimaryTransactionList(principal.getName()))));
    }

    @GetMapping("/savings")
    public ResponseEntity<AccountStatementDto> savings(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        AccountDto account = AccountDto.from(user.getSavingsAccount());
        return ResponseEntity.ok(new AccountStatementDto(account,
                TransactionDto.fromSavings(transactionService.findSavingsTransactionList(principal.getName()))));
    }
}
