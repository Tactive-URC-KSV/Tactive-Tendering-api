package com.ksv.tactive.controller;
import java.util.List;
import java.util.Locale;

import com.ksv.tactive.company.entity.Language;
import com.ksv.tactive.dto.CitiesDto;
import com.ksv.tactive.dto.ResourceDto;
import com.ksv.tactive.dto.StatesDto;
import com.ksv.tactive.location.entity.Country;
import com.ksv.tactive.master.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ksv.tactive.currency.entity.Currency;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.project.entity.CostCodeType;
import com.ksv.tactive.service.MasterService;
@RestController
@RequestMapping("/tactive")
public class MasterDataController {
	@Autowired
	private MasterService masterService;
	@Autowired
	private MessageSource messageSource;
	@PostMapping("/addRegion")
	public ResponseEntity<String> addRegion(@RequestBody Region region) {
		try {
			return ResponseEntity.status(HttpStatus.OK).body(masterService.addRegion(region));
		} catch (DuplicateEntryException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	@GetMapping("/regions")
	public ResponseEntity<?> getAllRegions(){
		if(masterService.getAllRegions().isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllRegions());
	}
	@PostMapping("/sector")
	public ResponseEntity<String> addSector(@RequestBody Sector sector){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addSector(sector));
	}
	@PutMapping("/sector/edit")
	public ResponseEntity<?> editSector(@RequestBody Sector sector){
		masterService.editSector(sector);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null, Locale.ENGLISH));
	}
	@PutMapping("/region/edit")
	public ResponseEntity<String> editRegion(@RequestBody Region region){
		masterService.editRegion(region);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@PostMapping("/scope")
	public ResponseEntity<String> addScope(@RequestBody ScopeOfPackage scope){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addScope(scope));
	}
	@PutMapping("/scope/edit")
	public ResponseEntity<String> editScope(@RequestBody ScopeOfPackage scope){
		masterService.editScopeOfPackage(scope);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@PostMapping("/uom")
	public ResponseEntity<String> addUOM(@RequestBody UOM uom){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addUom(uom));
	}
	@GetMapping("/uoms")
    public ResponseEntity<?> getAllUOMs() {
        List<UOM> uoms = masterService.getAllUOMs();
        if(uoms.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(uoms);
    }
	@PutMapping("/uom/edit")
	public ResponseEntity<?> editUom(@RequestBody UOM uom){
		masterService.editUom(uom);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
    @GetMapping("/sectors")
    public ResponseEntity<?> getAllSectors() {
        List<Sector> sectors = masterService.getAllSector();
        if(sectors.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(sectors);
    }
    @GetMapping("/scopes")
    public ResponseEntity<?> getAllScopes() {
        List<ScopeOfPackage> scopes = masterService.getAllScopes();
        if(scopes.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(scopes);
    }
    @GetMapping("/project-status")
	public ResponseEntity<?> getProjectStatus(){
    	List<ProjectStatus> status = masterService.getAllProjectStatus();
        if(status.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(status);
	}
    @PostMapping("/project-status/add")
    public ResponseEntity<String> addProjectStatus(@RequestBody ProjectStatus projectStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addProjectStatus(projectStatus));
    }
    @PostMapping("/approvalDocuments/add")
    public ResponseEntity<String> addListOfApprovals(@RequestBody ListOfApprovals listOfApprovals) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addListOfApprovals(listOfApprovals));
    }
	@PutMapping("/approvalDocuments/edit")
	public ResponseEntity<?> editListOfApprovals(@RequestBody ListOfApprovals listOfApprovals){
		masterService.editListOfApprovals(listOfApprovals);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
    @GetMapping("/listOfApprovals")
	public ResponseEntity<?> getAllListOfApprovals(){
    	List<ListOfApprovals> approvals = masterService.getAllListOfApprovals();
        if(approvals.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(approvals);
	}
    @PostMapping("/costCodeType/add")
    public ResponseEntity<?> addCostCodeType(@RequestBody CostCodeType costCodeType) {
    	try {
			return ResponseEntity.status(HttpStatus.OK).body(masterService.addCostCodeType(costCodeType));
		} catch (DuplicateEntryException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
    }
	@PutMapping("/costCodeType/edit")
	public ResponseEntity<?> editCostCodeType(@RequestBody CostCodeType costCodeType){
		masterService.editCostCodeType(costCodeType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
    @GetMapping("/costCodeTypes")
    public ResponseEntity<?> getAllCostCodeTypes() {
    	List<CostCodeType> costCodeType = masterService.getAllCostCodeTypes();
        if(costCodeType.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(costCodeType);
	}
    @GetMapping("/resourceType")
    public ResponseEntity<?> getAllResourceTypes() {
		List<ResourceType> resourceTypes = masterService.getAllResourceTypes();
		if(resourceTypes.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(resourceTypes);
    }
	@PostMapping("/resourceType")
	public ResponseEntity<?> addResourceTypes(@RequestBody ResourceType resourceType ) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addResourceType(resourceType));
	}
	@PutMapping("/resourceType/edit")
	public ResponseEntity<?> editResourceType(@RequestBody ResourceType resourceType){
		masterService.editResourceType(resourceType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/resources")
	public ResponseEntity<?> getAllResources() {
		List<Resources> resources = masterService.getAllResources();
		if (resources.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(resources);
	}
	@PostMapping("resource/add")
	public ResponseEntity<?> addResource(@RequestBody ResourceDto resourceDto) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(masterService.addResource(resourceDto));
	}
	@PutMapping("/resource/edit")
	public ResponseEntity<?> editResource(@RequestBody ResourceDto resourceDto) {
		masterService.editResource(resourceDto);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}

	@GetMapping("/quantityType")
	public ResponseEntity<?> getAllQuantityTypes() {
		List<QuantityType> quantityTypes = masterService.getAllQuantityTypes();
		if (quantityTypes.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(quantityTypes);
	}
	@PostMapping("/quantityType")
	public ResponseEntity<?> addQuantityType(@RequestBody QuantityType quantityType) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(masterService.addQuantityType(quantityType));
	}
	@PutMapping("/quantityType/edit")
	public ResponseEntity<?> editQuantityType(@RequestBody QuantityType quantityType) {
		masterService.editQuantityType(quantityType);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/resourceNature")
	public ResponseEntity<?> getAllResourceNatures() {
		List<ResourceNature> resourceNatures = masterService.getAllResourceNatures();
		if (resourceNatures.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(resourceNatures);
	}
	@GetMapping("/resources/{resourceTypeId}")
	public ResponseEntity<?> getAllResources(@PathVariable String resourceTypeId) {
		List<Resources> resources = masterService.getAllResources();
		if (resources.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		List<Resources> filteredResources = resources.stream()
				.filter(res -> res.getResourceType().getId().equals(resourceTypeId))
				.toList();
		return ResponseEntity.status(HttpStatus.OK).body(filteredResources);
	}
	@GetMapping("/project/currency")
	public ResponseEntity<?> getAllCurrencies(){
		List<Currency> currencies = masterService.getAllCurrencies();
		if(currencies.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(currencies);
	}
	@PostMapping("/resourceNature/add")
	public ResponseEntity<String> addResourceNature(@RequestBody ResourceNature resourceNature){
		try {
			return ResponseEntity.status(HttpStatus.OK).body(masterService.addResourceNature(resourceNature));
		}
		catch (DuplicateEntryException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(e.getMessage());
		}
	}
	@PutMapping("/resourceNature/edit")
	public ResponseEntity<?> editResourceNature(@RequestBody ResourceNature resourceNature){
		masterService.editResourceNature(resourceNature);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));

	}
	@GetMapping("/countries")
	public ResponseEntity<?> getAllCountries(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllCountries());
	}
	@GetMapping("/states")
	public ResponseEntity<?> getAllStates(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllStates());
	}
	@GetMapping("/states/{countryId}")
	public ResponseEntity<?> getStatesByCountry(@PathVariable String countryId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getStatesByCountry(countryId));
	}
	@PostMapping("/states")
	public ResponseEntity<?> addStates(@RequestBody StatesDto statesDto){
		masterService.addStates(statesDto);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/states/edit")
	public ResponseEntity<?> editStates(@RequestBody StatesDto statesDto){
		masterService.editStates(statesDto);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@GetMapping("/cities/byCountry/{countryId}")
	public ResponseEntity<?> getCitiesByCountry(@PathVariable String countryId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getCitiesByCountry(countryId));
	}
	@GetMapping("/cities")
	public ResponseEntity<?> getAllCities(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllCities());
	}
	@GetMapping("/cities/byState/{stateId}")
	public ResponseEntity<?> getCitiesByStates(@PathVariable String stateId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getCitiesByStates(stateId));
	}
	@PostMapping("/cities")
	public ResponseEntity<?> addCities(@RequestBody CitiesDto citiesDto){
		masterService.addCities(citiesDto);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/cities/edit")
	public ResponseEntity<?> editCities(@RequestBody CitiesDto citiesDto){
		masterService.editCities(citiesDto);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@PostMapping("/addressType")
	public ResponseEntity<?> addAddressType(@RequestBody AddressType addressType){
		AddressType addressType1 = masterService.addAddressType(addressType);
		if(addressType1 != null){
			return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("addressType.save.success", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("addressType.save.failure", null, Locale.ENGLISH));
	}
	@GetMapping("/addressType")
	public ResponseEntity<?> getAllAddressTypes(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllAddressTypes());
	}
	@PutMapping("/addressType/edit")
	public ResponseEntity<?> editAddressType(@RequestBody AddressType addressType){
		AddressType savedAddressType = masterService.editAddressType(addressType);
		if(savedAddressType != null){
			return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("addressType.edit.success", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(messageSource.getMessage("addressType.edit.failure", null, Locale.ENGLISH));
	}
	@PostMapping("/country")
	public ResponseEntity<?> addCountry(@RequestBody Country country) {
		masterService.addCountry(country);
		return ResponseEntity.ok(
				messageSource.getMessage("country.save.success", null, Locale.ENGLISH)
		);
	}
	@PutMapping("/country/edit")
	public ResponseEntity<?> editCountry(@RequestBody Country country) {
		masterService.editCountry(country);
		return ResponseEntity.ok(
				messageSource.getMessage("country.edit.success", null, Locale.ENGLISH)
		);
	}
	@PostMapping("/taxType/add")
	public ResponseEntity<?> addTaxType(@RequestBody TaxType taxType){
		masterService.addTaxType(taxType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/taxType/edit")
	public ResponseEntity<?> editTaxType(@RequestBody TaxType taxType){
		masterService.editTaxType(taxType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@GetMapping("/taxType")
	public ResponseEntity<?> getAllTaxType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllTaxType());
	}
	@PostMapping("/territoryType/add")
	public ResponseEntity<?> addTerritoryType(@RequestBody TerritoryType territoryType){
		masterService.addTerritoryType(territoryType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/territoryType/edit")
	public ResponseEntity<?> editTaxType(@RequestBody TerritoryType territoryType){
		masterService.editTerritoryType(territoryType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@GetMapping("/territoryType")
	public ResponseEntity<?> getAllTerritoryType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllTerritoryType());
	}
	@PostMapping("/identityType/add")
	public ResponseEntity<?> addIdentityType(@RequestBody IdentityType identityType){
		masterService.addIdentityType(identityType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/identityType/edit")
	public ResponseEntity<?> editIdentityType(@RequestBody IdentityType identityType){
		masterService.editIdentityType(identityType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@GetMapping("/identityType")
	public ResponseEntity<?> getAllIdentityType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllIdentityType());
	}
	@PostMapping("/currency/add")
	public ResponseEntity<?> addCurrency(@RequestBody Currency currency){
		masterService.addCurrency(currency);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/currency/edit")
	public ResponseEntity<?> editCurrency(@RequestBody Currency currency){
		masterService.editCurrency(currency);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@PostMapping("/companyType/add")
	public ResponseEntity<?> addCompanyType(@RequestBody CompanyType companyType){
		masterService.addCompanyType(companyType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.save.success",null,Locale.ENGLISH));
	}
	@PutMapping("/companyType/edit")
	public ResponseEntity<?> editCompanyType(@RequestBody CompanyType companyType){
		masterService.editCompanyType(companyType);
		return ResponseEntity.status(HttpStatus.OK).body(messageSource.getMessage("data.updated",null,Locale.ENGLISH));
	}
	@GetMapping("/companyType")
	public ResponseEntity<?> getAllCompanyType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllCompanyTypes());
	}
	@PostMapping("/companyLevel/add")
	public ResponseEntity<?> addCompanyLevel(@RequestBody CompanyLevel companyLevel) {
		masterService.addCompanyLevel(companyLevel);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.save.success", null, Locale.ENGLISH));
	}
	@PutMapping("/companyLevel/edit")
	public ResponseEntity<?> editCompanyLevel(@RequestBody CompanyLevel companyLevel) {
		masterService.editCompanyLevel(companyLevel);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}

	@GetMapping("/companyLevel")
	public ResponseEntity<?> getAllCompanyLevel() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllCompanyLevel());
	}
	@PostMapping("/companyStatus/add")
	public ResponseEntity<?> addCompanyStatus(@RequestBody CompanyStatus companyStatus) {
		masterService.addCompanyStatus(companyStatus);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.save.success", null, Locale.ENGLISH));
	}
	@PutMapping("/companyStatus/edit")
	public ResponseEntity<?> editCompanyStatus(@RequestBody CompanyStatus companyStatus) {
		masterService.editCompanyStatus(companyStatus);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/companyStatus")
	public ResponseEntity<?> getAllCompanyStatus() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllCompanyStatus());
	}
	@PostMapping("/companyConstitution/add")
	public ResponseEntity<?> addCompanyConstitution(@RequestBody CompanyConstitution companyConstitution) {
		masterService.addCompanyConstitution(companyConstitution);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.save.success", null, Locale.ENGLISH));
	}
	@PutMapping("/companyConstitution/edit")
	public ResponseEntity<?> editCompanyConstitution(@RequestBody CompanyConstitution companyConstitution) {
		masterService.editCompanyConstitution(companyConstitution);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/companyConstitution")
	public ResponseEntity<?> getAllCompanyConstitution() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllCompanyConstitution());
	}
	@PostMapping("/companyNature/add")
	public ResponseEntity<?> addCompanyNature(@RequestBody CompanyNature companyNature) {
		masterService.addCompanyNature(companyNature);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.save.success", null, Locale.ENGLISH));
	}
	@PutMapping("/companyNature/edit")
	public ResponseEntity<?> editCompanyNature(@RequestBody CompanyNature companyNature) {
		masterService.editCompanyNature(companyNature);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/companyNature")
	public ResponseEntity<?> getAllCompanyNature() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllCompanyNature());
	}
	@PostMapping("/language/add")
	public ResponseEntity<?> addLanguage(@RequestBody Language language) {
		masterService.addLanguage(language);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.save.success", null, Locale.ENGLISH));
	}
	@PutMapping("/language/edit")
	public ResponseEntity<?> editLanguage(@RequestBody Language language) {
		masterService.editLanguage(language);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/language")
	public ResponseEntity<?> getAllLanguage() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllLanguage());
	}
	@PostMapping("/businessNature/add")
	public ResponseEntity<?> addBusinessNature(@RequestBody BusinessNature businessNature) {
		masterService.addBusinessNature(businessNature);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.save.success", null, Locale.ENGLISH));
	}
	@PutMapping("/businessNature/edit")
	public ResponseEntity<?> editBusinessNature(@RequestBody BusinessNature businessNature) {
		masterService.editBusinessNature(businessNature);
		return ResponseEntity.status(HttpStatus.OK)
				.body(messageSource.getMessage("data.updated", null, Locale.ENGLISH));
	}
	@GetMapping("/businessNature")
	public ResponseEntity<?> getAllBusinessNature() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllBusinessNature());
	}
}
