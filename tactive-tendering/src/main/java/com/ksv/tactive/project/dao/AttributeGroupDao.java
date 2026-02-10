package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeGroupDao extends JpaRepository<AttributeGroup, String> {
    boolean existsByGroupName(String groupName);
}
