package com.ksv.tactive.controller;
import java.util.HashMap;
import java.util.Map;

import com.ksv.tactive.dto.LoginDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ksv.tactive.service.UserService;
@RestController
@RequestMapping("/tactive")
public class UserController {
	@Autowired
	private UserService userService;
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDto loginDto){
		String token=userService.login(loginDto.getUsername(), loginDto.getPassword());
		Map<String, String> response =new HashMap<>();
		response.put("token", token);
		if(token != null) {
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
	}
	@GetMapping("/user/{userId}")
	public ResponseEntity<?> getUserById(@PathVariable String userId) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("User not found");
		}
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
}
