package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ResourceType;

@Repository
public interface ResourceTypeDao extends JpaRepository<ResourceType, String>{

	ResourceType findByResourceTypeNameIgnoreCase(String resouceTypeName);

}
