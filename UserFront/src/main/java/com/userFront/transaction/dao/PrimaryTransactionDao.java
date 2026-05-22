package com.userFront.transaction.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userFront.transaction.domain.PrimaryTransaction;

public interface PrimaryTransactionDao extends CrudRepository<PrimaryTransaction, Long> {

    List<PrimaryTransaction> findAll();
}
