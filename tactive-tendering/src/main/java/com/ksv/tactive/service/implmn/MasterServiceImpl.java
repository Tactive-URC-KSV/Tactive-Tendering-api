package com.ksv.tactive.service.implmn;
import java.util.*;
import java.util.stream.Collectors;

import com.ksv.tactive.dataloader.LocationMemoryEngine;
import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.dao.LanguageDao;
import com.ksv.tactive.project.entity.Language;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.dto.States;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.ksv.tactive.project.dao.CurrencyDao;
import com.ksv.tactive.project.entity.Currency;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.dto.Cities;
import com.ksv.tactive.dto.Country;
import com.ksv.tactive.service.MasterService;
@Service
public class MasterServiceImpl implements MasterService{

    @Autowired
    private LocationMemoryEngine memoryEngine;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private ListOfApprovalsDao listOfApprovalsDao;
	@Autowired
	private RegionDao regionRepo;
	@Autowired
	private UomDao uomDao;
	@Autowired
	private SectorDao sectorDao;
	@Autowired
	private ScopeOfPackDao scopeOfPackDao;
	@Autowired
	private ProjectStatusDao statusDao;
	@Autowired
	private CostCodeTypeDao costCodeTypeDao;
	@Autowired
	private ResourceTypeDao resourceTypeDao;
	@Autowired
	private ResourceDao resourceDao;
	@Autowired
	private ResourceNatureDao resouceNatureDao;
	@Autowired
	private CurrencyDao currencyDao;
	@Autowired
	private AddressTypeDao addressTypeDao;
	@Autowired
	private IdentityTypeDao identityTypeDao;
	@Autowired
	private TaxTypeDao taxTypeDao;
	@Autowired
	private CompanyLevelDao companyLevelDao;
	@Autowired
	private CompanyStatusDao companyStatusDao;
	@Autowired
	private CompanyNatureDao companyNatureDao;
	@Autowired
	private CompanyConstitutionDao companyConstitutionDao;
	@Autowired
	private LanguageDao languageDao;
	@Autowired
	private BusinessNatureDao businessNatureDao;
	@Autowired
	private AttributeDao attributeDao;
	@Autowired
	private AttributeGroupDao attributeGroupDao;
	
