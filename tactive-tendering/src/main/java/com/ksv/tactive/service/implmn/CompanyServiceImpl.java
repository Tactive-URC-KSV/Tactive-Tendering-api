package com.ksv.tactive.service.implmn;

import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.util.CompanyType;
import com.ksv.tactive.util.DirectorType;
import com.ksv.tactive.util.TerritoryType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ksv.tactive.service.CompanyService;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class CompanyServiceImpl implements CompanyService {

	@Autowired
	private CompanyDao companyRepository;

	@Value("${file.company.upload.path}")
	private String fileDir;

	@Autowired
	private MasterService masterService;
	@Autowired
	private CompanyAdditionalInfoDao companyAdditionalInfoDao;
	@Autowired
	private CompanyContactDao companyContactDao;
	@Autowired
	private CompanyTaxDao companyTaxDao;
	@Autowired
	private CompanyLocalNameDao companyLocalNameDao;
	@Autowired
	private CompanyDirectorDao companyDirectorDao;
	@Autowired
	private CompanyJointVenturesDao companyJointVenturesDao;
	@Autowired
	private ComAddressDetailsDao comAddressDetailsDao;
	@Autowired
	private CompanyProfileDao companyProfileDao;

	@Override
	@Transactional
	public void saveCompany(CompanySaveDTO dto,List<MultipartFile> files){
		Company company=saveCompanyCore(dto);
		saveAddress(company,dto);
		saveContacts(company,dto);
		saveTaxes(company,dto);
		saveAdditionalInfos(company,dto);
		saveProfile(company,dto,files);
		saveLocalNames(company,dto);
		saveDirectors(company,dto);
		saveJointVentures(company,dto);
		companyRepository.save(company);
	}
	private Company saveCompanyCore(CompanySaveDTO dto){
		Company company=(dto.getCompanyId()!=null&&!dto.getCompanyId().isBlank())
				?companyRepository.findById(dto.getCompanyId()).orElseThrow(()->new RuntimeException("Company not found"))
				:new Company();
		if(company.getId()==null){company.setId(UUID.randomUUID().toString());}
		company.setCompanyName(dto.getCompanyName());
		company.setShortName(dto.getShortName());
		company.setComType(isValid(dto.getComTypeId())?CompanyType.valueOf(dto.getComTypeId().trim().toUpperCase()):null);
		company.setCompanyLevel(isValid(dto.getComLevelId())?masterService.getCompanyLevelById(dto.getComLevelId().trim()):null);
		company.setCompanyNature(isValid(dto.getComNatureId())?masterService.getCompanyNatureById(dto.getComNatureId().trim()):null);
		company.setBusinessNature(isValid(dto.getBusinessNatureId())?masterService.getBusinessNatureById(dto.getBusinessNatureId().trim()):null);
		company.setCompanyConstitution(isValid(dto.getCompanyConstitutionId())?masterService.getCompanyConstitutionById(dto.getCompanyConstitutionId().trim()):null);
		company.setCompanyStatus(isValid(dto.getStatusId())?masterService.getCompanyStatusById(dto.getStatusId().trim()):null);
		company.setCurrency(isValid(dto.getCurrencyId())?masterService.getCurrencyById(dto.getCurrencyId().trim()):null);
		company.setLanguage(isValid(dto.getLanguageId())?masterService.getLanguageById(dto.getLanguageId().trim()):null);
		company.setParentCompany(isValid(dto.getParentCompanyId())?companyRepository.findById(dto.getParentCompanyId())
				.orElseThrow(()->new RuntimeException("Company not found")):null);
		company.setBank(dto.getBank());
		company = companyRepository.save(company);
		return company;
	}
	private void saveAddress(Company company,CompanySaveDTO dto){
		if(dto.getAddress()!=null){
			ComAddressDetails address=company.getAddressDetails()!=null?company.getAddressDetails():new ComAddressDetails();
			if(address.getId()==null){address.setId(UUID.randomUUID().toString());}
			address.setAddress1(dto.getAddress().getAddress1());
			address.setAddress2(dto.getAddress().getAddress2());
			address.setZipcode(dto.getAddress().getZipcode());
			address.setEmail(dto.getAddress().getEmail());
			address.setWebsite(dto.getAddress().getWebsite());
			address.setFaxNo(dto.getAddress().getFaxNo());
			address.setPhone(dto.getAddress().getPhone());
			address.setAddressType(isValid(dto.getAddress().getAddressTypeId())
					?masterService.findAddressTypeById(dto.getAddress().getAddressTypeId().trim())
					:null);
			address.setCompany(company);
			company.setAddressDetails(comAddressDetailsDao.save(address));
		}else{
			company.setAddressDetails(null);
		}
	}
	private void saveContacts(Company company,CompanySaveDTO dto){
		if(company.getContacts()==null){company.setContacts(new ArrayList<>());}else{company.getContacts().clear();}
		if(dto.getContacts()!=null){
			for(ContactDTO c:dto.getContacts()){
				CompanyContact contact=(c.getId()!=null)
						?companyContactDao.findById(c.getId()).orElseThrow(()->new RuntimeException("Contact not found"))
						:new CompanyContact();
				if(contact.getId()==null){contact.setId(UUID.randomUUID().toString());}
				contact.setPosition(c.getPosition());
				contact.setName(c.getName());
				contact.setPhoneNo(c.getPhoneNo());
				contact.setEmail(c.getEmail());
				contact.setCompany(company);
				company.getContacts().add(companyContactDao.save(contact));
			}}
	}
	private void saveTaxes(Company company,CompanySaveDTO dto){
		if(company.getTaxDetails()==null){company.setTaxDetails(new ArrayList<>());}else{company.getTaxDetails().clear();}
		if(dto.getTaxDetails()!=null){
			for(TaxDTO t:dto.getTaxDetails()){
				CompanyTax tax=(t.getId()!=null)
						?companyTaxDao.findById(t.getId()).orElseThrow(()->new RuntimeException("Tax not found"))
						:new CompanyTax();
				if(tax.getId()==null){tax.setId(UUID.randomUUID().toString());}
				tax.setTaxType(isValid(t.getTaxTypeId())?masterService.findTaxTypeById(t.getTaxTypeId().trim()):null);
				tax.setTerritoryTypeId(isValid(t.getTerritoryTypeId())
						?TerritoryType.valueOf(t.getTerritoryTypeId().trim().toUpperCase()).name()
						:null);
				tax.setTerritory(t.getTerritory());
				tax.setTaxRegNo(t.getTaxRegNo());
				tax.setTaxRegDate(t.getTaxRegDate());
				tax.setEffectiveFrom(t.getEffectiveFrom());
				tax.setEffectiveTo(t.getEffectiveTo());
				tax.setPrimary(t.isPrimary());
				tax.setAddress1(t.getAddress1());
				tax.setAddress2(t.getAddress2());
				tax.setCity(t.getCity());
				tax.setPinCode(t.getPinCode());
				tax.setEmail(t.getEmail());
				tax.setPrimary(t.isPrimary());
				tax.setCompany(company);
				company.getTaxDetails().add(companyTaxDao.save(tax));
			}}
	}
	private void saveAdditionalInfos(Company company,CompanySaveDTO dto){
		if(company.getAdditionalInfos()==null){company.setAdditionalInfos(new ArrayList<>());}else{company.getAdditionalInfos().clear();}
		if(dto.getAdditionalInfos()!=null){
			for(AdditionalInfoDTO a:dto.getAdditionalInfos()){
				CompanyAdditionalInfo info=(a.getId()!=null)
						?companyAdditionalInfoDao.findById(a.getId())
						.orElseThrow(()->new RuntimeException("CompanyAdditionalInfo not found: "+a.getId()))
						:new CompanyAdditionalInfo();
				if(info.getId()==null){info.setId(UUID.randomUUID().toString());}
				info.setIdentityType(isValid(a.getIdTypeId())
						?masterService.findIdentityTypeById(a.getIdTypeId().trim())
						:null);
				info.setRegistrationNo(a.getRegistrationNo());
				info.setCompany(company);
				company.getAdditionalInfos().add(companyAdditionalInfoDao.save(info));
			}}
	}
	private void saveProfile(Company company,CompanySaveDTO dto,List<MultipartFile> files){
		if(dto.getProfile()!=null){
			CompanyProfile profile=company.getProfile()!=null?company.getProfile():new CompanyProfile();
			if(profile.getId()==null){profile.setId(UUID.randomUUID().toString());}
			profile.setOrderNo(dto.getProfile().getOrderNo());
			profile.setDescription(dto.getProfile().getDescription());
			profile.setRemarks(dto.getProfile().getRemarks());
			profile.setCompany(company);
			if(files!=null&&!files.isEmpty()){profile.setAttachmentUrl(saveDocuments(files));}
			company.setProfile(companyProfileDao.save(profile));
		}else{
			company.setProfile(null);
		}
	}
	private void saveLocalNames(Company company,CompanySaveDTO dto){
		if(company.getLocalNames()==null){
			company.setLocalNames(new ArrayList<>());
		}else{
			company.getLocalNames().clear();
		}
		if(dto.getLocalNames()!=null){
			for(LocalNameDTO l:dto.getLocalNames()){
				CompanyLocalName local=(l.getId()!=null)
						?companyLocalNameDao.findById(l.getId())
						.orElseThrow(()->new RuntimeException("LocalName not found: "+l.getId()))
						:new CompanyLocalName();
				if(local.getId()==null){
					local.setId(UUID.randomUUID().toString());
				}
				local.setLanguage(
						isValid(l.getLanguageId())
								?masterService.getLanguageById(l.getLanguageId().trim())
								:null
				);
				local.setName(l.getName());
				local.setCompany(company);
				company.getLocalNames().add(companyLocalNameDao.save(local));
			}}
	}
	private void saveDirectors(Company company,CompanySaveDTO dto){
		if(company.getDirectors()==null){
			company.setDirectors(new ArrayList<>());
		}else{
			company.getDirectors().clear();
		}
		if(dto.getDirectors()!=null){
			for(DirectorDTO d:dto.getDirectors()){
				CompanyDirector director=(d.getId()!=null)
						?companyDirectorDao.findById(d.getId())
						.orElseThrow(()->new RuntimeException("Director not found: "+d.getId()))
						:new CompanyDirector();
				if(director.getId()==null){
					director.setId(UUID.randomUUID().toString());
				}
				director.setDirectorType(
						isValid(d.getDirectorTypeId())
								?DirectorType.valueOf(d.getDirectorTypeId().trim().toUpperCase())
								:null
				);
				director.setDirectorName(d.getDirectorName());
				director.setSharePercentage(d.getSharePercentage());
				director.setNoOfShares(d.getNoOfShares());
				director.setCompany(company);
				company.getDirectors().add(companyDirectorDao.save(director));
			}}
	}
	private void saveJointVentures(Company company,CompanySaveDTO dto){
		if(company.getJointVentures()==null){
			company.setJointVentures(new ArrayList<>());
		}else{
			company.getJointVentures().clear();
		}
		if(dto.getJointVentures()!=null){
			for(JointVentureDTO j:dto.getJointVentures()){
				CompanyJointVenture venture=(j.getId()!=null)
						?companyJointVenturesDao.findById(j.getId())
						.orElseThrow(()->new RuntimeException("JointVenture not found: "+j.getId()))
						:new CompanyJointVenture();
				if(venture.getId()==null){
					venture.setId(UUID.randomUUID().toString());
				}
				venture.setPartnerId(j.getPartnerId());
				venture.setSharePercentage(j.getSharePercentage());
				venture.setCompany(company);
				company.getJointVentures().add(companyJointVenturesDao.save(venture));
			}}
	}

	private boolean isValid(String value) {
		return value != null && !value.trim().isBlank();
	}
	private List<String> saveDocuments(List<MultipartFile> files) {
		List<String> paths = new ArrayList<>();
		if (files == null || files.isEmpty()) {
			return paths;
		}
		Path directory = Paths.get(fileDir);
		try {
			Files.createDirectories(directory);
			for (MultipartFile file : files) {
				if (file.isEmpty()) continue;
				String filename = StringUtils.cleanPath(file.getOriginalFilename());
				String uniqueFilename = UUID.randomUUID() + "_" + filename;
				Path targetLocation = directory.resolve(uniqueFilename);
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				paths.add(targetLocation.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not save file", e);
		}
		return paths;
	}

	@Override
	public List<Map<String, String>> getAllCompany() {
		List<Map<String, String>> companyList = new ArrayList<>();
		List<Company> companies = companyRepository.findAll();
		if(companies!=null && companies.size()>0){
			companyList = companies.stream()
					.map(company -> {
						Map<String, String> companyMap = new HashMap<>();
						companyMap.put(company.getId(), company.getCompanyName());
						return companyMap;
					})
					.toList();
		}
		throw new DataNotFoundException("Company not found");
	}
}