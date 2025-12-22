package com.ksv.tactive.controller;
import java.util.List;
import java.util.Locale;

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
	@PostMapping("/sector/addSector")
	public ResponseEntity<String> addSector(@RequestBody Sector sector){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addSector(sector));
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
	@PostMapping("/uom/addUom")
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
	@GetMapping("/resources")
	public ResponseEntity<?> getAllResources() {
		List<Resources> resources = masterService.getAllResources();
		if (resources.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(messageSource.getMessage("data.not.found", null, Locale.ENGLISH));
		}
		return ResponseEntity.status(HttpStatus.OK).body(resources);
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
	@GetMapping("/countries")
	public ResponseEntity<?> getAllCountries(){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getAllCountries());
	}
	@GetMapping("/cities/{countryId}")
	public ResponseEntity<?> getCitiesByCountry(@PathVariable String countryId){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.getCitiesByCountry(countryId));
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
}
