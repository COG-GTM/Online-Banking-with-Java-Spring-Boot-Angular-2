package com.userFront.transaction.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.userFront.account.dao.PrimaryAccountDao;
import com.userFront.account.dao.SavingsAccountDao;
import com.userFront.account.domain.PrimaryAccount;
import com.userFront.account.domain.SavingsAccount;
import com.userFront.dao.UserDao;
import com.userFront.domain.User;
import com.userFront.transaction.domain.Recipient;
import com.userFront.transaction.service.LedgerService;
import com.userFront.transaction.service.RecipientService;

@Controller
@RequestMapping("/transfer")
public class TransferController {

	@Autowired
	private LedgerService ledgerService;

	@Autowired
	private RecipientService recipientService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@RequestMapping(value = "/betweenAccounts", method = RequestMethod.GET)
	public String betweenAccounts(Model model) {
		model.addAttribute("transferFrom", "");
		model.addAttribute("transferTo", "");
		model.addAttribute("amount", "");

		return "betweenAccounts";
	}

	@RequestMapping(value = "/betweenAccounts", method = RequestMethod.POST)
	public String betweenAccountsPost(@ModelAttribute("transferFrom") String transferFrom,
			@ModelAttribute("transferTo") String transferTo, @ModelAttribute("amount") String amount,
			Principal principal) throws Exception {
		User user = userDao.findByUsername(principal.getName());
		PrimaryAccount primaryAccount = primaryAccountDao.findOne(user.getPrimaryAccountId());
		SavingsAccount savingsAccount = savingsAccountDao.findOne(user.getSavingsAccountId());
		ledgerService.betweenAccountsTransfer(transferFrom, transferTo, amount, primaryAccount, savingsAccount);

		return "redirect:/userFront";
	}

	@RequestMapping(value = "/recipient", method = RequestMethod.GET)
	public String recipient(Model model, Principal principal) {
		User user = userDao.findByUsername(principal.getName());
		List<Recipient> recipientList = recipientService.findRecipientList(user.getUserId());

		Recipient recipient = new Recipient();

		model.addAttribute("recipientList", recipientList);
		model.addAttribute("recipient", recipient);

		return "recipient";
	}

	@RequestMapping(value = "/recipient/save", method = RequestMethod.POST)
	public String recipientPost(@ModelAttribute("recipient") Recipient recipient, Principal principal) {

		User user = userDao.findByUsername(principal.getName());
		recipient.setUserId(user.getUserId());
		recipientService.saveRecipient(recipient);

		return "redirect:/transfer/recipient";
	}

	@RequestMapping(value = "/recipient/edit", method = RequestMethod.GET)
	public String recipientEdit(@RequestParam(value = "recipientName") String recipientName, Model model,
			Principal principal) {

		Recipient recipient = recipientService.findRecipientByName(recipientName);
		User user = userDao.findByUsername(principal.getName());
		List<Recipient> recipientList = recipientService.findRecipientList(user.getUserId());

		model.addAttribute("recipientList", recipientList);
		model.addAttribute("recipient", recipient);

		return "recipient";
	}

	@RequestMapping(value = "/recipient/delete", method = RequestMethod.GET)
	@Transactional
	public String recipientDelete(@RequestParam(value = "recipientName") String recipientName, Model model,
			Principal principal) {

		recipientService.deleteRecipientByName(recipientName);

		User user = userDao.findByUsername(principal.getName());
		List<Recipient> recipientList = recipientService.findRecipientList(user.getUserId());

		Recipient recipient = new Recipient();
		model.addAttribute("recipient", recipient);
		model.addAttribute("recipientList", recipientList);

		return "recipient";
	}

	@RequestMapping(value = "/toSomeoneElse", method = RequestMethod.GET)
	public String toSomeoneElse(Model model, Principal principal) {
		User user = userDao.findByUsername(principal.getName());
		List<Recipient> recipientList = recipientService.findRecipientList(user.getUserId());

		model.addAttribute("recipientList", recipientList);
		model.addAttribute("accountType", "");

		return "toSomeoneElse";
	}

	@RequestMapping(value = "/toSomeoneElse", method = RequestMethod.POST)
	public String toSomeoneElsePost(@ModelAttribute("recipientName") String recipientName,
			@ModelAttribute("accountType") String accountType, @ModelAttribute("amount") String amount,
			Principal principal) {
		User user = userDao.findByUsername(principal.getName());
		PrimaryAccount primaryAccount = primaryAccountDao.findOne(user.getPrimaryAccountId());
		SavingsAccount savingsAccount = savingsAccountDao.findOne(user.getSavingsAccountId());
		Recipient recipient = recipientService.findRecipientByName(recipientName);
		ledgerService.toSomeoneElseTransfer(recipient, accountType, amount, primaryAccount, savingsAccount);

		return "redirect:/userFront";
	}
}
