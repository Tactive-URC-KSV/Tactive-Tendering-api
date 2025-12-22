package com.ksv.tactive.location.dao;
import com.ksv.tactive.location.entity.States;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface StatesDao extends JpaRepository<States, String> {

}
