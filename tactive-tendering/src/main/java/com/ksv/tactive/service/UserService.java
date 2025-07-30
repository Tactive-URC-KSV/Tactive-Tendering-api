package com.ksv.tactive.service;

import org.springframework.stereotype.Service;

import com.ksv.tactive.user.entity.User;

@Service
public interface UserService {

	String login(String username, String password);
	User findLoggedInUser();
	User getUserById(String userId);
}
