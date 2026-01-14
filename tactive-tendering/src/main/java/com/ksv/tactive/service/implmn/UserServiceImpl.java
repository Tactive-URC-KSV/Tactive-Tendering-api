package com.ksv.tactive.service.implmn;
import com.ksv.tactive.config.CustomUserDetailsService;
import com.ksv.tactive.config.UserPrincipal;
import com.ksv.tactive.project.dao.ContractorContactsDao;
import com.ksv.tactive.project.entity.Contractor;
import com.ksv.tactive.project.entity.ContractorContacts;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.TenderInvites;
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
	@Autowired
	private ContractorContactsDao contactsDao;
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

	@Override
	public String getTempToken(ContractorFormInvite formInvite) {
		User user = new User();
		user.setId(formInvite.getId());
		user.setRole("CONTRACTOR");
		user.setName(formInvite.getName());
		user.setUsername(formInvite.getUsername());
		user.setPassword("");
		UserPrincipal userPrincipal = new UserPrincipal(user);
		return jwtService.generateToken(userPrincipal);
	}
	@Override
	public String getTempTokenForTender(TenderInvites tenderInvite) {
		User user = new User();
		user.setId(tenderInvite.getId());
		user.setUsername(tenderInvite.getTempToken());
		Contractor contractor = tenderInvite.getContractor();
		if (contractor != null) {
			ContractorContacts contractorContacts = contactsDao.findByContractorId(contractor.getId());
			user.setName(contractorContacts != null ? contractorContacts.getName() : null);
			user.setEmail(contractorContacts != null ? contractorContacts.getEmail() : null);
		}
		user.setRole("CONTRACTOR");
		user.setPassword("");
		UserPrincipal userPrincipal = new UserPrincipal(user);
		return jwtService.generateToken(userPrincipal);
	}
}
