package com.zss.service.imp;

import org.springframework.beans.factory.annotation.Autowired;

import com.zss.dao.UserMapper;
import com.zss.pojo.UserInfo;
import com.zss.service.UserService;

public class UserServiceImp implements UserService {
    @Autowired
    UserMapper userMapper;
	@Override
	public void addUser(UserInfo userInfo) {
		// TODO Auto-generated method stub
		userMapper.insertUser(userInfo);
	}



}
