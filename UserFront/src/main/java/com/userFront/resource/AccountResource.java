package com.userFront.resource;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userFront.domain.User;
import com.userFront.dto.AccountDto;
import com.userFront.dto.DepositRequest;
import com.userFront.dto.TransactionDto;
import com.userFront.dto.WithdrawRequest;
import com.userFront.service.AccountService;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

@RestController
@RequestMapping("/api/account")
@PreAuthorize("hasRole('USER')")
public class AccountResource {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/primary")
    public Map<String, Object> primary(Principal principal) {
        User user = userService.findByUsername(principal.getName());

        List<TransactionDto> transactions = transactionService
                .findPrimaryTransactionList(principal.getName()).stream()
                .map(TransactionDto::from)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("account", AccountDto.from(user.getPrimaryAccount()));
        response.put("transactions", transactions);
        return response;
    }

    @GetMapping("/savings")
    public Map<String, Object> savings(Principal principal) {
        User user = userService.findByUsername(principal.getName());

        List<TransactionDto> transactions = transactionService
                .findSavingsTransactionList(principal.getName()).stream()
                .map(TransactionDto::from)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("account", AccountDto.from(user.getSavingsAccount()));
        response.put("transactions", transactions);
        return response;
    }

    @PostMapping("/deposit")
    public AccountDto deposit(@RequestBody DepositRequest request, Principal principal) {
        accountService.deposit(request.getAccountType(), request.getAmount(), principal);
        return accountForType(principal, request.getAccountType());
    }

    @PostMapping("/withdraw")
    public AccountDto withdraw(@RequestBody WithdrawRequest request, Principal principal) {
        accountService.withdraw(request.getAccountType(), request.getAmount(), principal);
        return accountForType(principal, request.getAccountType());
    }

    private AccountDto accountForType(Principal principal, String accountType) {
        User user = userService.findByUsername(principal.getName());
        if ("Savings".equals(accountType)) {
            return AccountDto.from(user.getSavingsAccount());
        }
        return AccountDto.from(user.getPrimaryAccount());
    }
}
