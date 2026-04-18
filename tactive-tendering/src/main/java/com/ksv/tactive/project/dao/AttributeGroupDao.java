package com.ksv.tactive.project.dao;

import com.ksv.tactive.project.entity.AttributeGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Repository
public interface AttributeGroupDao extends JpaRepository<AttributeGroup, UUID> {
    Optional<AttributeGroup> findByGroupName(String groupName);
    Optional<AttributeGroup> findByGroupCode(long groupCode);
    @Query("""
    SELECT g FROM AttributeGroup g
    WHERE 
        LOWER(g.groupName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR g.groupCode = :code
""")
    Page<AttributeGroup> searchAttributeGroups(
            @Param("search") String search,
            @Param("code") Long code,
            Pageable pageable
    );
}