	@Override
	public String addSector(Sector sector) {
		sector.setId(UUID.randomUUID().toString());
		sectorDao.save(sector);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public void editSector(Sector sector) {
		Sector existingSector = sectorDao.findById(sector.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (sector.isActive() &&
				!existingSector.getSectorName().equalsIgnoreCase(sector.getSectorName())
		) {
			sectorDao
					.findBySectorNameIgnoreCaseAndActiveTrue(sector.getSectorName())
					.ifPresent(s -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingSector.setSectorName(sector.getSectorName());
		existingSector.setActive(sector.isActive());
		sectorDao.save(existingSector);
	}
	@Override
	public String addScope(ScopeOfPackage scope) {
		scope.setId(UUID.randomUUID().toString());
		scopeOfPackDao.save(scope);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public void editScopeOfPackage(ScopeOfPackage scope) {
		ScopeOfPackage existingScope = scopeOfPackDao.findById(scope.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (scope.isActive() &&
						!existingScope.getScope().equalsIgnoreCase(scope.getScope())
		) {
			scopeOfPackDao
					.findByScopeIgnoreCaseAndActiveTrue(scope.getScope())
					.ifPresent(s -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingScope.setScope(scope.getScope());
		existingScope.setActive(scope.isActive());
		scopeOfPackDao.save(existingScope);
	}
	@Override
	public String addUom(Uom uom) {
		uom.setId(UUID.randomUUID().toString());
		uomDao.save(uom);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public void editUom(Uom uom) {
		Uom existingUom = uomDao.findById(uom.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (uom.isActive() &&
				!existingUom.getUomName().equalsIgnoreCase(uom.getUomName())
		) {
			uomDao.findByUomNameIgnoreCaseAndActiveTrue(uom.getUomName())
					.ifPresent(u -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		if (uom.isActive() &&
				!existingUom.getUomCode().equalsIgnoreCase(uom.getUomCode())
		) {
			uomDao.findByUomCodeIgnoreCaseAndActiveTrue(uom.getUomCode())
					.ifPresent(u -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingUom.setUomName(uom.getUomName());
		existingUom.setUomCode(uom.getUomCode());
		existingUom.setActive(uom.isActive());
		uomDao.save(existingUom);
	}
	@Override
	public List<Region> getAllRegions() {
		return regionRepo.findAll();
	}
	@Override
	public Region findRegionById(String regionId) {
		return regionRepo.findById(regionId).orElse(null);
	}
	@Override
	public void editRegion(Region region) {
		Region existingRegion = regionRepo.findById(region.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (
				region.isActive() &&
						!existingRegion.getRegionName().equalsIgnoreCase(region.getRegionName())
		) {
			regionRepo
					.findByRegionNameIgnoreCaseAndActiveTrue(region.getRegionName())
					.ifPresent(r -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingRegion.setRegionName(region.getRegionName());
		existingRegion.setActive(region.isActive());
		regionRepo.save(existingRegion);
	}
	@Override
	public String addRegion(Region region) {
		if(regionRepo.findByRegionName(region.getRegionName()) != null) {
			throw new DuplicateEntryException(messageSource.getMessage("region.duplicate", null, Locale.ENGLISH));
		}
		region.setId(UUID.randomUUID().toString());
		regionRepo.save(region);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public List<Uom> getAllUOMs() {
		return uomDao.findAll();
	}
	@Override
	public Uom findUomByCode(String uomCode) {
		Uom uom = uomDao.findByUomCode(uomCode);
		return uom;
	}
	@Override
	public Uom findUomById(String uomId) {
		Uom uom = uomDao.findById(uomId).orElse(null);
		return uom;
	}
	@Override
	public List<Sector> getAllSector() {
		return sectorDao.findAll();
	}
	@Override
	public Sector findSectorById(String sectorId) {
		return sectorDao.findById(sectorId).orElse(null);
	}
	@Override
	public List<ScopeOfPackage> getAllScopes() {
		return scopeOfPackDao.findAll();
	}
	@Override
	public ScopeOfPackage findScopeOfPackageById(String id) {
		return scopeOfPackDao.findById(id).orElse(null);
	}
	@Override
	public List<ProjectStatus> getAllProjectStatus() {
		return statusDao.findAll();
	}
	@Override
	public ProjectStatus findStatusById(String projectStatusId) {
		return statusDao.findById(projectStatusId).orElse(null);
	}
	@Override
	public ProjectStatus findStatusByProcessUrl(String requestURI) {
		return statusDao.findByProcessUrl(requestURI);
	}
	@Override
	public String addProjectStatus(ProjectStatus projectStatus) {
		projectStatus.setId(UUID.randomUUID().toString());
		statusDao.save(projectStatus);
		return null;
	}
	@Override
	public String addListOfApprovals(ListOfApprovals listOfApprovals) {
		listOfApprovals.setId(UUID.randomUUID().toString());
		listOfApprovalsDao.save(listOfApprovals);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public void editListOfApprovals(ListOfApprovals listOfApprovals) {
		ListOfApprovals existingApproval = listOfApprovalsDao.findById(listOfApprovals.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (listOfApprovals.isActive() &&
				!existingApproval.getDocumentName()
						.equalsIgnoreCase(listOfApprovals.getDocumentName())
		) {
			listOfApprovalsDao
					.findByDocumentNameIgnoreCaseAndActiveTrue(listOfApprovals.getDocumentName())
					.ifPresent(a -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingApproval.setDocumentName(listOfApprovals.getDocumentName());
		existingApproval.setActive(listOfApprovals.isActive());
		listOfApprovalsDao.save(existingApproval);
	}
	@Override
	public List<ListOfApprovals> getAllListOfApprovals() {
		List<ListOfApprovals> listOfApprovals = listOfApprovalsDao.findAll();
		return listOfApprovals;
	}
	@Override
	public String addCostCodeType(CostCodeType costCodeType) {
		if(costCodeTypeDao.findByCostCodeName(costCodeType.getCostCodeName()) != null) {
			throw new DuplicateEntryException(messageSource.getMessage("costcode.duplicate", null, Locale.ENGLISH));
		}
		costCodeType.setId(UUID.randomUUID().toString());
		costCodeTypeDao.save(costCodeType);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public void editCostCodeType(CostCodeType costCodeType) {
		CostCodeType existingCostCodeType = costCodeTypeDao.findById(costCodeType.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (costCodeType.isActive() &&
				!existingCostCodeType.getCostCodeName()
						.equalsIgnoreCase(costCodeType.getCostCodeName())
		) {
			costCodeTypeDao
					.findByCostCodeNameIgnoreCaseAndActiveTrue(costCodeType.getCostCodeName())
					.ifPresent(c -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingCostCodeType.setCostCodeName(costCodeType.getCostCodeName());
		existingCostCodeType.setActive(costCodeType.isActive());
		costCodeTypeDao.save(existingCostCodeType);
	}
	@Override
	public List<CostCodeType> getAllCostCodeTypes() {
		return costCodeTypeDao.findAll();
	}
	@Override
	public List<ResourceType> getAllResourceTypes() {
		return resourceTypeDao.findAll();
	}
	@Override
	public String addResourceType(ResourceType resourceType) {
		resourceTypeDao
				.findByResourceTypeNameIgnoreCaseAndActiveTrue(
						resourceType.getResourceTypeName()
				)
				.ifPresent(r -> {
					throw new DuplicateEntryException(
							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
					);
				});
		resourceType.setId(UUID.randomUUID().toString());
		resourceType.setActive(true);
		resourceTypeDao.save(resourceType);
		return messageSource.getMessage(
				"data.save.success", null, Locale.ENGLISH
		);
	}
	@Override
	public void editResourceType(ResourceType resourceType) {
		ResourceType existingResourceType = resourceTypeDao.findById(resourceType.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (resourceType.isActive() &&
				!existingResourceType.getResourceTypeName()
						.equalsIgnoreCase(resourceType.getResourceTypeName())
		) {
			resourceTypeDao
					.findByResourceTypeNameIgnoreCaseAndActiveTrue(
							resourceType.getResourceTypeName()
					)
					.ifPresent(r -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingResourceType.setResourceTypeName(resourceType.getResourceTypeName());
		existingResourceType.setActive(resourceType.isActive());

		resourceTypeDao.save(existingResourceType);
	}
	@Override
	public ResourceType findResourceTypeById(String resourceTypeId) {
		return resourceTypeDao.findById(resourceTypeId).orElse(null);
	}
	@Override
	public List<Resources> getAllResources() {
		return resourceDao.findAll();
	}
	@Override
	public Resources findResourceById(String resourceId) {
		return resourceDao.findById(resourceId).orElse(null);
	}

	@Override
	public List<QuantityTypeDto> getAllQuantityTypes() {
		return Arrays.stream(QuantityType.values())
				.map(qt -> new QuantityTypeDto(qt.name(), qt.getLabel()))
				.toList();
	}
//	@Override
//	public String addQuantityType(QuantityType quantityType) {
//		quantityTypeDao
//				.findByQuantityTypeIgnoreCaseAndActiveTrue(
//						quantityType.getQuantityType()
//				)
//				.ifPresent(q -> {
//					throw new DuplicateEntryException(
//							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
//					);
//				});
//		quantityType.setId(UUID.randomUUID().toString());
//		quantityType.setActive(true);
//		quantityTypeDao.save(quantityType);
//		return messageSource.getMessage(
//				"data.save.success", null, Locale.ENGLISH
//		);
//	}
//	@Override
//	public void editQuantityType(QuantityType quantityType) {
//		QuantityType existingQuantityType = quantityTypeDao.findById(quantityType.getId())
//				.orElseThrow(() -> new DataNotFoundException(
//						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
//				));
//		if (quantityType.isActive() &&
//				!existingQuantityType.getQuantityType()
//						.equalsIgnoreCase(quantityType.getQuantityType())
//		) {
//			quantityTypeDao
//					.findByQuantityTypeIgnoreCaseAndActiveTrue(
//							quantityType.getQuantityType()
//					)
//					.ifPresent(q -> {
//						throw new DuplicateEntryException(
//								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
//						);
//					});
//		}
//		existingQuantityType.setQuantityType(quantityType.getQuantityType());
//		existingQuantityType.setActive(quantityType.isActive());
//		quantityTypeDao.save(existingQuantityType);
//	}
//	@Override
//	public QuantityType findQuantityTypeById(String quantityTypeId) {
//		return quantityTypeDao.findById(quantityTypeId).orElse(null);
//	}
	@Override
	public List<ResourceNature> getAllResourceNatures() {
		return resouceNatureDao.findAll();
	}
	@Override
	public ResourceNature findResourceNatureById(String resourceNatureId) {
		return resouceNatureDao.findById(resourceNatureId).orElse(null);
	}
	@Override
	public List<Currency> getAllCurrencies() {
		return currencyDao.findAll();
	}
	@Override
	public Currency findCurrencyById(String currencyId) {
		return currencyDao.findById(currencyId).orElse(null);
	}
	@Override
	public String addResourceNature(ResourceNature resourceNature) {
		if(resouceNatureDao.findByNatureIgnoreCase(resourceNature.getNature()) != null) {
			throw new DuplicateEntryException(messageSource.getMessage("resourcenature.duplicate", null, Locale.ENGLISH));
		}
		resourceNature.setId(UUID.randomUUID().toString());
		resouceNatureDao.save(resourceNature);
		return null;
	}

	@Override
	public void editResourceNature(ResourceNature resourceNature) {
		ResourceNature existingResourceNature = resouceNatureDao.findById(resourceNature.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (resourceNature.isActive() &&
				!existingResourceNature.getNature()
						.equalsIgnoreCase(resourceNature.getNature())
		) {
			resouceNatureDao
					.findByNatureIgnoreCaseAndActiveTrue(resourceNature.getNature())
					.ifPresent(r -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existingResourceNature.setNature(resourceNature.getNature());
		existingResourceNature.setActive(resourceNature.isActive());

		resouceNatureDao.save(existingResourceNature);
	}

	@Override
	public AddressType findAddressTypeById(String addressTypeId) {
		return addressTypeDao.findById(addressTypeId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

//	@Override
//	public Country findCountryById(String countryId) {
//		return countryDao.findById(countryId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
//	}
//
//	@Override
//	public Cities findCityById(String cityId) {
//		return citiesDao.findById(cityId)
//				.orElse(null);
//	}

	@Override
	public IdentityType findIdentityTypeById(String identityId) {
		return identityTypeDao.findById(identityId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

	@Override
	public TaxType findTaxTypeById(String taxTypeId) {
		return taxTypeDao.findById(taxTypeId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null,Locale.ENGLISH)));
	}

//	@Override
//	public TerritoryType findterritoryTypeId(String territoryTypeId) {
//		return territoryTypeDao.findById(territoryTypeId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
//	}
	@Override
	public AddressType addAddressType(AddressType addressType) {
		List<AddressType> addressTypes = addressTypeDao.findAll();
		for(AddressType addressType1 : addressTypes) {
			if(addressType.getAddressType().equalsIgnoreCase(addressType1.getAddressType())) {
				throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
			}
		}
		addressType.setId(UUID.randomUUID().toString());
		return addressTypeDao.save(addressType);
	}

	@Override
	public List<AddressType> getAllAddressTypes() {
		return addressTypeDao.findAll();
	}

	@Override
	public AddressType editAddressType(AddressType addressType) {
		AddressType existing = addressTypeDao.findById(addressType.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (addressType.isActive()) {
			boolean duplicateExists =
					addressTypeDao.existsByAddressTypeIgnoreCaseAndActiveTrueAndIdNot(
							addressType.getAddressType(),
							addressType.getId()
					);
			if (duplicateExists) {
				throw new DuplicateEntryException(
						messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
				);
			}
		}
		existing.setAddressType(addressType.getAddressType());
		existing.setActive(addressType.isActive());
		return addressTypeDao.save(existing);
	}
	@Override
	public List<Country> getAllCountries() {
		return memoryEngine.findAllCountries();
	}
	@Override
	public List<States> getStatesByCountry(String countryId) {
		return memoryEngine.findStatesByCountryId(countryId);
	}
	@Override
	public List<Cities> getCitiesByStates(String stateId) {
		return memoryEngine.findCitiesByStateId(stateId);
	}

	@Override
	public String addResource(ResourceDto dto) {
		ResourceType resourceType = resourceTypeDao.findById(dto.getResourceTypeId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Uom uom = uomDao.findById(dto.getUomId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Resources resource = new Resources();
		resource.setId(UUID.randomUUID().toString());
		resource.setResourceCode(dto.getResourceCode());
		resource.setResourceName(dto.getResourceName());
		resource.setUnitRate(dto.getUnitRate());
		resource.setResourceType(resourceType);
		resource.setUom(uom);
		resource.setActive(true);
		resourceDao.save(resource);
		return messageSource.getMessage(
				"resource.save.success", null, Locale.ENGLISH
		);
	}
	@Override
	public void editResource(ResourceDto dto) {
		Resources existingResource = resourceDao.findById(dto.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		ResourceType resourceType = resourceTypeDao.findById(dto.getResourceTypeId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Uom uom = uomDao.findById(dto.getUomId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));

		existingResource.setResourceCode(dto.getResourceCode());
		existingResource.setResourceName(dto.getResourceName());
		existingResource.setUnitRate(dto.getUnitRate());
		existingResource.setResourceType(resourceType);
		existingResource.setUom(uom);
		existingResource.setActive(dto.isActive());
		resourceDao.save(existingResource);
	}
	@Override
	public void addTaxType(TaxType taxType) {
		taxTypeDao
				.findByTaxTypeIgnoreCaseAndActiveTrue(taxType.getTaxType())
				.ifPresent(t -> {
					throw new DuplicateEntryException(
							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
					);
				});
		taxType.setId(UUID.randomUUID().toString());
		taxType.setActive(true);
		taxTypeDao.save(taxType);
	}
	@Override
	public void editTaxType(TaxType taxType) {
		TaxType existing = taxTypeDao.findById(taxType.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (taxType.isActive() &&
				!existing.getTaxType().equalsIgnoreCase(taxType.getTaxType())
		) {
			taxTypeDao
					.findByTaxTypeIgnoreCaseAndActiveTrue(taxType.getTaxType())
					.ifPresent(t -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existing.setTaxType(taxType.getTaxType());
		existing.setActive(taxType.isActive());
		taxTypeDao.save(existing);
	}
	@Override
	public List<TaxType> getAllTaxType() {
		return taxTypeDao.findAll();
	}
//	@Override
//	public void addTerritoryType(TerritoryType territoryType) {
//		territoryTypeDao
//				.findByTerritoryTypeIgnoreCaseAndActiveTrue(
//						territoryType.getTerritoryType()
//				)
//				.ifPresent(t -> {
//					throw new DuplicateEntryException(
//							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
//					);
//				});
//		territoryType.setId(UUID.randomUUID().toString());
//		territoryType.setActive(true);
//		territoryTypeDao.save(territoryType);
//	}
//	@Override
//	public void editTerritoryType(TerritoryType territoryType) {
//		TerritoryType existing = territoryTypeDao.findById(territoryType.getId())
//				.orElseThrow(() -> new DataNotFoundException(
//						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
//				));
//
//		if (territoryType.isActive() &&
//				!existing.getTerritoryType()
//						.equalsIgnoreCase(territoryType.getTerritoryType())
//		) {
//			territoryTypeDao
//					.findByTerritoryTypeIgnoreCaseAndActiveTrue(
//							territoryType.getTerritoryType()
//					)
//					.ifPresent(t -> {
//						throw new DuplicateEntryException(
//								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
//						);
//					});
//		}
//		existing.setTerritoryType(territoryType.getTerritoryType());
//		existing.setActive(territoryType.isActive());
//		territoryTypeDao.save(existing);
//	}
@Override
public List<TerritoryTypeDto> getAllTerritoryType() {
	return Arrays.stream(TerritoryType.values())
			.map(type -> new TerritoryTypeDto(type.name(), type.getLabel()))
			.toList();
}
	@Override
	public void addIdentityType(IdentityType identityType) {

		identityTypeDao
				.findByIdTypeIgnoreCaseAndActiveTrue(
						identityType.getIdType()
				)
				.ifPresent(i -> {
					throw new DuplicateEntryException(
							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
					);
				});
		identityType.setId(UUID.randomUUID().toString());
		identityType.setActive(true);
		identityTypeDao.save(identityType);
	}
	@Override
	public void editIdentityType(IdentityType identityType) {
		IdentityType existing = identityTypeDao.findById(identityType.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (identityType.isActive() &&
				!existing.getIdType()
						.equalsIgnoreCase(identityType.getIdType())
		) {
			identityTypeDao
					.findByIdTypeIgnoreCaseAndActiveTrue(
							identityType.getIdType()
					)
					.ifPresent(i -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existing.setIdType(identityType.getIdType());
		existing.setActive(identityType.isActive());
		identityTypeDao.save(existing);
	}
	@Override
	public List<IdentityType> getAllIdentityType() {
		return identityTypeDao.findAll();
	}
	@Override
	public void addCurrency(Currency currency) {
		currencyDao
				.findByCurrencyCodeIgnoreCaseAndActiveTrue(
						currency.getCurrencyCode()
				)
				.ifPresent(c -> {
					throw new DuplicateEntryException(
							messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
					);
				});

		currency.setId(UUID.randomUUID().toString());
		currency.setActive(true);
		currencyDao.save(currency);
	}
	@Override
	public void editCurrency(Currency currency) {
		Currency existing = currencyDao.findById(currency.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (currency.isActive() &&
				!existing.getCurrencyCode()
						.equalsIgnoreCase(currency.getCurrencyCode())
		) {
			currencyDao
					.findByCurrencyCodeIgnoreCaseAndActiveTrue(
							currency.getCurrencyCode()
					)
					.ifPresent(c -> {
						throw new DuplicateEntryException(
								messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
						);
					});
		}
		existing.setCurrencyCode(currency.getCurrencyCode());
		existing.setCurrencyName(currency.getCurrencyName());
		existing.setActive(currency.isActive());
		currencyDao.save(existing);
	}
//	@Override
//	public void addCompanyType(CompanyType companyType) {
//		if (companyTypeDao.existsByTypeIgnoreCase(companyType.getType())) {
//			throw new DuplicateEntryException(
//					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
//			);
//		}
//		companyType.setId(UUID.randomUUID().toString());
//		companyType.setActive(true);
//		companyTypeDao.save(companyType);
//	}
//	@Override
//	public void editCompanyType(CompanyType companyType) {
//		CompanyType existing = companyTypeDao.findById(companyType.getId())
//				.orElseThrow(() -> new DataNotFoundException(
//						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
//				));
//		if (!existing.getType().equalsIgnoreCase(companyType.getType())
//				&& companyTypeDao.existsByTypeIgnoreCase(companyType.getType())) {
//			throw new DuplicateEntryException(
//					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
//			);
//		}
//		existing.setType(companyType.getType());
//		existing.setActive(companyType.isActive());
//		companyTypeDao.save(existing);
//	}
@Override
public List<CompanyTypeDto> getAllCompanyTypes() {
	return Arrays.stream(CompanyType.values())
			.map(type -> new CompanyTypeDto(type.name(), type.getLabel()))
			.collect(Collectors.toList());
}
	@Override
	public void addCompanyLevel(CompanyLevel companyLevel) {
		if (companyLevelDao.existsByLevelIgnoreCase(companyLevel.getLevel())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		companyLevel.setId(UUID.randomUUID().toString());
		companyLevel.setActive(true);
		companyLevelDao.save(companyLevel);
	}
	@Override
	public void editCompanyLevel(CompanyLevel companyLevel) {
		CompanyLevel existing = companyLevelDao.findById(companyLevel.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));

		if (!existing.getLevel().equalsIgnoreCase(companyLevel.getLevel())
				&& companyLevelDao.existsByLevelIgnoreCase(companyLevel.getLevel())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		existing.setLevel(companyLevel.getLevel());
		existing.setActive(companyLevel.isActive());
		companyLevelDao.save(existing);
	}
	@Override
	public List<CompanyLevel> getAllCompanyLevel() {
		return companyLevelDao.findAll();
	}
	@Override
	public void addCompanyStatus(CompanyStatus companyStatus) {
		if (companyStatusDao.existsByComStatusIgnoreCase(companyStatus.getComStatus())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		companyStatus.setId(UUID.randomUUID().toString());
		companyStatus.setActive(true);
		companyStatusDao.save(companyStatus);
	}
	@Override
	public void editCompanyStatus(CompanyStatus companyStatus) {
		CompanyStatus existing = companyStatusDao.findById(companyStatus.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (!existing.getComStatus().equalsIgnoreCase(companyStatus.getComStatus())
				&& companyStatusDao.existsByComStatusIgnoreCase(companyStatus.getComStatus())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		existing.setComStatus(companyStatus.getComStatus());
		existing.setActive(companyStatus.isActive());
		companyStatusDao.save(existing);
	}
	@Override
	public List<CompanyStatus> getAllCompanyStatus() {
		return companyStatusDao.findAll();
	}
	@Override
	public void addCompanyConstitution(CompanyConstitution companyConstitution) {
		if (companyConstitutionDao.existsByComConstitutionIgnoreCase(
				companyConstitution.getComConstitution())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		companyConstitution.setId(UUID.randomUUID().toString());
		companyConstitution.setActive(true);
		companyConstitutionDao.save(companyConstitution);
	}
	@Override
	public void editCompanyConstitution(CompanyConstitution companyConstitution) {
		CompanyConstitution existing = companyConstitutionDao.findById(companyConstitution.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (!existing.getComConstitution().equalsIgnoreCase(companyConstitution.getComConstitution())
				&& companyConstitutionDao.existsByComConstitutionIgnoreCase(
				companyConstitution.getComConstitution())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		existing.setComConstitution(companyConstitution.getComConstitution());
		existing.setActive(companyConstitution.isActive());
		companyConstitutionDao.save(existing);
	}
	@Override
	public List<CompanyConstitution> getAllCompanyConstitution() {
		return companyConstitutionDao.findAll();
	}

	@Override
	public void addCompanyNature(CompanyNature companyNature) {
		if (companyNatureDao.existsByComNatureIgnoreCase(companyNature.getComNature())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		companyNature.setId(UUID.randomUUID().toString());
		companyNature.setActive(true);
		companyNatureDao.save(companyNature);
	}
	@Override
	public void editCompanyNature(CompanyNature companyNature) {
		CompanyNature existing = companyNatureDao.findById(companyNature.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (!existing.getComNature().equalsIgnoreCase(companyNature.getComNature())
				&& companyNatureDao.existsByComNatureIgnoreCase(companyNature.getComNature())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		existing.setComNature(companyNature.getComNature());
		existing.setActive(companyNature.isActive());
		companyNatureDao.save(existing);
	}
	@Override
	public List<CompanyNature> getAllCompanyNature() {
		return companyNatureDao.findAll();
	}
	@Override
	public void addLanguage(Language language) {
		if (languageDao.existsByLanguageIgnoreCase(language.getLanguage())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		language.setId(UUID.randomUUID().toString());
		language.setActive(true);
		languageDao.save(language);
	}
	@Override
	public void editLanguage(Language language) {
		Language existing = languageDao.findById(language.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (!existing.getLanguage().equalsIgnoreCase(language.getLanguage())
				&& languageDao.existsByLanguageIgnoreCase(language.getLanguage())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		existing.setLanguage(language.getLanguage());
		existing.setActive(language.isActive());
		languageDao.save(existing);
	}
	@Override
	public List<Language> getAllLanguage() {
		return languageDao.findAll();
	}
	@Override
	public void addBusinessNature(BusinessNature businessNature) {
		if (businessNatureDao.existsByBusinessNatureIgnoreCase(
				businessNature.getBusinessNature())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		businessNature.setId(UUID.randomUUID().toString());
		businessNature.setActive(true);
		businessNatureDao.save(businessNature);
	}
	@Override
	public void editBusinessNature(BusinessNature businessNature) {
		BusinessNature existing = businessNatureDao.findById(businessNature.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (!existing.getBusinessNature().equalsIgnoreCase(
				businessNature.getBusinessNature())
				&& businessNatureDao.existsByBusinessNatureIgnoreCase(
				businessNature.getBusinessNature())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		existing.setBusinessNature(businessNature.getBusinessNature());
		existing.setActive(businessNature.isActive());
		businessNatureDao.save(existing);
	}
	@Override
	public List<BusinessNature> getAllBusinessNature() {
		return businessNatureDao.findAll();
	}

	@Override
	public CompanyLevel getCompanyLevelById(String comLevelId) {
			return companyLevelDao.findById(comLevelId).orElseThrow(
					() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))
			);

	}
	@Override
	public CompanyNature getCompanyNatureById(String comNatureId) {
		return companyNatureDao.findById(comNatureId)
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public BusinessNature getBusinessNatureById(String businessNatureId) {
		return businessNatureDao.findById(businessNatureId)
				.orElseThrow(()->new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}

	@Override
	public CompanyConstitution getCompanyConstitutionById(String companyConstitutionId) {
		return companyConstitutionDao.findById(companyConstitutionId)
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}

	@Override
	public CompanyStatus getCompanyStatusById(String statusId) {
		return companyStatusDao.findById(statusId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}
	@Override
	public Currency getCurrencyById(String currencyId) {
		return currencyDao.findById(currencyId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}
	@Override
	public List<DirectorTypeDto> getAllDirectorType() {
		return Arrays.stream(DirectorType.values())
				.map(type -> new DirectorTypeDto(type.name(), type.getLabel()))
				.toList();
	}
	@Override
	public Language getLanguageById(String languageId) {
		return languageDao.findById(languageId)
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public TaxType getTaxTYpeById(String taxTypeId) {
		return taxTypeDao.findById(taxTypeId)
				.orElseThrow(() -> new DataNotFoundException(messageSource
						.getMessage("data.not.found", null, Locale.ENGLISH)));
	}

	@Override
	public List<OfferSubmissionModeDto> getAllOfferSubmissionMode() {
		return Arrays.stream(OfferSubmissionMode.values())
				.map(mode -> new OfferSubmissionModeDto(mode.name(), mode.getLabel()))
				.toList();

	}

    @Override
    public Country findCountryById(String countryId) {
        return getAllCountries().stream()
                .filter(con -> con.getId().equals(countryId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
    }

	@Override
	public String addAttribute(Attribute attribute) {
		if(!attributeDao.existsByAttributeName(attribute.getAttributeName())){
			attribute.setId(UUID.randomUUID().toString());
			attributeDao.save(attribute);
			return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
		}
		return messageSource.getMessage("data.duplicate", null, Locale.ENGLISH);
	}

	@Override
	public List<Attribute> getAllAttributes() {
		return attributeDao.findAll();
	}

	@Override
	public List<AttributeGroup> getAllAttributeGroup() {
		return attributeGroupDao.findAll();
	}

	@Override
	public String addAttributeGroup(AttributeGroup attributeGroup, List<String> attributeIds) {
		AttributeGroup savedAttributeGroup = null;
		if(!attributeGroupDao.existsByGroupName(attributeGroup.getGroupName())){
			if(attributeGroup.getId() == null){
				attributeGroup.setId(UUID.randomUUID().toString());
				savedAttributeGroup = attributeGroupDao.save(attributeGroup);
			}
			else{
				savedAttributeGroup = attributeGroupDao.findById(attributeGroup.getId())
						.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			}

			for(String id : attributeIds){
				Attribute attribute = attributeDao.findById(id)
						.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
				Set<AttributeGroup> attributeGroupSet = attribute.getAttributeGroup();
				attributeGroupSet.add(savedAttributeGroup);
				attribute.setAttributeGroup(attributeGroupSet);
				attributeDao.save(attribute);
			}
			return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
		}
		return messageSource.getMessage("data.duplicate", null, Locale.ENGLISH);
	}
}
