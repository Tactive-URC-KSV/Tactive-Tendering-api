package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ResourceAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceAttributeDao extends JpaRepository<ResourceAttribute, String> {
    Optional<ResourceAttribute> findByResourcesIdAndAttributeGroupId(String resourceId, String attributeGroupId);
    List<ResourceAttribute> findByResourcesId(String resourceId);
}
