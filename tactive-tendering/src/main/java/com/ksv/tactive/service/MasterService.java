package com.ksv.tactive.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ksv.tactive.project.entity.CostCodeType;
import com.ksv.tactive.project.entity.ListOfApprovals;
import com.ksv.tactive.project.entity.ProjectStatus;
import com.ksv.tactive.project.entity.Region;
import com.ksv.tactive.project.entity.ScopeOfPackage;
import com.ksv.tactive.project.entity.Sector;
import com.ksv.tactive.project.entity.UOM;

@Service
public interface MasterService {
	
	String addRegion(Region region);
	List<Region> getAllRegions();
	
	String addSector(Sector sector);
	List<Sector> getAllSector();
	
	String addScope(ScopeOfPackage scope);
	List<ScopeOfPackage> getAllScopes();
	
	String addUom(UOM uom);
	List<UOM> getAllUOMs();
	UOM findUomByCode(String uomCode);
	
	List<ProjectStatus> getAllProjectStatus();
	String addProjectStatus(ProjectStatus projectStatus);
	
	String addListOfApprovals(ListOfApprovals listOfApprovals);
	List<ListOfApprovals> getAllListOfApprovals();
	

	String addCostCodeType(CostCodeType costCodeType);
	List<CostCodeType> getAllCostCodeTypes();

}
