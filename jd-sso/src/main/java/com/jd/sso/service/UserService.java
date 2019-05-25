package com.jd.sso.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jd.pojo.JDResult;
import com.jd.pojo.User;

public interface UserService {
	// 检查数据是否可用
	JDResult checkData(String content, Integer type);

	JDResult createUser(User user);

	JDResult login(String username, String password,HttpServletRequest request,HttpServletResponse response);

	// 生成验证码
	void geneVerifyCode(String key, HttpServletResponse response) throws IOException;

	// 验证码校验
	JDResult checkVerifyCode(String key, String verifycode);

	JDResult getUserByToken(String token);	
	
	//quite
	JDResult quite(String token);
}
