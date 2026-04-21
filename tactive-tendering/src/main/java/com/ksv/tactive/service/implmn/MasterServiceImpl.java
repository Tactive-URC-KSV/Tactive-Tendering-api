package com.ksv.tactive.service.implmn;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
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
	private ResourceDao resourceDao;
	@Autowired
	private CurrencyDao currencyDao;
	@Autowired
	private AddressTypeDao addressTypeDao;
	@Autowired
	private IdentityTypeDao identityTypeDao;
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
	private ResourceAttributeGroupDao resourceAttributeGroupDao;
	@Autowired
	private UomConversionDao uomConversionDao;
	@Autowired
	private SectorFieldsDao  sectorFieldsDao;
	@Autowired
	private DesignationDao designationDao;
	@Autowired
	private CurrencyConversionDao currencyConversionDao;

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
	public String addUomConversion(UomConversionDto uomConversionDto) {
		Uom baseUom = uomDao.findById(convertToUUID(uomConversionDto.getBaseUomId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		Uom conversionUom = uomDao.findById(convertToUUID(uomConversionDto.getConversionUomId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		UomConversion conversion = new UomConversion();
		conversion.setBaseUom(baseUom);
		conversion.setConversionUom(conversionUom);
		conversion.setFormula(uomConversionDto.getFormula());
		if(uomConversionDto.getId() != null){
			conversion.setId(convertToUUID(uomConversionDto.getId()));
		}
		uomConversionDao.save(conversion);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public List<UomConversion> getUomConversionByBaseUom(String baseUomId) {
		return uomConversionDao.findAllByBaseUom_Id(convertToUUID(baseUomId));
	}
	@Override
	public List<EnumDto> getAllFieldType() {
		return Arrays.stream(FieldType.values())
				.map(type -> new EnumDto(type.name(), type.getLabel()))
				.toList();
	}

	@Override
	public List<EnumDto> getAllFieldSection() {
		return Arrays.stream(FieldSection.values())
				.map(section -> new EnumDto(section.name(), section.getLabel()))
				.toList();
	}

	@Override
	public String addSectorFields(SectorFieldsDto sectorFieldsDto) {
		UUID sectorId = convertToUUID(sectorFieldsDto.getSectorId());
		if (sectorFieldsDto.getId() == null) {
			if (sectorFieldsDao.existsByFieldNameAndSector_Id(
					sectorFieldsDto.getFieldName(), sectorId)) {
				throw new DuplicateEntryException("Field already exists");
			}
		}
		else {
			UUID id = convertToUUID(sectorFieldsDto.getId());

			if (sectorFieldsDao.existsByFieldNameAndSector_IdAndIdNot(
					sectorFieldsDto.getFieldName(), sectorId, id)) {
				throw new DuplicateEntryException("Field already exists");
			}
		}
		SectorFields sectorFields;
		if (sectorFieldsDto.getId() != null) {
			sectorFields = sectorFieldsDao.findById(convertToUUID(sectorFieldsDto.getId()))
					.orElseThrow(() -> new DataNotFoundException(
							messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		} else {
			sectorFields = new SectorFields();
		}
		Sector sector = findSectorById(sectorFieldsDto.getSectorId());
		sectorFields.setSector(sector);
		sectorFields.setFieldName(sectorFieldsDto.getFieldName());
		sectorFields.setFieldType(
				FieldType.valueOf(sectorFieldsDto.getFieldType().toUpperCase()));
		sectorFields.setFieldSection(
				FieldSection.valueOf(sectorFieldsDto.getFieldSection().toUpperCase()));
		sectorFields.setMandatory(sectorFieldsDto.isMandatory());
		sectorFields.setActive(sectorFieldsDto.isActive());
		sectorFieldsDao.save(sectorFields);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}

	@Override
	public List<SectorFields> getFieldsBySector(String sectorId) {
		return sectorFieldsDao.findAllBySector_Id(convertToUUID(sectorId));
	}

	@Override
	public SectorFields findSectorFieldById(String techFieldId) {
		return sectorFieldsDao.findById(convertToUUID(techFieldId))
				.orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
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
	public List<EnumDto> getAllResourceTypes() {
		return Arrays.stream(ResourceType.values())
				.map(type-> new EnumDto(type.name(),type.getLabel()))
				.toList();
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
	public List<EnumDto> getAllTaxType() {
		return Arrays.stream(TaxType.values())
				.map(tax -> new EnumDto(tax.name(), tax.getLabel()))
				.toList();
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
	public Page<ResourceDto> getAllResources(int page, int size, String resourceType) {
		Page<Resources> resourcePage;
		Pageable pageable = PageRequest.of(page, size, Sort.by("resourceCode").ascending());
		if (resourceType != null && !resourceType.isEmpty()) {
			ResourceType type;
			try {
				type = ResourceType.valueOf(resourceType.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Invalid resourceType: " + resourceType);
			}
			resourcePage = resourceDao.findByResourceType(type,pageable);
		}
		else {
			resourcePage = resourceDao.findAll(pageable);
		}
		return resourcePage.map(this::convertToDto);
	}
	@Override
	public Page<ResourceDto> searchResources(int page, int size, String search, String resourceType, String resourceName, String resourceCode) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("resourceCode").ascending());
		
		Specification<Resources> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (resourceType != null && !resourceType.isEmpty()) {
				predicates.add(cb.equal(root.get("resourceType"), ResourceType.valueOf(resourceType.toUpperCase())));
			}

			if (resourceName != null && !resourceName.trim().isEmpty()) {
				predicates.add(cb.like(cb.lower(root.get("resourceName")), "%" + resourceName.toLowerCase() + "%"));
			}

			if (resourceCode != null && !resourceCode.trim().isEmpty()) {
				predicates.add(cb.like(cb.lower(root.get("resourceCode")), "%" + resourceCode.toLowerCase() + "%"));
			}

			if (search != null && !search.trim().isEmpty()) {
				String keyword = "%" + search.toLowerCase() + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.get("resourceName")), keyword),
						cb.like(cb.lower(root.get("resourceCode")), keyword)
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		return resourceDao.findAll(spec, pageable).map(this::convertToDto);
	}
	private ResourceDto convertToDto(Resources resource) {
		ResourceDto dto = new ResourceDto();
		dto.setId(resource.getId().toString());
		dto.setResourceName(resource.getResourceName());
		dto.setResourceCode(resource.getResourceCode());
		dto.setLevel(resource.getLevel());
		dto.setLastLevel(resource.isLastLevel());
		dto.setActive(resource.isActive());
		dto.setResourceTypeId(
				resource.getResourceType() != null ? resource.getResourceType().name() : null
		);
		dto.setUomId(
				resource.getUom() != null ? resource.getUom().getUomCode() : null
		);
		dto.setParentId(
				resource.getParent() != null ? resource.getParent().getResourceCode() : null
		);
		return dto;
	}
	@Override
	public String addResource(ResourceDto dto) {
		ResourceType resourceType = ResourceType.valueOf(dto.getResourceTypeId());
		Uom uom = uomDao.findById(convertToUUID(dto.getUomId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		Resources resource = new Resources();
		resource.setResourceCode(dto.getResourceCode());
		resource.setResourceName(dto.getResourceName());
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
		ResourceType resourceType = ResourceType.valueOf(dto.getResourceTypeId());
		Uom uom = uomDao.findById(convertToUUID(dto.getUomId()))
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));

		existingResource.setResourceCode(dto.getResourceCode());
		existingResource.setResourceName(dto.getResourceName());
		existingResource.setResourceType(resourceType);
		existingResource.setUom(uom);
		existingResource.setActive(dto.isActive());
		resourceDao.save(existingResource);
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
	public String addCurrencyConversion(String baseCurrencyId,
										String conversionCurrencyId,
										double rate,
										LocalDate effectiveDate) {
		UUID baseId = convertToUUID(baseCurrencyId);
		UUID targetId = convertToUUID(conversionCurrencyId);
		Currency base = currencyDao.findById(baseId)
				.orElseThrow(() -> new RuntimeException("Base currency not found"));

		Currency target = currencyDao.findById(targetId)
				.orElseThrow(() -> new RuntimeException("Conversion currency not found"));

		Optional<CurrencyConversion> existing = currencyConversionDao
				.findByBaseCurrencyAndConversionCurrencyAndEffectiveDate(
						base, target, effectiveDate
				);
		CurrencyConversion conversion;
		if (existing.isPresent()) {
			conversion = existing.get();
		} else {
			conversion = new CurrencyConversion();
			conversion.setBaseCurrency(base);
			conversion.setConversionCurrency(target);
			conversion.setEffectiveDate(effectiveDate);
		}
		conversion.setConversionRate(BigDecimal.valueOf(rate));
		currencyConversionDao.save(conversion);
		return "Currency conversion saved successfully";
	}
	@Override
	public List<CurrencyConversion> getLatestByBaseCurrency(String baseCurrencyId) {
		UUID baseId = convertToUUID(baseCurrencyId);
		Currency base = currencyDao.findById(baseId)
				.orElseThrow(() -> new RuntimeException("Base currency not found"));
		return currencyConversionDao.findLatestByBaseCurrency(base);
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
	public List<EnumDto> getAllCompanyStatus() {

		return Arrays.stream(CompanyStatus.values())
				.map(status -> new EnumDto(status.name(), status.getLabel()))
				.toList();
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
	public List<AttributeDTO> getAttributesByGroup(String groupId) {
		return attributeDao.findByAttributeGroupId(convertToUUID(groupId))
				.stream()
				.map(attr -> {
					AttributeDTO dto = new AttributeDTO();
					dto.setAttributeCode(attr.getAttributeCode());
					dto.setAttributeName(attr.getAttributeName());
					dto.setActive(attr.isActive());
					dto.setId(attr.getId().toString());
					return dto;
				})
				.toList();
	}
	@Override
	public Page<AttributeGroupDTO> getAttributeGroups(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("groupCode").ascending());
		Page<AttributeGroup> groupPage = attributeGroupDao.findAll(pageable);
		return groupPage.map(group -> {
			AttributeGroupDTO dto = new AttributeGroupDTO();
			dto.setId(group.getId().toString());
			dto.setGroupCode(group.getGroupCode());
			dto.setGroupName(group.getGroupName());
			dto.setActive(group.isActive());
			return dto;
		});
	}
	@Override
	public Page<AttributeGroupDTO> searchAttributeGroups(int page, int size, String search) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("groupCode").ascending());
		Long code = null;
		try {
			code = Long.parseLong(search);
		} catch (NumberFormatException ignored) {}
		Page<AttributeGroup> groupPage =
				attributeGroupDao.searchAttributeGroups(search, code, pageable);
		return groupPage.map(group -> {
			AttributeGroupDTO dto = new AttributeGroupDTO();
			dto.setId(group.getId().toString());
			dto.setGroupCode(group.getGroupCode());
			dto.setGroupName(group.getGroupName());
			dto.setActive(group.isActive());
			return dto;
		});
	}
	@Override
	public String addAttributeGroup(AttributeGroup attributeGroup, List<String> attributeIds) {
		Optional<AttributeGroup> existingGroupByName =
				attributeGroupDao.findByGroupName(attributeGroup.getGroupName());
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
					.orElseThrow(() -> new DataNotFoundException(
							messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
					));
			savedAttributeGroup.setGroupName(attributeGroup.getGroupName());
			savedAttributeGroup.setActive(attributeGroup.isActive());
			savedAttributeGroup = attributeGroupDao.save(savedAttributeGroup);
		}
		List<Attribute> requestedAttributes = attributeDao.findAllById(
				attributeIds.stream().map(UUID::fromString).toList()
		);
		List<Attribute> currentAttributes =
				attributeDao.findByAttributeGroup(savedAttributeGroup);
		for (Attribute attr : currentAttributes) {
			if (requestedAttributes.stream().noneMatch(req -> req.getId().equals(attr.getId()))) {
				attr.setAttributeGroup(null);
				attributeDao.save(attr);
			}
		}
		for (Attribute attr : requestedAttributes) {
			if (attr.getAttributeGroup() == null ||
					!attr.getAttributeGroup().getId().equals(savedAttributeGroup.getId())) {

				attr.setAttributeGroup(savedAttributeGroup);
				attributeDao.save(attr);
			}
		}
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public String addResourceAttribute(ResourceAttributeDto dto) {
		Optional<ResourceAttributeGroup> existing = resourceAttributeGroupDao.findByResourceIdAndAttributeGroupId(UUID.fromString(dto.getResourceId()), UUID.fromString(dto.getAttributeGroupId()));
		if (existing.isPresent()) {
			boolean isNew = dto.getId() == null;
			boolean isDifferentId = !existing.get().getId().equals(dto.getId());
			if (isNew || isDifferentId) {
				return messageSource.getMessage("data.duplicate", null, Locale.ENGLISH);
			}
		}
		ResourceAttributeGroup resourceAttributeGroup;
		if (dto.getId() == null) {
			resourceAttributeGroup = new ResourceAttributeGroup();
		} else {
			resourceAttributeGroup = resourceAttributeGroupDao.findById(convertToUUID(dto.getId()))
					.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		}
		Resources resources = resourceDao.findById(convertToUUID(dto.getResourceId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		AttributeGroup attributeGroup = attributeGroupDao.findById(convertToUUID(dto.getAttributeGroupId()))
				.orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
		resourceAttributeGroup.setResource(resources);
		resourceAttributeGroup.setAttributeGroup(attributeGroup);
		resourceAttributeGroup.setMandatory(dto.isMandatory());
		resourceAttributeGroupDao.save(resourceAttributeGroup);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public List<ResourceAttributeGroup> getByResourceId(String resourceId) {
		return resourceAttributeGroupDao.findByResourceId(UUID.fromString(resourceId));
	}
	@Override
	public String addDesignation(Designation designation) {
		if (designation.getId() == null) {
			if (designationDao.existsByDesignationNameIgnoreCase(designation.getDesignationName())) {
				throw new RuntimeException("Designation already exists");
			}
			designationDao.save(designation);
			return "Designation added successfully";
		}
		Designation existing = designationDao.findById(designation.getId())
				.orElseThrow(() -> new RuntimeException("Designation not found"));
		if (designationDao.existsByDesignationNameIgnoreCaseAndIdNot(
				designation.getDesignationName(), designation.getId())) {
			throw new RuntimeException("Designation already exists");
		}
		existing.setDesignationName(designation.getDesignationName());
		designationDao.save(existing);
		return "Designation updated successfully";
	}

	@Override
	public List<Designation> getDesignation() {
		return designationDao.findAll();
	}

	@Override
	public Designation getDesignationById(String position) {
		return designationDao.findById(convertToUUID(position))
				.orElseThrow(()-> new  DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH)));
	}
}
