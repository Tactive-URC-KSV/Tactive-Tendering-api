package com.ksv.tactive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;

public class CustomUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserDao dao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = dao.findByUsername(username)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
	    return new UserPrincipal(user);
	}

}
