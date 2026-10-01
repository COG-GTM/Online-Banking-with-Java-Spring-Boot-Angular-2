package com.userFront.dao;

import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.PrimaryTransaction;

public interface PrimaryTransactionDao extends CrudRepository<PrimaryTransaction, Long> {
}
