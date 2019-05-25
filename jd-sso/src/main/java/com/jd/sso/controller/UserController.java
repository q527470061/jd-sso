package com.jd.sso.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jd.pojo.JDResult;
import com.jd.pojo.User;
import com.jd.sso.service.UserService;
import com.jd.util.ExceptionUtil;

@RequestMapping("/sso/user")
@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@CrossOrigin("*")
	@RequestMapping("/check/{param}/{type}")
	public JDResult checkData(@PathVariable String param, @PathVariable Integer type) {
		// 校验是否为空
		if (StringUtils.isBlank(param)) {
			return JDResult.build(400, "校验内容不能为空");
		}
		if (type == null) {
			return JDResult.build(400, "校验内容类型不能为空");
		}
		if (type != 1 && type != 2 && type != 3) {
			return JDResult.build(400, "校验内容类型错误");
		}
		try {
			JDResult jdResult = userService.checkData(param, type);
			return jdResult;
		} catch (Exception e) {
			e.printStackTrace();
			return JDResult.build(500, ExceptionUtil.getStackTrace(e));
		}

	}

	// 生成验证码
	@CrossOrigin("*")
	@RequestMapping("/geneVerifyCode/{key}")
	public void geneVerifyCode(@PathVariable String key, HttpServletResponse response) throws IOException {
		try {
			userService.geneVerifyCode(key, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 校验验证码
	@RequestMapping("/checkVerifyCode/{key}/{verifycode}")
	@ResponseBody
	public JDResult checkVerifyCode(@PathVariable String key, @PathVariable String verifycode,
			HttpServletRequest request) {
		try {
			JDResult result = userService.checkVerifyCode(key, verifycode);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return JDResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}

	@CrossOrigin("*")
	@RequestMapping("/registe")
	public JDResult createUser(User user) {
		try {
			JDResult result = userService.createUser(user);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return JDResult.build(500, ExceptionUtil.getStackTrace(e));
		}

	}

	@CrossOrigin("*")
	@RequestMapping("/login")
	public JDResult userLogin(String username, String password, HttpServletRequest request,
			HttpServletResponse response) {
		// 校验是否为空
		if (StringUtils.isBlank(username)) {
			return JDResult.build(400, "用户名为空");
		}
		// 校验是否为空
		if (StringUtils.isBlank(password)) {
			return JDResult.build(400, "密码为空");
		}

		try {
			JDResult result = userService.login(username, password, request, response);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return JDResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}

	@CrossOrigin("*")
	@RequestMapping("/token/{token}")
	public JDResult getUserByToken(@PathVariable String token) {
		try {
			JDResult result = userService.getUserByToken(token);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return JDResult.build(500, ExceptionUtil.getStackTrace(e));
		}

	}
	
	@CrossOrigin("*")
	@RequestMapping("/quite/{token}")
	public JDResult quite(@PathVariable String token) {
		return userService.quite(token);
	}

}
