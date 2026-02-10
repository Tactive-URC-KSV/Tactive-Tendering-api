package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeDao extends JpaRepository<Attribute, String> {
    boolean existsByAttributeName(String attributeName);
}
