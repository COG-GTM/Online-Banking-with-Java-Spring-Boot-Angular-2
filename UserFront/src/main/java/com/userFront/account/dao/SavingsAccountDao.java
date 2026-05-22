package com.userFront.account.dao;

import org.springframework.data.repository.CrudRepository;

import com.userFront.account.domain.SavingsAccount;

public interface SavingsAccountDao extends CrudRepository<SavingsAccount, Long> {

    SavingsAccount findByAccountNumber(int accountNumber);
}
