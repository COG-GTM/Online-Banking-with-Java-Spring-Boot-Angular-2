package com.userFront.identity.dao;

import org.springframework.data.repository.CrudRepository;

import com.userFront.identity.domain.Role;

public interface RoleDao extends CrudRepository<Role, Integer> {

	Role findByName(String name);
}
