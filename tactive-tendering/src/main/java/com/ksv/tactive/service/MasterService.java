package com.ksv.tactive.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ksv.tactive.currency.entity.Currency;
import com.ksv.tactive.location.entity.Cities;
import com.ksv.tactive.location.entity.Country;
import com.ksv.tactive.master.entity.ListOfApprovals;
import com.ksv.tactive.master.entity.ProjectStatus;
import com.ksv.tactive.master.entity.QuantityType;
import com.ksv.tactive.master.entity.Region;
import com.ksv.tactive.master.entity.ResourceNature;
import com.ksv.tactive.master.entity.ResourceType;
import com.ksv.tactive.master.entity.Resources;
import com.ksv.tactive.master.entity.ScopeOfPackage;
import com.ksv.tactive.master.entity.Sector;
import com.ksv.tactive.master.entity.UOM;
import com.ksv.tactive.project.entity.CostCodeType;
@Service
public interface MasterService {
	String addRegion(Region region);
	List<Region> getAllRegions();
	Region findRegionById(String regionId);
	String addSector(Sector sector);
	List<Sector> getAllSector();
	Sector findSectorById(String sectorId);
	String addScope(ScopeOfPackage scope);
	List<ScopeOfPackage> getAllScopes();
	String addUom(UOM uom);
	UOM findUomById(String uomId);
	List<UOM> getAllUOMs();
	UOM findUomByCode(String uomCode);
	List<ProjectStatus> getAllProjectStatus();
	ProjectStatus findStatusById(String projectStatusId);
	ProjectStatus findStatusByProcessUrl(String requestURI);
	String addProjectStatus(ProjectStatus projectStatus);
	String addListOfApprovals(ListOfApprovals listOfApprovals);
	List<ListOfApprovals> getAllListOfApprovals();
	String addCostCodeType(CostCodeType costCodeType);
	List<CostCodeType> getAllCostCodeTypes();
	List<ResourceType> getAllResourceTypes();
	ResourceType findResourceTypeById(String resourceTypeId);
	List<Resources> getAllResources();
	List<QuantityType> getAllQuantityTypes();
	List<ResourceNature> getAllResourceNatures();
	ResourceNature findResourceNatureById(String resourceNatureId);
	List<Currency> getAllCurrencies();
	String addResourceNature(ResourceNature resourceNature);
	ScopeOfPackage findScopeOfPackageById(String id);
	Resources findResourceById(String resourceId);
	QuantityType findQuantityTypeById(String quantityTypeId);
	Currency findCurrencyById(String currencyId);
	List<Country> getAllCountries();
	List<Cities> getCitiesByCountry(String countryId);
	
	
	
	
	
	
}
