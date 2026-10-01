package com.userFront.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import com.userFront.domain.User;

public interface UserDao extends CrudRepository<User, Long> {

	User findByUsername(String username);
	User findByEmail(String email);

	@EntityGraph(attributePaths = {"primaryAccount", "savingsAccount"}, type = EntityGraph.EntityGraphType.LOAD)
	Page<User> findAll(Pageable pageable);
}
