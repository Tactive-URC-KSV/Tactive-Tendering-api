package com.ksv.tactive.controller;
import java.util.HashMap;
import java.util.Map;

import com.ksv.tactive.dto.LoginDto;
import com.ksv.tactive.dto.ResetPasswordRequest;
import com.ksv.tactive.dto.VerifyOtpRequest;
import com.ksv.tactive.service.LoginRateLimiterService;
import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import com.ksv.tactive.service.UserService;
@RestController
@RequestMapping("/tactive")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private LoginRateLimiterService rateLimiterService;
	@Autowired
	private UserDao userDao;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
		String clientIp = rateLimiterService.getClientIp(request);
		String inputUsername = (loginDto != null && loginDto.getUsername() != null)
				? loginDto.getUsername().trim() : "";

		// Resolve canonical user if available (so 'admin' and 'admin@gmail.com' share the same lockout)
		User matchedUser = userDao.findByUsername(inputUsername)
				.or(() -> java.util.Optional.ofNullable(userDao.findByEmail(inputUsername)))
				.orElse(null);

		String accountKey = (matchedUser != null && matchedUser.getUsername() != null)
				? matchedUser.getUsername() : inputUsername;

		// 1. HARD LOCKOUT CHECK FIRST:
		// Even if the user knows the correct password, they CANNOT log in while locked.
		// They must wait out the lockout period completely.
		if (rateLimiterService.isBlocked(clientIp, accountKey)
				|| (matchedUser != null && matchedUser.getEmail() != null && rateLimiterService.isBlocked(clientIp, matchedUser.getEmail()))) {
			long remainingMinutes = Math.max(
					rateLimiterService.getRemainingLockoutMinutes(clientIp, accountKey),
					matchedUser != null && matchedUser.getEmail() != null
							? rateLimiterService.getRemainingLockoutMinutes(clientIp, matchedUser.getEmail())
							: 0
			);
			Map<String, Object> error = new HashMap<>();
			error.put("message", "Too many failed login attempts. Account is locked. " +
					"Please try again after " + remainingMinutes + " minute(s).");
			error.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
			error.put("remainingMinutes", remainingMinutes);
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
		}

		// 2. Attempt authentication
		try {
			String token = userService.login(inputUsername, loginDto != null ? loginDto.getPassword() : null);
			if (token != null) {
				// Login success after lockout expired (or within allowed attempts) -> Reset failure counters
				rateLimiterService.resetAttempts(clientIp, accountKey);
				if (matchedUser != null && matchedUser.getEmail() != null) {
					rateLimiterService.resetAttempts(null, matchedUser.getEmail());
				}
				Map<String, String> response = new HashMap<>();
				response.put("token", token);
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
		} catch (AuthenticationException e) {
			// Record failed attempt (IP incremented once, alias account linked)
			rateLimiterService.recordFailedAttempt(clientIp, accountKey);
			if (matchedUser != null && matchedUser.getEmail() != null && !matchedUser.getEmail().equalsIgnoreCase(accountKey)) {
				rateLimiterService.recordFailedAttempt(null, matchedUser.getEmail());
			}

			// If this attempt triggers the lockout
			if (rateLimiterService.isBlocked(clientIp, accountKey)) {
				long remainingMinutes = rateLimiterService.getRemainingLockoutMinutes(clientIp, accountKey);
				Map<String, Object> error = new HashMap<>();
				error.put("message", "Too many failed login attempts. Account is locked. " +
						"Please try again after " + remainingMinutes + " minute(s).");
				error.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
				error.put("remainingMinutes", remainingMinutes);
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
			}

			int remaining = rateLimiterService.getRemainingAttempts(clientIp, accountKey);
			Map<String, Object> error = new HashMap<>();
			error.put("message", "Invalid username or password. Remaining attempts: " + remaining);
			error.put("status", HttpStatus.UNAUTHORIZED.value());
			error.put("remainingAttempts", remaining);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication failed"));
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<?> getUserById(@PathVariable String userId) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId));
	}
	@GetMapping("/loggedin-user")
	public ResponseEntity<?> getLoggedInUser() {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(userService.findLoggedInUser());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("User not found");
		}
	}
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request){
		return ResponseEntity.ok(userService.logout(request.getHeader("Authorization")));
	}
	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestParam String username) throws Exception {
		String resetToken = userService.handleForgotPassword(username);
		return ResponseEntity.ok(Map.of(
				"resetToken", resetToken,
				"message", "If account exists, OTP sent"
		));
	}
	@PostMapping("/verify-reset-otp")
	public ResponseEntity<?> verifyResetOtp(
			@RequestBody VerifyOtpRequest request) throws Exception {
		String resetAuthToken = userService.verifyResetOtp(request);
		return ResponseEntity.ok(Map.of(
				"resetAuthToken", resetAuthToken,
				"message", "OTP verified successfully"
		));
	}
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) throws Exception {
		userService.handleResetPassword(request);
		return ResponseEntity.ok("Password updated successfully");
	}
}
