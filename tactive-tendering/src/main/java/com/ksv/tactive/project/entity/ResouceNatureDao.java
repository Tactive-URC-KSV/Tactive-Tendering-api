package com.ksv.tactive.project.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResouceNatureDao extends JpaRepository<ResourceNature, String>{

}
