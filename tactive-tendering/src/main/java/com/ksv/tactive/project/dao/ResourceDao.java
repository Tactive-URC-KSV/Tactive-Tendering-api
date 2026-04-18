package com.ksv.tactive.project.dao;
import java.util.List;
import java.util.UUID;

import com.ksv.tactive.util.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Resources;
@Repository
public interface ResourceDao extends JpaRepository<Resources, UUID>{
    Page<Resources> findByResourceType(ResourceType type, Pageable pageable);
    Page<Resources> findByResourceNameContainingIgnoreCaseOrResourceCodeContainingIgnoreCase(String search, String search1, Pageable pageable);
    Page<Resources> findByResourceTypeAndResourceNameContainingIgnoreCaseOrResourceTypeAndResourceCodeContainingIgnoreCase(ResourceType type, String search, ResourceType type1, String search1, Pageable pageable);
}
