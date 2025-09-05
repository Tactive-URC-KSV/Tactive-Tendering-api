package com.ksv.tactive.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ksv.tactive.service.UserService;

@RestController
@RequestMapping("/tactive")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestParam String username,
			@RequestParam String password){
		String token=userService.login(username, password);
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

}
