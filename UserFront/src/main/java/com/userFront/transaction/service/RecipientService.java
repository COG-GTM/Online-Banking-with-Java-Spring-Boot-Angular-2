package com.userFront.transaction.service;

import java.util.List;

import com.userFront.transaction.domain.Recipient;

public interface RecipientService {

	List<Recipient> findRecipientList(Long userId);

	Recipient saveRecipient(Recipient recipient);

	Recipient findRecipientByName(String recipientName);

	void deleteRecipientByName(String recipientName);
}
