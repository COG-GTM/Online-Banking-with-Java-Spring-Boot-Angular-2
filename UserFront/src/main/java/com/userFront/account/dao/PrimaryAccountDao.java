package com.userFront.account.dao;

import org.springframework.data.repository.CrudRepository;

import com.userFront.account.domain.PrimaryAccount;

public interface PrimaryAccountDao extends CrudRepository<PrimaryAccount, Long> {

    PrimaryAccount findByAccountNumber(int accountNumber);
}
