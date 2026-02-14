package com.ksv.tactive;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ksv.tactive.user.dao.UserDao;
import com.ksv.tactive.user.entity.User;
@SpringBootApplication
public class TactiveTenderingApplication implements CommandLineRunner{
	public static void main(String[] args) {
		SpringApplication.run(TactiveTenderingApplication.class, args);
	}
	@Autowired
	private UserDao userDao;
	@Autowired
	private BCryptPasswordEncoder encoder;
	@Override
	public void run(String... args) throws Exception {
		if(userDao.count()==0) {
			User user = new User();
			user.setId(UUID.randomUUID().toString());
			user.setEmail("admin@gmail.com");
			user.setPassword(encoder.encode("admin@123"));
			user.setName("admin");
			user.setUsername(user.getEmail());
			user.setPhoneNumber(9876543210L);
			user.setRole("admin");
			userDao.save(user);
			user.setId(UUID.randomUUID().toString());
			user.setEmail("user@gmail.com");
			user.setPassword(encoder.encode("user@123"));
			user.setName("user");
			user.setUsername(user.getEmail());
			user.setPhoneNumber(9876543210L);
			user.setRole("user");
			userDao.save(user);
		}
	}
	
	
}

