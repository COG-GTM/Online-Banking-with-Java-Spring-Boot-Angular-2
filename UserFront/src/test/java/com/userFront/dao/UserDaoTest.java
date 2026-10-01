package com.userFront.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
public class UserDaoTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserDao userDao;

	@Before
	public void setUp() {
		Role userRole = role(1, "ROLE_USER");
		Role adminRole = role(2, "ROLE_ADMIN");

		User user = new User();
		user.setUsername("alice");
		user.setPassword("secret");
		user.setEmail("alice@example.com");
		user.getUserRoles().add(new UserRole(user, userRole));
		user.getUserRoles().add(new UserRole(user, adminRole));
		entityManager.persist(user);

		entityManager.flush();
		entityManager.clear();
	}

	@Test
	public void findByUsernameDoesNotLoadRoles() {
		User user = userDao.findByUsername("alice");

		assertThat(user).isNotNull();
		assertThat(Hibernate.isInitialized(user.getUserRoles())).isFalse();
	}

	@Test
	public void findWithUserRolesByUsernameFetchesRolesForAuthorities() {
		User user = userDao.findWithUserRolesByUsername("alice");

		assertThat(user).isNotNull();
		assertThat(Hibernate.isInitialized(user.getUserRoles())).isTrue();
		entityManager.clear();

		Set<String> authorities = new HashSet<>();
		for (GrantedAuthority authority : user.getAuthorities()) {
			authorities.add(authority.getAuthority());
		}
		assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
	}

	@Test
	public void findWithUserRolesByUsernameReturnsNullForUnknownUser() {
		assertThat(userDao.findWithUserRolesByUsername("nobody")).isNull();
	}

	private Role role(int id, String name) {
		Role role = new Role();
		role.setRoleId(id);
		role.setName(name);
		return entityManager.persist(role);
	}
}
