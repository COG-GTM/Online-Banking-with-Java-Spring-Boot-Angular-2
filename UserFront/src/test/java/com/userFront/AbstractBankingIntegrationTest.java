package com.userFront;

import java.util.HashSet;
import java.util.Set;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.service.UserService;

/**
 * Shared setup for the integration tests that lock in the banking behaviour
 * before any framework upgrade. Each test runs against an in-memory H2 database
 * (see src/test/resources/application.properties) inside its own transaction so
 * the persisted state is rolled back afterwards.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public abstract class AbstractBankingIntegrationTest {

    protected static final int ROLE_USER_ID = 1;
    protected static final int ROLE_ADMIN_ID = 2;

    @Autowired
    protected UserService userService;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected RoleDao roleDao;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    protected User createUser(String username, String email, String rawPassword) {
        return createUser(username, email, rawPassword, ROLE_USER_ID, "ROLE_USER");
    }

    protected User createUser(String username, String email, String rawPassword, int roleId, String roleName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(rawPassword);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("0000000000");

        Role role = new Role();
        role.setRoleId(roleId);
        role.setName(roleName);

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user, role));

        return userService.createUser(user, userRoles);
    }
}
