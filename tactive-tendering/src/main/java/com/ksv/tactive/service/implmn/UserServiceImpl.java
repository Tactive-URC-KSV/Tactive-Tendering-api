package com.ksv.tactive.service.implmn;
import com.ksv.tactive.config.CustomUserDetailsService;
import com.ksv.tactive.config.UserPrincipal;
import com.ksv.tactive.dto.ResetPasswordRequest;
import com.ksv.tactive.dto.VerifyOtpRequest;
import com.ksv.tactive.project.dao.ContractorContactsDao;
import com.ksv.tactive.project.entity.Contractor;
import com.ksv.tactive.project.entity.ContractorContacts;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.TenderInvites;
import com.ksv.tactive.service.EmailService;
import com.ksv.tactive.util.PasswordGenerator;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.ksv.tactive.filter.JwtService;
import com.ksv.tactive.service.UserService;
import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
	@Autowired
	private AuthenticationManager authManager;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private UserDao userDao;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private ContractorContactsDao contactsDao;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private EmailService emailService;
	@Autowired
	private SecureRandom secureRandom;
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
	@Override
	public String logout(String authHeader) {
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			jwtService.blacklistToken(authHeader.substring(7));
			return messageSource.getMessage("logout.success",null, Locale.ENGLISH);
		}
		return messageSource.getMessage("logout.failure",null, Locale.ENGLISH);
	}
	@Override
	public String handleForgotPassword(String username) throws NoSuchAlgorithmException {
		Optional<User> optionalUser = userDao.findByUsername(username);
		if (optionalUser.isEmpty()) {
			return null;
		}
		User user = optionalUser.get();
		String otp = generateOtp();
		String otpHash = hashOtp(otp);
		String resetToken =
				jwtService.generatePasswordResetToken(user.getUsername(), otpHash);
		emailService.sendOtp(user, otp);
		return resetToken;
	}
	private String generateOtp() {
		int otp = 100000 + secureRandom.nextInt(900000);
		return String.valueOf(otp);
	}
	private String hashOtp(String otp) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(otp.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(hash);
	}
	@Override
	public String verifyResetOtp(VerifyOtpRequest request) throws NoSuchAlgorithmException {
		Claims claims = jwtService.extractAllClaims(request.getResetToken());
		if (!"PASSWORD_RESET".equals(claims.get("tokenType"))) {
			throw new RuntimeException("Invalid token type");
		}
		String emailFromToken = claims.getSubject();
		String storedOtpHash = claims.get("otpHash", String.class);
		if (!emailFromToken.equals(request.getUsername())) {
			throw new RuntimeException("Invalid request");
		}
		String enteredOtpHash = hashOtp(request.getOtp());
		if (!storedOtpHash.equals(enteredOtpHash)) {
			throw new RuntimeException("Invalid OTP");
		}
		return jwtService.generateResetAuthToken(emailFromToken);
	}

	@Override
	public void handleResetPassword(ResetPasswordRequest request) throws NoSuchAlgorithmException {
        Claims claims = jwtService.extractAllClaims(request.getResetAuthToken());
        if (!"RESET_AUTH".equals(claims.get("tokenType"))) {
            throw new RuntimeException("Unauthorized request");
        }
        String usernameFromToken = claims.getSubject();
        if (!usernameFromToken.equals(request.getUsername())) {
            throw new RuntimeException("Invalid request");
        }
        User user = userDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
	}

	@Override
	public void addUser(User user) {
		String password = PasswordGenerator.generatePassword();
		user.setId(UUID.randomUUID().toString());
		user.setUsername(user.getEmail());
		user.setCreatedAt(Date.valueOf(LocalDate.now()));
		user.setEnabled(true);
		user.setPassword(passwordEncoder.encode(password));
		userDao.save(user);
		emailService.sentLoginCredentials(user, password);
	}
}
