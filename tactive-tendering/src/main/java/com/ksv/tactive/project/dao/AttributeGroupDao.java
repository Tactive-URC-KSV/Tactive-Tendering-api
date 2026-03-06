package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeGroupDao extends JpaRepository<AttributeGroup, String> {
    Optional<AttributeGroup> findByGroupName(String groupName);
}
