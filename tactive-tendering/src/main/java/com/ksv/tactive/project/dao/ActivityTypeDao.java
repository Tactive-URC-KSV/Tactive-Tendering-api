package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ActivityType;

@Repository
public interface ActivityTypeDao extends JpaRepository<ActivityType, String>{

}
