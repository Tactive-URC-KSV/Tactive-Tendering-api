package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ksv.tactive.project.entity.ActivityGroup;

import java.util.Optional;

@Repository
public interface ActivityGroupDao extends JpaRepository<ActivityGroup, String>{
	ActivityGroup findByActivityName(String activityGroup);
	ActivityGroup findByActivityNameIgnoreCase(String activityName);
    Optional<ActivityGroup> findByActivityCodeIgnoreCaseAndActiveTrue(String activityCode);
}
