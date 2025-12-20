package com.ksv.tactive.service;
import java.util.List;

import com.ksv.tactive.master.entity.*;
import org.springframework.stereotype.Service;

import com.ksv.tactive.currency.entity.Currency;
import com.ksv.tactive.location.entity.Cities;
import com.ksv.tactive.location.entity.Country;
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
	AddressType findAddressTypeById(String addressTypeId);
	Country findCountryById(String countryId);
	Cities findCityById(String cityId);
	IdentityType findIdentityTypeById(String identityId);
	TaxType findTaxTypeById(String taxTypeId);
	TerritoryType findterritoryTypeId(String territoryTypeId);
	
	
	
	
	
	
}
