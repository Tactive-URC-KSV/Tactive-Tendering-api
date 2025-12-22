package com.ksv.tactive.service.implmn;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.master.dao.*;
import com.ksv.tactive.master.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.ksv.tactive.currency.dao.CurrencyDao;
import com.ksv.tactive.currency.entity.Currency;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.location.dao.CitiesDao;
import com.ksv.tactive.location.dao.CountryDao;
import com.ksv.tactive.location.entity.Cities;
import com.ksv.tactive.location.entity.Country;
import com.ksv.tactive.project.dao.CostCodeTypeDao;
import com.ksv.tactive.project.entity.CostCodeType;
import com.ksv.tactive.service.MasterService;
@Service
public class MasterServiceImpl implements MasterService{
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private ListOfApprovalsDao listOfApprovalsDao;
	@Autowired
	private RegionDao regionRepo;
	@Autowired
	private UOMDao uomDao;
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
	private QuantityTypeDao quantityTypeDao;
	@Autowired
	private ResourceNatureDao resouceNatureDao;
	@Autowired
	private CurrencyDao currencyDao;
	@Autowired
	private CountryDao countryDao;
	@Autowired
	private CitiesDao citiesDao;
	@Autowired
	private AddressTypeDao addressTypeDao;
	@Autowired
	private IdentityTypeDao identityTypeDao;
	@Autowired
	private TaxTypeDao taxTypeDao;
	@Autowired
	private TerritoryTypeDao territoryTypeDao;
	
	@Override
	public String addSector(Sector sector) {
		sector.setId(UUID.randomUUID().toString());
		sectorDao.save(sector);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}
	@Override
	public String addScope(ScopeOfPackage scope) {
		scope.setId(UUID.randomUUID().toString());
		scopeOfPackDao.save(scope);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
	}

	@Override
	public void editScopeOfPackage(ScopeOfPackage scope) {
		ScopeOfPackage existingScope = scopeOfPackDao.findById(scope.getId()).orElseThrow(
				()->new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))
		);
		if(scopeOfPackDao.findByScopeIgnoreCase(existingScope.getScope()).equals(scope)) {
			throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
		}
	}

	@Override
	public String addUom(UOM uom) {
		uom.setId(UUID.randomUUID().toString());
		uomDao.save(uom);
		return messageSource.getMessage("data.save.success", null, Locale.ENGLISH);
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
		Region existingRegion = regionRepo.findById(region.getId()).orElseThrow(
				() -> new DataNotFoundException(messageSource.getMessage("data.not.found", null, Locale.ENGLISH))
		);
		if(regionRepo.findByRegionNameIgnoreCase(existingRegion.getRegionName()).isActive()) {
			throw new DuplicateEntryException(messageSource.getMessage("data.duplicate", null, Locale.ENGLISH));
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
	public List<UOM> getAllUOMs() {
		return uomDao.findAll();
	}
	@Override
	public UOM findUomByCode(String uomCode) {
		UOM uom = uomDao.findByUomCode(uomCode);
		return uom;
	}
	@Override
	public UOM findUomById(String uomId) {
		UOM uom = uomDao.findById(uomId).orElse(null);
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
	public List<CostCodeType> getAllCostCodeTypes() {
		return costCodeTypeDao.findAll();
	}
	@Override
	public List<ResourceType> getAllResourceTypes() {
		return resourceTypeDao.findAll();
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
	public List<QuantityType> getAllQuantityTypes() {
		return quantityTypeDao.findAll();
	}
	@Override
	public QuantityType findQuantityTypeById(String quantityTypeId) {
		return quantityTypeDao.findById(quantityTypeId).orElse(null);
	}
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
	public List<Country> getAllCountries() {
		return countryDao.findAll();
	}
	@Override
	public List<Cities> getCitiesByCountry(String countryId) {
		return citiesDao.findByCountryId(countryId);
	}

	@Override
	public AddressType findAddressTypeById(String addressTypeId) {
		return addressTypeDao.findById(addressTypeId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

	@Override
	public Country findCountryById(String countryId) {
		return countryDao.findById(countryId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

	@Override
	public Cities findCityById(String cityId) {
		return citiesDao.findById(cityId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

	@Override
	public IdentityType findIdentityTypeById(String identityId) {
		return identityTypeDao.findById(identityId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}

	@Override
	public TaxType findTaxTypeById(String taxTypeId) {
		return taxTypeDao.findById(taxTypeId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found", null,Locale.ENGLISH)));
	}

	@Override
	public TerritoryType findterritoryTypeId(String territoryTypeId) {
		return territoryTypeDao.findById(territoryTypeId).orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
	}
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
	public Country addCountry(Country country) {
		if (countryDao.existsByCountryIgnoreCaseAndActiveTrue(country.getCountry())
				|| countryDao.existsByCountryCodeIgnoreCaseAndActiveTrue(country.getCountryCode())) {
			throw new DuplicateEntryException(
					messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
			);
		}
		country.setId(UUID.randomUUID().toString());
		country.setActive(true);
		return countryDao.save(country);
	}

	@Override
	public void editCountry(Country country) {
		Country existing = countryDao.findById(country.getId())
				.orElseThrow(() -> new DataNotFoundException(
						messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
				));
		if (country.isActive()) {
			if (countryDao.existsByCountryIgnoreCaseAndActiveTrueAndIdNot(
					country.getCountry(), country.getId())
					|| countryDao.existsByCountryCodeIgnoreCaseAndActiveTrueAndIdNot(
					country.getCountryCode(), country.getId())) {
				throw new DuplicateEntryException(
						messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
				);
			}
		}
		existing.setCountry(country.getCountry());
		existing.setActive(country.isActive());
		existing.setCountryCode(country.getCountryCode());
		countryDao.save(existing);
	}
}
