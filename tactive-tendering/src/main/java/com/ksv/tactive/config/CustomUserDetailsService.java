package com.ksv.tactive.config;

import com.ksv.tactive.project.dao.ContractorContactsDao;
import com.ksv.tactive.project.dao.ContractorFormInviteDao;
import com.ksv.tactive.project.dao.TenderInviteDao;
import com.ksv.tactive.project.entity.Contractor;
import com.ksv.tactive.project.entity.ContractorContacts;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.TenderInvites;
import com.ksv.tactive.util.ContractorStatus;
import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.Timestamp;
import java.util.UUID;

public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserDao dao;

	@Autowired
	private ContractorFormInviteDao inviteRepository;

	@Autowired
	private TenderInviteDao tenderInviteDao;

	@Autowired
	private ContractorContactsDao contactsDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = dao.findByUsername(username).orElse(null);
		if (user != null) {
			return new UserPrincipal(user);
		}

		ContractorFormInvite invite = inviteRepository.findByUsername(username).orElse(null);
		if (invite != null && invite.getContractorStatus() != ContractorStatus.VERIFIED) {
			User inviteUser = new User();
			inviteUser.setId(invite.getId().toString());
			inviteUser.setEmail(invite.getEmail());
			inviteUser.setName(invite.getName());
			inviteUser.setRole("CONTRACTOR");
			inviteUser.setPassword("");
			inviteUser.setUsername(invite.getEmail());
			return new UserPrincipal(inviteUser);
		}
		TenderInvites tenderInvite = tenderInviteDao.findByTempToken(username).orElse(null);
		if (tenderInvite != null) {
			if (tenderInvite.isSubmitted()) {
				throw new UsernameNotFoundException("Quote already submitted.");
			}
			if (tenderInvite.getTokenExpiry() != null &&
					tenderInvite.getTokenExpiry().before(new Timestamp(System.currentTimeMillis()))) {
				throw new UsernameNotFoundException("Link has expired.");
			}

			User tenderUser = new User();
			tenderUser.setId(tenderInvite.getId().toString());
			tenderUser.setUsername(tenderInvite.getTempToken());
			tenderUser.setRole("CONTRACTOR");
			tenderUser.setPassword("");
			Contractor contractor = tenderInvite.getContractor();
			if (contractor != null) {
				ContractorContacts contractorContacts = contactsDao.findByContractorId(contractor.getId());
				tenderUser.setName(contractorContacts != null ? contractorContacts.getName() : null);
				tenderUser.setEmail(contractorContacts != null ? contractorContacts.getEmail() : null);
			}

			return new UserPrincipal(tenderUser);
		}

		throw new UsernameNotFoundException("User not found");
	}
}