package com.ksv.tactive.user.dao;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.user.entity.User;
@Repository
public interface UserDao extends JpaRepository<User, UUID>{
	Optional<User> findByUsername(String username);
    User findByEmail(String email);
}
