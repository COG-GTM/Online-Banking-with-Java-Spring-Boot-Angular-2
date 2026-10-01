package com.userFront.dao;

import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.SavingsTransaction;

public interface SavingsTransactionDao extends CrudRepository<SavingsTransaction, Long> {
}

