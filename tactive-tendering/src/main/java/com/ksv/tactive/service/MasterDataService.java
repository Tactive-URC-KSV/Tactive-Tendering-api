package com.ksv.tactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ksv.tactive.entity.ScopeOfPackage;
import com.ksv.tactive.entity.Sector;
import com.ksv.tactive.entity.UOM;

@Service
public interface MasterDataService {
	String addSector(Sector sector);
	String addScope(ScopeOfPackage scope);
	String addUom(UOM uom);
	List<UOM> getAllUOMs();
	List<Sector> getAllSector();
	List<ScopeOfPackage> getAllScopes();
	
}
