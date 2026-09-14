package com.userFront.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.Recipient;

public interface RecipientDao extends CrudRepository<Recipient, Long> {
    List<Recipient> findAll();

    Recipient findByNameAndUserUsername(String recipientName, String username);

    void deleteByNameAndUserUsername(String recipientName, String username);
}
