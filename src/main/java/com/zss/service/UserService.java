package com.zss.service;

import org.springframework.stereotype.Service;

import com.zss.pojo.UserInfo;

@Service
public interface UserService {
	
	public void addUser(UserInfo userInfo);

}
