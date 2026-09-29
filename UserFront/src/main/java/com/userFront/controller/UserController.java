package com.userFront.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userFront.domain.User;
import com.userFront.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	private static final String[] PROFILE_EDITABLE_FIELDS = {"firstName", "lastName", "email", "phone"};

	@Autowired
	private UserService userService;

	@InitBinder("user")
	public void initProfileBinder(WebDataBinder binder) {
		binder.setAllowedFields(PROFILE_EDITABLE_FIELDS);
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public String profile(Principal principal, Model model) {
		User user = userService.findByUsername(principal.getName());

		model.addAttribute("user", user);

		return "profile";
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public String profilePost(@ModelAttribute("user") User profileForm, Principal principal, Model model) {
		User user = userService.findByUsername(principal.getName());

		User emailOwner = userService.findByEmail(profileForm.getEmail());
		if (emailOwner != null && !emailOwner.getUserId().equals(user.getUserId())) {
			model.addAttribute("emailExists", true);
			model.addAttribute("user", user);
			return "profile";
		}

		user.setFirstName(profileForm.getFirstName());
		user.setLastName(profileForm.getLastName());
		user.setEmail(profileForm.getEmail());
		user.setPhone(profileForm.getPhone());

		userService.saveUser(user);

		model.addAttribute("user", user);

		return "profile";
	}

}
