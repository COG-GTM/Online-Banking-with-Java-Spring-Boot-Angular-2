package com.userFront.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userFront.domain.User;
import com.userFront.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public String profile(Principal principal, Model model) {
		User user = userService.findByUsername(principal.getName());

		model.addAttribute("user", user);

		return "profile";
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public String profilePost(@ModelAttribute("profileForm") ProfileForm form, Principal principal, Model model) {
		User user = userService.findByUsername(principal.getName());

		User emailOwner = userService.findByEmail(form.getEmail());
		if (emailOwner != null && !emailOwner.getUserId().equals(user.getUserId())) {
			model.addAttribute("emailExists", true);
			model.addAttribute("user", user);
			return "profile";
		}

		user.setFirstName(form.getFirstName());
		user.setLastName(form.getLastName());
		user.setEmail(form.getEmail());
		user.setPhone(form.getPhone());

		userService.saveUser(user);

		model.addAttribute("user", user);

		return "profile";
	}

}
