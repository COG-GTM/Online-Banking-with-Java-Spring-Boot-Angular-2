package com.userFront.transaction.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.userFront.transaction.dao.RecipientDao;
import com.userFront.transaction.domain.Recipient;

@Service
public class RecipientServiceImpl implements RecipientService {

	@Autowired
	private RecipientDao recipientDao;

	public List<Recipient> findRecipientList(Long userId) {
		return recipientDao.findByUserId(userId);
	}

	public Recipient saveRecipient(Recipient recipient) {
		return recipientDao.save(recipient);
	}

	public Recipient findRecipientByName(String recipientName) {
		return recipientDao.findByName(recipientName);
	}

	public void deleteRecipientByName(String recipientName) {
		recipientDao.deleteByName(recipientName);
	}
}
