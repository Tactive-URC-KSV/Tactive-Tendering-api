package com.ksv.tactive.service.implmn;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.ksv.tactive.dao.ScopeOfPackDao;
import com.ksv.tactive.dao.SectorDao;
import com.ksv.tactive.dao.UOMDao;
import com.ksv.tactive.entity.ScopeOfPackage;
import com.ksv.tactive.entity.Sector;
import com.ksv.tactive.entity.UOM;
import com.ksv.tactive.service.MasterDataService;

@Service
public class MasterServiceImpl implements MasterDataService {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private SectorDao sectorDao;

	@Autowired
	private ScopeOfPackDao scopeOfPackDao;

	@Autowired
	private UOMDao uomDao;

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
	public List<UOM> getAllUOMs() {
		return uomDao.findAll();
	}

	@Override
	public List<Sector> getAllSector() {
		return sectorDao.findAll();
	}

	@Override
	public List<ScopeOfPackage> getAllScopes() {
		return scopeOfPackDao.findAll();
	}

}
