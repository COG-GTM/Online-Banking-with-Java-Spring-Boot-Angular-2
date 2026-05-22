package com.userFront.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.userFront.identity.dao.UserDao;
import com.userFront.identity.domain.Authority;
import com.userFront.identity.domain.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

@Service
public class UserSecurityService implements UserDetailsService {

	private static final Logger LOG = LoggerFactory.getLogger(UserSecurityService.class);

	@Autowired
	private UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		if (user == null) {
			LOG.warn("Username {} not found", username);
			throw new UsernameNotFoundException("Username " + username + " not found");
		}
		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				user.isEnabled(),
				true, true, true,
				getAuthorities(user));
	}

	private Collection<? extends GrantedAuthority> getAuthorities(User user) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		user.getUserRoles().forEach(ur -> authorities.add(new Authority(ur.getRole().getName())));
		return authorities;
	}
}
