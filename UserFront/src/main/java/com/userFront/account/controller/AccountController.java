package com.userFront.account.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userFront.account.dao.PrimaryAccountDao;
import com.userFront.account.dao.SavingsAccountDao;
import com.userFront.account.domain.PrimaryAccount;
import com.userFront.account.domain.SavingsAccount;
import com.userFront.account.service.AccountService;
import com.userFront.identity.dao.UserDao;
import com.userFront.identity.domain.User;
import com.userFront.transaction.domain.PrimaryTransaction;
import com.userFront.transaction.domain.SavingsTransaction;
import com.userFront.transaction.service.LedgerService;

@Controller
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private UserDao userDao;

	@Autowired
	private AccountService accountService;

	@Autowired
	private LedgerService ledgerService;

	@Autowired
	private PrimaryAccountDao primaryAccountDao;

	@Autowired
	private SavingsAccountDao savingsAccountDao;

	@RequestMapping("/primaryAccount")
	public String primaryAccount(Model model, Principal principal) {
		List<PrimaryTransaction> primaryTransactionList = ledgerService
				.findPrimaryTransactionList(principal.getName());

		User user = userDao.findByUsername(principal.getName());
		PrimaryAccount primaryAccount = primaryAccountDao.findOne(user.getPrimaryAccountId());

		model.addAttribute("primaryAccount", primaryAccount);
		model.addAttribute("primaryTransactionList", primaryTransactionList);

		return "primaryAccount";
	}

	@RequestMapping("/savingsAccount")
	public String savingsAccount(Model model, Principal principal) {
		List<SavingsTransaction> savingsTransactionList = ledgerService
				.findSavingsTransactionList(principal.getName());
		User user = userDao.findByUsername(principal.getName());
		SavingsAccount savingsAccount = savingsAccountDao.findOne(user.getSavingsAccountId());

		model.addAttribute("savingsAccount", savingsAccount);
		model.addAttribute("savingsTransactionList", savingsTransactionList);

		return "savingsAccount";
	}

	@RequestMapping(value = "/deposit", method = RequestMethod.GET)
	public String deposit(Model model) {
		model.addAttribute("accountType", "");
		model.addAttribute("amount", "");

		return "deposit";
	}

	@RequestMapping(value = "/deposit", method = RequestMethod.POST)
	public String depositPOST(@ModelAttribute("amount") String amount,
			@ModelAttribute("accountType") String accountType, Principal principal) {
		accountService.deposit(accountType, Double.parseDouble(amount), principal);

		return "redirect:/userFront";
	}

	@RequestMapping(value = "/withdraw", method = RequestMethod.GET)
	public String withdraw(Model model) {
		model.addAttribute("accountType", "");
		model.addAttribute("amount", "");

		return "withdraw";
	}

	@RequestMapping(value = "/withdraw", method = RequestMethod.POST)
	public String withdrawPOST(@ModelAttribute("amount") String amount,
			@ModelAttribute("accountType") String accountType, Principal principal) {
		accountService.withdraw(accountType, Double.parseDouble(amount), principal);

		return "redirect:/userFront";
	}
}
