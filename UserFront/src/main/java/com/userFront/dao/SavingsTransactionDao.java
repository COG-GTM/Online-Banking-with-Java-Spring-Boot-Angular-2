package com.userFront.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.SavingsTransaction;

public interface SavingsTransactionDao extends CrudRepository<SavingsTransaction, Long>,
        JpaSpecificationExecutor<SavingsTransaction> {

    List<SavingsTransaction> findAll();
}

