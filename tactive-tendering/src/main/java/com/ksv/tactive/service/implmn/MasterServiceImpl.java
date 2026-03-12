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
	private CurrencyDao currencyDao;
	@Autowired
	private AddressTypeDao addressTypeDao;
	@Autowired
	private IdentityTypeDao identityTypeDao;
	@Autowired
	private TaxTypeDao taxTypeDao;
	@Autowired
	private CompanyStatusDao companyStatusDao;
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
	@Autowired
	private ResourceAttributeDao resourceAttributeDao;

	private UUID convertToUUID(String value){
		return UUID.fromString(value);
	}
	@Override
	public String addSector(Sector sector) {
		if(sectorDao.findBySectorNameIgnoreCase(sector.getSectorName()) != null){
			throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
		}
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
		if(scopeOfPackDao.findByScope(scope.getScope()) != null){
			throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
		}
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
		if(uomDao.findByUomCodeIgnoreCaseAndUomNameIgnoreCase(uom.getUomCode(),uom.getUomName()) != null){
			throw new  DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
		}
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
		return regionRepo.findById(convertToUUID(regionId)).orElse(null);
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
		regionRepo.save(region);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public List<Uom> getAllUOMs() {
		return uomDao.findAll();
	}
	@Override
	public Uom findUomByCode(String uomCode) {
		return uomDao.findByUomCode(uomCode);
	}
	@Override
	public Uom findUomById(String uomId) {
		return uomDao.findById(convertToUUID(uomId)).orElse(null);
	}
	@Override
	public List<Sector> getAllSector() {
		return sectorDao.findAll();
	}
	@Override
	public Sector findSectorById(String sectorId) {
		return sectorDao.findById(convertToUUID(sectorId)).orElse(null);
	}
	@Override
	public List<ScopeOfPackage> getAllScopes() {
		return scopeOfPackDao.findAll();
	}
	@Override
	public ScopeOfPackage findScopeOfPackageById(String id) {
		return scopeOfPackDao.findById(convertToUUID(id)).orElse(null);
	}
	@Override
	public List<ProjectStatus> getAllProjectStatus() {
		return statusDao.findAll();
	}
	@Override
	public ProjectStatus findStatusById(String projectStatusId) {
		return statusDao.findById(convertToUUID(projectStatusId)).orElse(null);
	}
	@Override
	public ProjectStatus findStatusByProcessUrl(String requestURI) {
		return statusDao.findByProcessUrl(requestURI);
	}
	@Override
	public String addProjectStatus(ProjectStatus projectStatus) {
		statusDao.save(projectStatus);
		return null;
	}
	@Override
	public String addListOfApprovals(ListOfApprovals listOfApprovals) {
		if(listOfApprovalsDao.findByDocumentName(listOfApprovals.getDocumentName()) != null) {
			throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
		}
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
		return listOfApprovalsDao.findAll();
	}
	@Override
	public String addCostCodeType(CostCodeType costCodeType) {
		if(costCodeTypeDao.findByCostCodeName(costCodeType.getCostCodeName()) != null) {
			throw new DuplicateEntryException(messageSource.getMessage("costcode.duplicate", null, Locale.ENGLISH));
		}
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
		return resourceTypeDao.findById(convertToUUID(resourceTypeId)).orElse(null);
	}
	@Override
	public List<Resources> getAllResources() {
		return resourceDao.findAll();
	}
	@Override
	public Resources findResourceById(String resourceId) {
		return resourceDao.findById(convertToUUID(resourceId)).orElse(null);
	}

	@Override
	public List<EnumDto> getAllQuantityTypes() {
		return Arrays.stream(QuantityType.values())
				.map(qt -> new EnumDto(qt.name(), qt.getLabel()))
				.toList();
	}
	@Override
	public List<EnumDto> getAllResourceNatures() {
		return Arrays.stream(ResourceNature.values())
				.map(nature->new EnumDto(nature.name(),nature.getLabel()))
				.collect(Collectors.toList());
	}
	@Override
	public List<Currency> getAllCurrencies() {
		return currencyDao.findAll();
	}
	@Override
	public Currency findCurrencyById(String currencyId) {
		return currencyDao.findById(convertToUUID(currencyId)).orElse(null);
	}
	@Override
	public AddressType findAddressTypeById(String addressTypeId) {
		return addressTypeDao.findById(convertToUUID(addressTypeId)).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}
	@Override
	public IdentityType findIdentityTypeById(String identityId) {
		return identityTypeDao.findById(convertToUUID(identityId)).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

	@Override
	public TaxType findTaxTypeById(String taxTypeId) {
		return taxTypeDao.findById(convertToUUID(taxTypeId)).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null,Locale.ENGLISH)));
	}
	@Override
	public AddressType addAddressType(AddressType addressType) {
		List<AddressType> addressTypes = addressTypeDao.findAll();
		for(AddressType addressType1 : addressTypes) {
			if(addressType.getAddressType().equalsIgnoreCase(addressType1.getAddressType())) {
				throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
			}
		}
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
					addressTypeDao.existsByAddressTypeIgnoreCaseAndActiveTrue(
							addressType.getAddressType()
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
		ResourceType resourceType = resourceTypeDao.findById(convertToUUID(dto.getResourceTypeId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Uom uom = uomDao.findById(convertToUUID(dto.getUomId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Resources resource = new Resources();
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
		Resources existingResource = resourceDao.findById(convertToUUID(dto.getId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		ResourceType resourceType = resourceTypeDao.findById(convertToUUID(dto.getResourceTypeId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Uom uom = uomDao.findById(convertToUUID(dto.getUomId()))
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
	@Override
	public List<EnumDto> getAllTerritoryType() {
		return Arrays.stream(TerritoryType.values())
				.map(type -> new EnumDto(type.name(), type.getLabel()))
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
	@Override
	public List<EnumDto> getAllCompanyTypes() {
		return Arrays.stream(CompanyType.values())
				.map(type -> new EnumDto(type.name(), type.getLabel()))
				.collect(Collectors.toList());
	}
	@Override
	public List<EnumDto> getAllCompanyLevel() {
		return Arrays.stream(CompanyLevel.values())
				.map(level -> new EnumDto(level.name(), level.getLabel()))
				.collect(Collectors.toList());
	}
	@Override
	public void addCompanyStatus(CompanyStatus companyStatus) {
		if (companyStatusDao.existsByComStatusIgnoreCase(companyStatus.getComStatus())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
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
	public void addLanguage(Language language) {
		if (languageDao.existsByLanguageIgnoreCase(language.getLanguage())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
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
	public List<EnumDto> getAllCompanyNature() {
		return Arrays.stream(CompanyNature.values())
				.map(nature -> new EnumDto(nature.name(),nature.getLabel()))
				.collect(Collectors.toList());
	}

	@Override
	public BusinessNature getBusinessNatureById(String businessNatureId) {
		return businessNatureDao.findById(convertToUUID(businessNatureId))
				.orElseThrow(()->new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}

	@Override
	public CompanyConstitution getCompanyConstitutionById(String companyConstitutionId) {
		return companyConstitutionDao.findById(convertToUUID(companyConstitutionId))
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}

	@Override
	public CompanyStatus getCompanyStatusById(String statusId) {
		return companyStatusDao.findById(convertToUUID(statusId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}
	@Override
	public Currency getCurrencyById(String currencyId) {
		return currencyDao.findById(convertToUUID(currencyId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}
	@Override
	public List<EnumDto> getAllDirectorType() {
		return Arrays.stream(DirectorType.values())
				.map(type -> new EnumDto(type.name(), type.getLabel()))
				.toList();
	}
	@Override
	public Language getLanguageById(String languageId) {
		return languageDao.findById(convertToUUID(languageId))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
	@Override
	public TaxType getTaxTYpeById(String taxTypeId) {
		return taxTypeDao.findById(convertToUUID(taxTypeId))
				.orElseThrow(() -> new DataNotFoundException(messageSource
						.getMessage("data.not.found", null, Locale.ENGLISH)));
	}

	@Override
	public List<EnumDto> getAllOfferSubmissionMode() {
		return Arrays.stream(OfferSubmissionMode.values())
				.map(mode -> new EnumDto(mode.name(), mode.getLabel()))
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
		Optional<Attribute> existingByName = attributeDao.findByAttributeName(attribute.getAttributeName());

		if (existingByName.isPresent()) {
			boolean isNew = attribute.getId() == null;
			boolean isDifferentId = !existingByName.get().getId().equals(attribute.getId());

			if (isNew || isDifferentId) {
				return messageSource.getMessage("data.duplicate", null, Locale.ENGLISH);
			}
		}

		if (attribute.getId() == null) {
			attributeDao.save(attribute);
		} else {
			Attribute savedAttribute = attributeDao.findById(attribute.getId())
					.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			savedAttribute.setAttributeName(attribute.getAttributeName());
			savedAttribute.setActive(attribute.isActive());
			attributeDao.save(savedAttribute);
		}
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
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
		Optional<AttributeGroup> existingGroupByName = attributeGroupDao.findByGroupName(attributeGroup.getGroupName());
		if (existingGroupByName.isPresent()) {
			boolean isNew = attributeGroup.getId() == null;
			boolean isDifferentId = !existingGroupByName.get().getId().equals(attributeGroup.getId());
			if (isNew || isDifferentId) {
				return messageSource.getMessage("data.duplicate", null, Locale.ENGLISH);
			}
		}
		AttributeGroup savedAttributeGroup;
		if (attributeGroup.getId() == null) {
			savedAttributeGroup = attributeGroupDao.save(attributeGroup);
		} else {
			savedAttributeGroup = attributeGroupDao.findById(attributeGroup.getId())
					.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
			savedAttributeGroup.setGroupName(attributeGroup.getGroupName());
			savedAttributeGroup.setActive(attributeGroup.isActive());
			savedAttributeGroup = attributeGroupDao.save(savedAttributeGroup);
		}
		List<Attribute> requestedAttributes = attributeDao.findAllById(
				attributeIds.stream()
						.map(UUID::fromString)
						.toList()
		);
		List<Attribute> currentAttributes = attributeDao.findByAttributeGroup(savedAttributeGroup);
		for (Attribute attr : currentAttributes) {
			if (requestedAttributes.stream().noneMatch(req -> req.getId().equals(attr.getId()))) {
				attr.getAttributeGroup().remove(savedAttributeGroup);
				attributeDao.save(attr);
			}
		}
		for (Attribute attr : requestedAttributes) {
			if (!attr.getAttributeGroup().contains(savedAttributeGroup)) {
				attr.getAttributeGroup().add(savedAttributeGroup);
				attributeDao.save(attr);
			}
		}
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public String addResourceAttribute(ResourceAttributeDto dto) {
		Optional<ResourceAttribute> existing = resourceAttributeDao.findByResourcesIdAndAttributeGroupId(UUID.fromString(dto.getResourceId()), UUID.fromString(dto.getAttributeGroupId()));
		if (existing.isPresent()) {
			boolean isNew = dto.getId() == null;
			boolean isDifferentId = !existing.get().getId().equals(dto.getId());

			if (isNew || isDifferentId) {
				return messageSource.getMessage("data.duplicate", null, Locale.ENGLISH);
			}
		}
		ResourceAttribute resourceAttribute;
		if (dto.getId() == null) {
			resourceAttribute = new ResourceAttribute();
		} else {
			resourceAttribute = resourceAttributeDao.findById(convertToUUID(dto.getId()))
					.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		}
		Resources resources = resourceDao.findById(convertToUUID(dto.getResourceId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		AttributeGroup attributeGroup = attributeGroupDao.findById(convertToUUID(dto.getAttributeGroupId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		resourceAttribute.setResources(resources);
		resourceAttribute.setAttributeGroup(attributeGroup);
		resourceAttribute.setMandatory(dto.isMandatory());
		resourceAttributeDao.save(resourceAttribute);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public List<ResourceAttribute> getByResourceId(String resourceId) {
		return resourceAttributeDao.findByResourcesId(UUID.fromString(resourceId));
	}
	@Override
	public List<Attribute> getAttributesByGroupId(String attributeGroupId) {
		return attributeDao.findByAttributeGroupId(UUID.fromString(attributeGroupId));
	}
}
