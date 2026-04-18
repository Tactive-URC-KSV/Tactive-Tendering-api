package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.Attribute;
import com.ksv.tactive.project.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Repository
public interface AttributeDao extends JpaRepository<Attribute, UUID> {
    List<Attribute> findByAttributeGroup(AttributeGroup savedAttributeGroup);
    Optional<Attribute> findByAttributeName(String attributeName);
    List<Attribute> findByAttributeGroupId(UUID attributeGroupId);
    Optional<Attribute> findByAttributeCode(long attributeCode);
}
