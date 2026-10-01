package com.userFront.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.Recipient;

public interface RecipientDao extends CrudRepository<Recipient, Long> {
    List<Recipient> findAll();

    List<Recipient> findByUser_Username(String username);

    Recipient findByName(String recipientName);

    void deleteByName(String recipientName);
}
