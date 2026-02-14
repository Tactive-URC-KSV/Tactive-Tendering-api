package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Attribute;
import com.ksv.tactive.project.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeDao extends JpaRepository<Attribute, String> {
    List<Attribute> findByAttributeGroup(AttributeGroup savedAttributeGroup);
    Optional<Attribute> findByAttributeName(String attributeName);
    List<Attribute> findByAttributeGroupId(String attributeGroupId);
}
