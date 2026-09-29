package com.userFront.service.UserServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.userFront.domain.PrimaryAccount;
import com.userFront.domain.SavingsAccount;

/**
 * Loads account rows with a PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) lock so that
 * balance read-modify-write sequences are serialized per account for the
 * remainder of the caller's transaction.
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class AccountLockService {

	@PersistenceContext
	private EntityManager entityManager;

	public PrimaryAccount lockPrimaryAccount(Long id) {
		return lock(PrimaryAccount.class, id);
	}

	public SavingsAccount lockSavingsAccount(Long id) {
		return lock(SavingsAccount.class, id);
	}

	private <T> T lock(Class<T> type, Long id) {
		T account = entityManager.find(type, id);
		if (account == null) {
			throw new IllegalArgumentException(type.getSimpleName() + " " + id + " not found");
		}
		// refresh (rather than find with a lock) so any stale state already in the
		// persistence context is overwritten with the locked row's current values
		entityManager.refresh(account, LockModeType.PESSIMISTIC_WRITE);
		return account;
	}
}
