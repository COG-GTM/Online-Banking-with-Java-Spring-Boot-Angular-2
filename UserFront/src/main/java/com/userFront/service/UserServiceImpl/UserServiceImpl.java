package com.userFront.service.UserServiceImpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.dao.RoleDao;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.domain.security.Role;
import com.userFront.domain.security.UserRole;
import com.userFront.dto.SignupForm;
import com.userFront.service.AccountService;
import com.userFront.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private AccountService accountService;
	
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

	public User createUser(SignupForm signupForm) {
		User localUser = userDao.findByUsername(signupForm.getUsername());

		if (localUser != null) {
			LOG.info("User with username {} already exist. Nothing will be done. ", signupForm.getUsername());
			return localUser;
		}

		Role userRole = roleDao.findByName("ROLE_USER");
		if (userRole == null) {
			throw new IllegalStateException("ROLE_USER is not configured");
		}

		User user = new User();
		user.setFirstName(signupForm.getFirstName());
		user.setLastName(signupForm.getLastName());
		user.setPhone(signupForm.getPhone());
		user.setEmail(signupForm.getEmail());
		user.setUsername(signupForm.getUsername());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.getUserRoles().add(new UserRole(user, userRole));
		user.setPrimaryAccount(accountService.createPrimaryAccount());
		user.setSavingsAccount(accountService.createSavingsAccount());

		return userDao.save(user);
	}

	public boolean checkUserExists(String username, String email) {
		if (checkUsernameExists(username) || checkEmailExists(email)) {
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
	
	
}
