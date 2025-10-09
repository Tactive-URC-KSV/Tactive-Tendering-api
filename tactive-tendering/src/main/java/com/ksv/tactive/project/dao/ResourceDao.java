package com.ksv.tactive.project.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Resources;

@Repository
public interface ResourceDao extends JpaRepository<Resources, String>{

	List<Resources> findByResourceNameIgnoreCase(String resourceName);

	
}
