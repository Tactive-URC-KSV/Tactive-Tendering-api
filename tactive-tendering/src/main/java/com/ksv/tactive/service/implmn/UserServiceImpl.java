package com.ksv.tactive.service.implmn;
import com.ksv.tactive.config.CustomUserDetailsService;
import com.ksv.tactive.config.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ksv.tactive.filter.JwtService;
import com.ksv.tactive.service.UserService;
import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;
@Service
public class UserServiceImpl implements UserService{
	@Autowired
	private AuthenticationManager authManager;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private UserDao userDao;
	@Override
	public String login(String username, String password) {
		Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		if(authentication.isAuthenticated()) {
			UserPrincipal userDetails =
					(UserPrincipal) authentication.getPrincipal();
			return jwtService.generateToken(userDetails);
		}
		return null;
	}
	@Override
	public User findLoggedInUser() {
		Object authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (authenticatedUser instanceof UserDetails) {
			User user = userDao.findByUsername(((UserDetails) authenticatedUser).getUsername())
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			return user;
		}
		return null;
	}
	@Override
	public User getUserById(String userId) {
		return userDao.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
	}
}
