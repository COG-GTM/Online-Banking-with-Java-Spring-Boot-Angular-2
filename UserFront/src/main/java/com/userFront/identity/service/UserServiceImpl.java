package com.userFront.identity.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.identity.api.UserLookup;
import com.userFront.identity.dao.RoleDao;
import com.userFront.identity.dao.UserDao;
import com.userFront.identity.domain.User;
import com.userFront.identity.domain.UserRole;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserLookup {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public void save(User user) {
		userDao.save(user);
	}

	public User findByUsername(String username) {
		return userDao.findByUsername(username);
	}

	public User findByEmail(String email) {
		return userDao.findByEmail(email);
	}

	public User createUser(User user, Set<UserRole> userRoles) {
		User localUser = userDao.findByUsername(user.getUsername());

		if (localUser != null) {
			LOG.info("User with username {} already exist. Nothing will be done. ", user.getUsername());
		} else {
			String encryptedPassword = passwordEncoder.encode(user.getPassword());
			user.setPassword(encryptedPassword);

			for (UserRole ur : userRoles) {
				roleDao.save(ur.getRole());
			}

			user.getUserRoles().addAll(userRoles);

			localUser = userDao.save(user);
		}

		return localUser;
	}

	public boolean checkUserExists(String username, String email) {
		if (checkUsernameExists(username) || checkEmailExists(username)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkUsernameExists(String username) {
		if (null != findByUsername(username)) {
			return true;
		}

		return false;
	}

	public boolean checkEmailExists(String email) {
		if (null != findByEmail(email)) {
			return true;
		}

		return false;
	}

	public User saveUser(User user) {
		return userDao.save(user);
	}

	public void enableUser(String username) {
		User user = findByUsername(username);
		user.setEnabled(true);
		userDao.save(user);
	}

	public void disableUser(String username) {
		User user = findByUsername(username);
		user.setEnabled(false);
		System.out.println(user.isEnabled());
		userDao.save(user);
		System.out.println(username + " is disabled.");
	}

	public List<User> findUserList() {
        return userDao.findAll();
    }

	public Set<UserRole> getDefaultUserRoles(User user) {
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, roleDao.findByName("ROLE_USER")));
		return userRoles;
	}

	@Override
	public Long resolveUserId(String username) {
		User user = findByUsername(username);
		return user != null ? user.getUserId() : null;
	}

	@Override
	public boolean isEnabled(String username) {
		User user = findByUsername(username);
		return user != null && user.isEnabled();
	}
}
