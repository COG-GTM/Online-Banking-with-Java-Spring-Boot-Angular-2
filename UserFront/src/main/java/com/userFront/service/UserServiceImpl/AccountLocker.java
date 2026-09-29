package com.userFront.service.UserServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

@Component
public class AccountLocker {

	@PersistenceContext
	private EntityManager entityManager;

	public PrimaryAccount lock(PrimaryAccount account) {
		return lock(PrimaryAccount.class, account, account.getId());
	}

	public SavingsAccount lock(SavingsAccount account) {
		return lock(SavingsAccount.class, account, account.getId());
	}

	private <T> T lock(Class<T> type, T account, Long id) {
		if (entityManager.contains(account)) {
			entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
			return account;
		}
		return entityManager.find(type, id, LockModeType.PESSIMISTIC_WRITE);
	}
}
