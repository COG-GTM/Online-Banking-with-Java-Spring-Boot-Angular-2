package com.userFront.identity.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.account.domain.PrimaryAccount;
import com.userFront.account.domain.SavingsAccount;
import com.userFront.account.service.AccountService;
import com.userFront.identity.domain.User;
import com.userFront.identity.domain.UserRole;

@Service
@Transactional
public class UserRegistrationService {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    public User registerUser(User user, Set<UserRole> userRoles) {
        PrimaryAccount primaryAccount = accountService.createPrimaryAccount();
        SavingsAccount savingsAccount = accountService.createSavingsAccount();

        user.setPrimaryAccountId(primaryAccount.getId());
        user.setSavingsAccountId(savingsAccount.getId());

        return userService.createUser(user, userRoles);
    }
}
