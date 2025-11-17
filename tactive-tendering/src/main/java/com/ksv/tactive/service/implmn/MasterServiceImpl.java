package com.ksv.tactive.service.implmn;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.project.dao.CostCodeTypeDao;
import com.ksv.tactive.project.dao.ListOfApprovalsDao;
import com.ksv.tactive.project.dao.ProjectCurrencyDao;
import com.ksv.tactive.project.dao.ProjectStatusDao;
import com.ksv.tactive.project.dao.QuantityTypeDao;
import com.ksv.tactive.project.dao.RegionDao;
import com.ksv.tactive.project.dao.ResourceNatureDao;
import com.ksv.tactive.project.dao.ResourceDao;
import com.ksv.tactive.project.dao.ResourceTypeDao;
import com.ksv.tactive.project.dao.ScopeOfPackDao;
import com.ksv.tactive.project.dao.SectorDao;
import com.ksv.tactive.project.dao.UOMDao;
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
	private ProjectCurrencyDao currencyDao;
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
	public List<Sector> getAllSector() {
		return sectorDao.findAll();
	}
	@Override
	public List<ScopeOfPackage> getAllScopes() {
		return scopeOfPackDao.findAll();
	}
	@Override
	public List<ProjectStatus> getAllProjectStatus() {
		return statusDao.findAll();
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
	public List<Resources> getAllResources() {
		return resourceDao.findAll();
	}
	@Override
	public List<QuantityType> getAllQuantityTypes() {
		return quantityTypeDao.findAll();
	}
	@Override
	public List<ResourceNature> getAllResourceNatures() {
		return resouceNatureDao.findAll();
	}
	@Override
	public List<Currency> getAllCurrencies() {
		return currencyDao.findAll();
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
}
