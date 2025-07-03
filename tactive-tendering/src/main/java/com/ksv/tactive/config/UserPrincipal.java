package com.ksv.tactive.config;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ksv.tactive.entity.User;

public class UserPrincipal implements UserDetails{
	
	
	private static final long serialVersionUID = 1L;
	
	private final User user;
	
	public UserPrincipal(User user) {
		this.user=user;
	}

	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}
	
	
	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

}
