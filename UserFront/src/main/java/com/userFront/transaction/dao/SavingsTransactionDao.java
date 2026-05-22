package com.userFront.transaction.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userFront.transaction.domain.SavingsTransaction;

public interface SavingsTransactionDao extends CrudRepository<SavingsTransaction, Long> {

    List<SavingsTransaction> findAll();
}

