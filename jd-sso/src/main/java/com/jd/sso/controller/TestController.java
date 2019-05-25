package com.jd.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	// 用于发送邮件
	private JavaMailSender jms;

	@RequestMapping("/test/mail")
	public String sendEmail(String message) {
		// 用于封装邮件信息的实例
		SimpleMailMessage smm = new SimpleMailMessage();
		// 由谁来发送邮件
		smm.setFrom("qin527470061@163.com");
		// 邮件主题
		smm.setSubject("Hello");
		// 邮件内容
		smm.setText("<h1 style='color:red'>Hello SpringBoot_Email</h1>");
		// 接受邮件
		smm.setTo("527470061@qq.com");
		try {
			jms.send(smm);
			return "发送成功";
		} catch (Exception e) {
			return "发送失败///" + e.getMessage();
		}
	}
}
