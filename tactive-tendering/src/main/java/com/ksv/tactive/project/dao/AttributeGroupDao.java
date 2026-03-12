package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttributeGroupDao extends JpaRepository<AttributeGroup, UUID> {
    Optional<AttributeGroup> findByGroupName(String groupName);
}
