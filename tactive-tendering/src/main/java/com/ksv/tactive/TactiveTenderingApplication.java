package com.ksv.tactive;
import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

	@Value("${default.admin.email:admin@gmail.com}")
	private String adminEmail;

	@Value("${default.admin.username:admin}")
	private String adminUsername;

	@Value("${default.admin.password:admin@123}")
	private String adminPassword;

	@Value("${default.user.email:user@gmail.com}")
	private String userEmail;

	@Value("${default.user.username:user}")
	private String userUsername;

	@Value("${default.user.password:user@123}")
	private String userPassword;

	@Override
	public void run(String... args) throws Exception {
		if(userDao.count()==0) {
			User admin = new User();
			admin.setEmail(adminEmail);
			admin.setPassword(encoder.encode(adminPassword));
			admin.setName(adminUsername);
			admin.setUsername(adminUsername);
			admin.setPhoneNumber(9876543210L);
			admin.setRole("admin");
			userDao.save(admin);

			User user = new User();
			user.setEmail(userEmail);
			user.setPassword(encoder.encode(userPassword));
			user.setName(userUsername);
			user.setUsername(userUsername);
			user.setPhoneNumber(9876543210L);
			user.setRole("user");
			userDao.save(user);
		}
	}
	@Bean
	SecureRandom getSecureRandom(){
		return new SecureRandom();
	}
	
	
}

