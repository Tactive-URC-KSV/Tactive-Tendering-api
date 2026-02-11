package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.ResourceAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceAttributeDao extends JpaRepository<ResourceAttribute, String> {
}
