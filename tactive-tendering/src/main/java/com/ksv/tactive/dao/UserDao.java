package com.ksv.tactive.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.entity.User;

@Repository
public interface UserDao extends JpaRepository<User, String>{
	
	Optional<User> findByUsername(String username);
}
