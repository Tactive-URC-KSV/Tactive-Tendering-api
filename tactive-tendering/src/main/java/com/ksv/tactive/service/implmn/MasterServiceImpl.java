package com.ksv.tactive.service.implmn;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
import com.ksv.tactive.master.dao.ListOfApprovalsDao;
import com.ksv.tactive.master.dao.ProjectStatusDao;
import com.ksv.tactive.master.dao.QuantityTypeDao;
import com.ksv.tactive.master.dao.RegionDao;
import com.ksv.tactive.master.dao.ResourceDao;
import com.ksv.tactive.master.dao.ResourceNatureDao;
import com.ksv.tactive.master.dao.ResourceTypeDao;
import com.ksv.tactive.master.dao.ScopeOfPackDao;
import com.ksv.tactive.master.dao.SectorDao;
import com.ksv.tactive.master.dao.UOMDao;
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
}
