package com.ksv.tactive.service;
import com.ksv.tactive.dto.ResetPasswordRequest;
import com.ksv.tactive.dto.VerifyOtpRequest;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.TenderInvites;
import org.springframework.stereotype.Service;
import com.ksv.tactive.user.entity.User;

import java.security.NoSuchAlgorithmException;

@Service
public interface UserService {
	String login(String username, String password);
	User findLoggedInUser();
	User getUserById(String userId);
	String getTempToken(ContractorFormInvite formInvite);
	String getTempTokenForTender(TenderInvites tenderInvite);
    String logout(String authorization);
    String handleForgotPassword(String username) throws NoSuchAlgorithmException;
	void handleResetPassword(ResetPasswordRequest request) throws NoSuchAlgorithmException;
	String verifyResetOtp(VerifyOtpRequest request) throws NoSuchAlgorithmException;
	void addUser(User user);
}
