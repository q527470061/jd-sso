package com.jd.sso.service.imp;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.jd.mapper.UserMapper;
import com.jd.pojo.CartMergePojo;
import com.jd.pojo.JDResult;
import com.jd.pojo.User;
import com.jd.pojo.UserExample;
import com.jd.pojo.UserExample.Criteria;
import com.jd.sso.dao.JedisClient;
import com.jd.sso.service.UserService;
import com.jd.sso.util.CookieUtils;
import com.jd.util.JsonUtils;
import com.jd.util.verifycode.VerifyCode;

@Service
public class UserServiceImp implements UserService {

	
	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private UserMapper userMapper;

	// redis
	@Autowired
	private JedisClient jedisClient;
	// user token
	@Value("${REDIS_USER_SESSION_KEY}")
	private String REDIS_USER_SESSION_KEY;
	@Value("${SSO_SESSION_EXPIRE}")
	private int SSO_SESSION_EXPIRE;
	// 验证码
	@Value("${REDIS_USER_VERIFYCODE}")
	private String REDIS_USER_VERIFYCODE;
	@Value("${SSO_VERIFYCODE_EXPIRE}")
	private int SSO_VERIFYCODE_EXPIRE;

	// 邮箱有关
	@Autowired
	// 用于发送邮件
	private JavaMailSender jms;
	@Value("${SUBJECT}")
	private String SUBJECT;
	@Value("${CONTENT}")
	private String CONTENT;
	@Value("${FROM}")
	private String FROM;

	// cookie
	@Value("${COOKIE_NAME}")
	private String COOKIE_NAME;

	// 校验数据是否可用
	public JDResult checkData(String content, Integer type) {

		UserExample example = new UserExample();
		Criteria criteria = example.createCriteria();
		// 数据类型的的校验，用户名 :1 , 电话校验 :2 , email校验:3
		if (1 == type) {
			criteria.andUsernameEqualTo(content);
		} else if (2 == type) {
			criteria.andPhoneEqualTo(content);
		} else {
			criteria.andEmailEqualTo(content);
		}

		List<User> list = userMapper.selectByExample(example);
		if (list == null || list.size() == 0) {
			return JDResult.ok(true);
		}
		return JDResult.ok(false);

	}

	@Override
	@Transactional
	public JDResult createUser(User user) {
		user.setUpdated(new Date());
		user.setCreated(new Date());
		// md5加密
		user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
		// 邮箱激活验证码
		user.setErifyCode((UUID.randomUUID() + "").substring(0, 10));
		// 设置激活状态
		user.setState(false);
		try {
			userMapper.insert(user);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 用于封装邮件信息的实例
		SimpleMailMessage smm = new SimpleMailMessage();
		// 由谁来发送邮件
		smm.setFrom(FROM);
		// 邮件主题
		smm.setSubject(SUBJECT);
		// 邮件内容
		String content = MessageFormat.format(CONTENT, user.getErifyCode());
		smm.setText(content);
		// 接受邮件
		smm.setTo(user.getEmail());
		try {
			jms.send(smm);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return JDResult.ok();

	}

	@Override
	public JDResult login(String username, String password, HttpServletRequest request, HttpServletResponse response) {

		UserExample example = new UserExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		List<User> list = userMapper.selectByExample(example);
		// 如果没有此用户名
		if (null == list || list.size() == 0) {
			return JDResult.build(400, "用户名或密码错误");
		}
		User user = list.get(0);
		// 比对密码
		if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
			return JDResult.build(400, "用户名或密码错误");
		}
		// 生成token
		String token = UUID.randomUUID().toString();
		// 保存用户之前，把用户对象中的密码清空。
		user.setPassword(null);
		// 把用户信息写入redis

		jedisClient.set(REDIS_USER_SESSION_KEY + ":" + token, JsonUtils.objectToJson(user)); // 设置session的过期时间
		jedisClient.expire(REDIS_USER_SESSION_KEY + ":" + token, SSO_SESSION_EXPIRE);

		// 將token填入cookie
		CookieUtils.setCookie(request, response, COOKIE_NAME, token);
		
		//发送消息
		//创建合并对象
		String tmp_user_key=CookieUtils.getCookieValue(request, "tmp_user_key");
		Map<String,Object> message=new HashMap<String, Object>();
		message.put("userId", user.getId());
		message.put("tmp_user_key", tmp_user_key);
		try {
			SendMergeCart(message);
		}catch(Exception e) {
			e.printStackTrace();
		}
		// 返回token
		return JDResult.ok(token);

	}
	
	
	//合并购物车消息
	public void SendMergeCart(Map<String,Object> message) {
		Destination destination = new ActiveMQQueue("mergeCart");
		jmsTemplate.convertAndSend(destination,message);
	}
	

	@Override
	public JDResult checkVerifyCode(String key, String verifycode) {
		// 从redis取出验证码
		String vcode = jedisClient.get(REDIS_USER_VERIFYCODE + ":" + key);
		if (StringUtils.isBlank(vcode)) {
			return JDResult.build(400, "验证码已失效");
		} else if (StringUtils.isBlank(verifycode) || !vcode.equalsIgnoreCase(verifycode.trim()))
			return JDResult.build(400, "验证码错误");
		return JDResult.ok();
	}

	@Override
	public void geneVerifyCode(String key, HttpServletResponse response) throws IOException {
		VerifyCode vc = new VerifyCode();
		BufferedImage image = vc.getImage();// 获取一次性验证码图片
		// 获取验证码的值
		String code = vc.getText();
		// 验证码保存到redis里
		jedisClient.set(REDIS_USER_VERIFYCODE + ":" + key, code); // 设置验证码的过期时间
		jedisClient.expire(REDIS_USER_VERIFYCODE + ":" + key, SSO_VERIFYCODE_EXPIRE);
		VerifyCode.output(image, response.getOutputStream());
	}

	@Override
	public JDResult getUserByToken(String token) {

		// 根据token从redis中查询用户信息
		String json = jedisClient.get(REDIS_USER_SESSION_KEY + ":" + token);
		// 判断是否为空
		if (StringUtils.isBlank(json)) {
			return JDResult.build(400, "登录状态已经过期，请重新登录");
		}
		// 更新过期时间
		jedisClient.expire(REDIS_USER_SESSION_KEY + ":" + token, SSO_SESSION_EXPIRE);
		// 返回用户信息
		return JDResult.ok(JsonUtils.jsonToPojo(json, User.class));
	}

	@Override
	public JDResult quite(String token) {
		try {
		jedisClient.del(REDIS_USER_SESSION_KEY + ":" + token);
		return JDResult.ok();
		}catch (Exception e) {
			return JDResult.build(500, e.getMessage());
		}
	}

}
