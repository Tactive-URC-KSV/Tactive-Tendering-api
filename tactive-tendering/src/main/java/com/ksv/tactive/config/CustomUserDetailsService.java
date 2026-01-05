package com.ksv.tactive.config;
import com.ksv.tactive.project.dao.ContractorFormInviteDao;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;
public class CustomUserDetailsService implements UserDetailsService{
	@Autowired
	private UserDao dao;
	@Autowired
	private ContractorFormInviteDao inviteRepository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = dao.findByUsername(username)
	            .orElse(null);
		if(user != null){
			return new UserPrincipal(user);
		}
		ContractorFormInvite invite = inviteRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
		User inviteUser = new User();
		inviteUser.setId(invite.getId());
		inviteUser.setEmail(invite.getEmail());
		inviteUser.setName(invite.getName());
		inviteUser.setRole("CONTRACTOR");
		inviteUser.setPassword("");
		return new UserPrincipal(inviteUser);
	}
}
