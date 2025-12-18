package com.ksv.tactive.config;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.ksv.tactive.user.entity.User;
public class UserPrincipal implements UserDetails{
	private static final long serialVersionUID = 1L;
	private User user;
	public UserPrincipal(User user) {
		this.user=user;
	}
	public User getUser() {
		return user;
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(
				new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
		);
	}
	public String getRole(){
		return user.getRole().toUpperCase();
	}
	public String getUserName(){
		return user.getName();
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
		return true;
	}
}
