package com.ksv.tactive.project.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.ResourceNature;

import java.util.Optional;

@Repository
public interface ResourceNatureDao extends JpaRepository<ResourceNature, String>{
	ResourceNature findByNatureIgnoreCase(String nature);
	Optional<ResourceNature> findByNatureIgnoreCaseAndActiveTrue(String nature);
}
