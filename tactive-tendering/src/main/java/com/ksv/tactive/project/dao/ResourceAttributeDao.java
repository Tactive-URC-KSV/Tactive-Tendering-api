package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ResourceAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceAttributeDao extends JpaRepository<ResourceAttribute, UUID> {
    Optional<ResourceAttribute> findByResourcesIdAndAttributeGroupId(UUID resourceId, UUID attributeGroupId);
    List<ResourceAttribute> findByResourcesId(UUID resourceId);
}
