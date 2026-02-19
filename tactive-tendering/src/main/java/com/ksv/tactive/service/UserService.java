package com.ksv.tactive.service;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.TenderInvites;
import org.springframework.stereotype.Service;
import com.ksv.tactive.user.entity.User;
@Service
public interface UserService {
	String login(String username, String password);
	User findLoggedInUser();
	User getUserById(String userId);
	String getTempToken(ContractorFormInvite formInvite);
	String getTempTokenForTender(TenderInvites tenderInvite);
    String logout(String authorization);
}
