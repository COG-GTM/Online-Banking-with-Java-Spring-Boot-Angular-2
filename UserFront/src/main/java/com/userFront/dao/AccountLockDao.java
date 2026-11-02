package com.userFront.dao;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

@Repository
public class AccountLockDao {

	@PersistenceContext
	private EntityManager entityManager;

	public PrimaryAccount lockPrimaryAccount(Long id) {
		return lock(PrimaryAccount.class, id);
	}

	public SavingsAccount lockSavingsAccount(Long id) {
		return lock(SavingsAccount.class, id);
	}

	// refresh (not find) so an instance already cached by open-session-in-view is re-read under SELECT ... FOR UPDATE.
	private <T> T lock(Class<T> type, Long id) {
		T account = id == null ? null : entityManager.find(type, id);
		if (account == null) {
			throw new IllegalStateException(type.getSimpleName() + " not found");
		}
		entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
		return account;
	}
}
