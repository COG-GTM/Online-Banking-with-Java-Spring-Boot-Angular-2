package com.userFront.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.Recipient;
import com.userFront.domain.User;

public interface RecipientDao extends CrudRepository<Recipient, Long> {
    List<Recipient> findAll();

    List<Recipient> findByUser(User user);

    Recipient findByNameAndUser(String recipientName, User user);

    Recipient findByIdAndUser(Long id, User user);

    void deleteByNameAndUser(String recipientName, User user);
}
