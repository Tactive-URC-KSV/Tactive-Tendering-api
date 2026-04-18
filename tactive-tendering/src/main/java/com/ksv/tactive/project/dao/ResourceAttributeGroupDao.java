package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ResourceAttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceAttributeGroupDao extends JpaRepository<ResourceAttributeGroup, UUID> {
    Optional<ResourceAttributeGroup> findByResourceIdAndAttributeGroupId(UUID resourceId, UUID attributeGroupId);
    List<ResourceAttributeGroup> findByResourceId(UUID resourceId);
    @Query("SELECT DISTINCT r FROM ResourceAttributeGroup r LEFT JOIN FETCH r.attributes")
    List<ResourceAttributeGroup> findAllWithAttributes();
}
