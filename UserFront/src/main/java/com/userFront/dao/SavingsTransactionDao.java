package com.userFront.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.SavingsAccount;
import com.userFront.domain.SavingsTransaction;

public interface SavingsTransactionDao extends CrudRepository<SavingsTransaction, Long> {

    List<SavingsTransaction> findAll();

    Page<SavingsTransaction> findBySavingsAccountOrderByDateDescIdDesc(SavingsAccount savingsAccount, Pageable pageable);
}
