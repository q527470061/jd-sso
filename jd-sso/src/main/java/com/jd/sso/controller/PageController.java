package com.jd.sso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/page")
public class PageController {

	@RequestMapping("/registe")
	public String showRegister() {
		return "registe";
	}

	@CrossOrigin
	@RequestMapping("/login")
	public String showLogin() {
		return "login";
	}

}
