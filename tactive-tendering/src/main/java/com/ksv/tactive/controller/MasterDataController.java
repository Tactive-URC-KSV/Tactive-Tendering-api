package com.ksv.tactive.controller;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.entity.Language;
import com.ksv.tactive.project.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ksv.tactive.project.entity.Currency;
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
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addRegion(region));
	}
	@GetMapping("/regions")
	public ResponseEntity<?> getAllRegions(){
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
	@PostMapping("/addUom")
	public ResponseEntity<String> addUOM(@RequestBody Uom uom){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addUom(uom));
	}
	@PostMapping("/uom/conversion/add")
	public ResponseEntity<String> addConversion(@RequestBody UomConversionDto uomConversionDto){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addUomConversion(uomConversionDto));
	}
	@GetMapping("/uoms")
    public ResponseEntity<?> getAllUOMs() {
        List<Uom> uoms = masterService.getAllUOMs();
        if(uoms.isEmpty()) {
        	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
        }
        return ResponseEntity.status(HttpStatus.OK).body(uoms);
    }
	@GetMapping("/uom/conversion/{baseUomId}")
	public ResponseEntity<?> getAllConversion(@PathVariable String baseUomId) {
		return  ResponseEntity.status(HttpStatus.OK).body(masterService.getUomConversionByBaseUom(baseUomId));
	}
	@PutMapping("/uom/edit")
	public ResponseEntity<?> editUom(@RequestBody Uom uom){
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
	@GetMapping("/fieldType")
	public ResponseEntity<?> getAllFieldTypes() {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllFieldType());
	}
	@GetMapping("/fieldSection")
	public ResponseEntity<?> getAllFieldSection() {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllFieldSection());
	}
	@PostMapping("/sector/fields")
	public ResponseEntity<?> addSectorFields(@RequestBody SectorFieldsDto sectorFieldsDto){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addSectorFields(sectorFieldsDto));
	}
	@GetMapping("/sector/fields/{sectorId}")
	public ResponseEntity<?> getFieldsBySectorId(@PathVariable String sectorId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getFieldsBySector(sectorId));
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
    @PostMapping("/costCodeType/add")
    public ResponseEntity<?> addCostCodeType(@RequestBody CostCodeType costCodeType) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addCostCodeType(costCodeType));
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
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllResourceTypes());
    }
	@GetMapping("/resources")
	public ResponseEntity<?> getAllResources(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size,
			@RequestParam(required = false) String resourceType) {
		Page<ResourceDto> resourcePage = masterService.getAllResources(page, size, resourceType);
		if (resourcePage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.ok(resourcePage);
	}
	@GetMapping("/resources/search")
	public ResponseEntity<?> searchResources(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String resourceType,
			@RequestParam(required = false) String resourceName,
			@RequestParam(required = false) String resourceCode) {
		Page<ResourceDto> resourcePage =
				masterService.searchResources(page, size, search, resourceType, resourceName, resourceCode);
		if (resourcePage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.ok(resourcePage);
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
		List<EnumDto> quantityTypes = masterService.getAllQuantityTypes();
		if (quantityTypes.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(quantityTypes);
	}

	@GetMapping("/resourceNature")
	public ResponseEntity<?> getAllResourceNatures() {
		List<EnumDto> resourceNatures = masterService.getAllResourceNatures();
		if (resourceNatures.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(resourceNatures);
	}
	@GetMapping("/project/currency")
	public ResponseEntity<?> getAllCurrencies(){
		List<Currency> currencies = masterService.getAllCurrencies();
		if(currencies.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(currencies);
	}
	@GetMapping("/countries")
	public ResponseEntity<?> getAllCountries(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllCountries());
	}
	@GetMapping("/states/{countryId}")
	public ResponseEntity<?> getStatesByCountry(@PathVariable String countryId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getStatesByCountry(countryId));
	}
	@GetMapping("/cities/byState/{stateId}")
	public ResponseEntity<?> getCitiesByStates(@PathVariable String stateId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getCitiesByStates(stateId));
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
	@GetMapping("/territoryType")
	public ResponseEntity<?> getAllTerritoryType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllTerritoryType());
	}
	@GetMapping("/taxType")
	public ResponseEntity<?> getAllTaxType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllTaxType());
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
	@PostMapping("/currency-conversion")
	public ResponseEntity<String> addCurrencyConversion(
			@RequestParam String baseCurrencyId,
			@RequestParam String conversionCurrencyId,
			@RequestParam double rate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {
		return ResponseEntity.ok(
				masterService.addCurrencyConversion(
						baseCurrencyId,
						conversionCurrencyId,
						rate,
						effectiveDate
				)
		);
	}
	@GetMapping("/currency-conversion/base")
	public ResponseEntity<List<CurrencyConversion>> getLatestByBaseCurrency(
			@RequestParam String baseCurrencyId) {
		return ResponseEntity.ok(
				masterService.getLatestByBaseCurrency(baseCurrencyId)
		);
	}
	@GetMapping("/companyType")
	public ResponseEntity<?> getAllCompanyType(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllCompanyTypes());
	}
	@GetMapping("/companyLevel")
	public ResponseEntity<?> getAllCompanyLevel() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllCompanyLevel());
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
	@PostMapping("/designation")
	public ResponseEntity<?> addDesignation(@RequestBody Designation designation) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addDesignation(designation));
	}
	@GetMapping("/designation")
	public ResponseEntity<?> getAllDesignation() {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getDesignation());
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
	@GetMapping("/directorType")
	public ResponseEntity<?> getAllDirectorType(){
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllDirectorType());
	}
	@GetMapping("/offerSubmissionMode")
	public ResponseEntity<?> getAllOfferSubmissionMode(){
		return ResponseEntity.status(HttpStatus.OK)
				.body(masterService.getAllOfferSubmissionMode());
	}
	@PostMapping("/attribute")
	public ResponseEntity<?> addAttribute(@RequestBody Attribute attribute) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addAttribute(attribute));
	}
	@PostMapping("/attributeGroup")
	public ResponseEntity<?> addAttributeGroup(@RequestBody AttributeGroup attributeGroup,@RequestParam List<String> attributeIds) {
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addAttributeGroup(attributeGroup,attributeIds));
	}
	@GetMapping("/attribute/by-group/{groupId}")
	public ResponseEntity<?> getAttributesByGroup(@PathVariable String groupId) {
		return ResponseEntity.ok(masterService.getAttributesByGroup(groupId));
	}
	@GetMapping("/attributeGroup")
	public ResponseEntity<?> getAttributeGroups(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size) {
		return ResponseEntity.ok(masterService.getAttributeGroups(page, size));
	}
	@GetMapping("/attributeGroup/search")
	public ResponseEntity<?> searchAttributeGroups(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size,
			@RequestParam String search) {
		Page<AttributeGroupDTO> groupPage =
				masterService.searchAttributeGroups(page, size, search);
		if (groupPage.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body("No data found");
		}
		return ResponseEntity.ok(groupPage);
	}
	@PostMapping("/addResourceAttribute")
	public ResponseEntity<String> addResourceAttribute(@RequestBody ResourceAttributeDto resourceAttributeDto) {
		String response = masterService.addResourceAttribute(resourceAttributeDto);
		return ResponseEntity.ok(response);
	}
	@GetMapping("/get-by-resource/{resourceId}")
	public ResponseEntity<List<ResourceAttributeGroup>> getByResourceId(@PathVariable String resourceId) {
		return ResponseEntity.ok(masterService.getByResourceId(resourceId));
	}
}
