package com.ksv.tactive.project.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ksv.tactive.project.entity.Sector;

@Repository
public interface SectorDao extends JpaRepository<Sector, String>{

}
