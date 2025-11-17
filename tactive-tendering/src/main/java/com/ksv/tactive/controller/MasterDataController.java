package com.ksv.tactive.controller;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.project.entity.CostCodeType;
import com.ksv.tactive.project.entity.Currency;
import com.ksv.tactive.project.entity.ListOfApprovals;
import com.ksv.tactive.project.entity.ProjectStatus;
import com.ksv.tactive.project.entity.QuantityType;
import com.ksv.tactive.project.entity.Region;
import com.ksv.tactive.project.entity.Resources;
import com.ksv.tactive.project.entity.ResourceNature;
import com.ksv.tactive.project.entity.ResourceType;
import com.ksv.tactive.project.entity.ScopeOfPackage;
import com.ksv.tactive.project.entity.Sector;
import com.ksv.tactive.project.entity.UOM;
import com.ksv.tactive.service.MasterService;
@RestController
@RequestMapping("/tactive")
public class MasterDataController {
	@Autowired
	private MasterService masterService;
	@Autowired
	private MessageSource messageSource;
	@PostMapping("/regions/addRegion")
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
	@PostMapping("/scope/addScope")
	public ResponseEntity<String> addScope(@RequestBody ScopeOfPackage scope){
		return ResponseEntity.status(HttpStatus.OK).body(masterService.addScope(scope));
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
}
