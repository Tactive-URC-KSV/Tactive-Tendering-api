package com.ksv.tactive.service.implmn;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.dao.CompanyDao;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.CompanyService;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.util.CompanyType;
import com.ksv.tactive.util.DirectorType;
import com.ksv.tactive.util.TerritoryType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

	@Autowired
	private CompanyDao companyRepository;

	@Autowired
	private MasterService masterService;

	@Value("${file.company.upload.path}")
	private String fileDir;

	@Override
	public void saveCompany(CompanySaveDTO dto, List<MultipartFile> files) {

		Company company = buildCompany(dto);

		company.setAddressDetails(buildAddress(company, dto));
		company.setContacts(buildContacts(company, dto));
		company.setTaxDetails(buildTaxes(company, dto));
		company.setAdditionalInfos(buildAdditionalInfos(company, dto));
		company.setLocalNames(buildLocalNames(company, dto));
		company.setDirectors(buildDirectors(company, dto));
		company.setJointVentures(buildJointVentures(company, dto));
		company.setProfile(buildProfile(company, dto, files));

		companyRepository.save(company);
	}

	private Company buildCompany(CompanySaveDTO dto) {

		Company company = (dto.getCompanyId() != null && !dto.getCompanyId().isBlank())
				? companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new RuntimeException("Company not found"))
				: new Company();

		if (company.getId() == null) {
			company.setId(UUID.randomUUID().toString());
		}

		company.setCompanyName(dto.getCompanyName());
		company.setShortName(dto.getShortName());

		company.setComType(isValid(dto.getComTypeId())
				? CompanyType.valueOf(dto.getComTypeId().toUpperCase())
				: null);

		company.setCompanyLevel(isValid(dto.getComLevelId())
				? masterService.getCompanyLevelById(dto.getComLevelId())
				: null);

		company.setCompanyNature(isValid(dto.getComNatureId())
				? masterService.getCompanyNatureById(dto.getComNatureId())
				: null);

		company.setBusinessNature(isValid(dto.getBusinessNatureId())
				? masterService.getBusinessNatureById(dto.getBusinessNatureId())
				: null);

		company.setCompanyConstitution(isValid(dto.getCompanyConstitutionId())
				? masterService.getCompanyConstitutionById(dto.getCompanyConstitutionId())
				: null);

		company.setCompanyStatus(isValid(dto.getStatusId())
				? masterService.getCompanyStatusById(dto.getStatusId())
				: null);

		company.setCurrency(isValid(dto.getCurrencyId())
				? masterService.getCurrencyById(dto.getCurrencyId())
				: null);

		company.setLanguage(isValid(dto.getLanguageId())
				? masterService.getLanguageById(dto.getLanguageId())
				: null);

		company.setBank(dto.getBank());

		return company;
	}

	private List<ComAddressDetails> buildAddress(Company company, CompanySaveDTO dto) {

		List<ComAddressDetails> addresses = new ArrayList<>();
		if (dto.getAddress() == null) return addresses;
		for (AddressDTO a : dto.getAddress()) {
			ComAddressDetails address = new ComAddressDetails();
			address.setId(UUID.randomUUID().toString());
			address.setAddress1(a.getAddress1());
			address.setAddress2(a.getAddress2());
			address.setZipcode(a.getZipcode());
			address.setEmail(a.getEmail());
			address.setPhone(a.getPhone());
			address.setWebsite(a.getWebsite());
			address.setPrimary(a.isPrimary());
			address.setFaxNo(a.getFaxNo());
			address.setAddressType(
					isValid(a.getAddressTypeId())
							? masterService.findAddressTypeById(a.getAddressTypeId())
							: null
			);
			address.setCompany(company);
			addresses.add(address);
		}
		return addresses;
	}

	private List<CompanyContact> buildContacts(Company company, CompanySaveDTO dto) {

		List<CompanyContact> contacts = new ArrayList<>();

		if (dto.getContacts() == null) return contacts;

		for (ContactDTO c : dto.getContacts()) {

			CompanyContact contact = new CompanyContact();

			contact.setId(UUID.randomUUID().toString());
			contact.setName(c.getName());
			contact.setPhoneNo(c.getPhoneNo());
			contact.setEmail(c.getEmail());
			contact.setPosition(c.getPosition());
			contact.setCompany(company);

			contacts.add(contact);
		}

		return contacts;
	}

	private List<CompanyTax> buildTaxes(Company company, CompanySaveDTO dto) {

		List<CompanyTax> taxes = new ArrayList<>();

		if (dto.getTaxDetails() == null) return taxes;

		for (TaxDTO t : dto.getTaxDetails()) {

			CompanyTax tax = new CompanyTax();

			tax.setId(UUID.randomUUID().toString());

			tax.setTaxType(isValid(t.getTaxTypeId())
					? masterService.findTaxTypeById(t.getTaxTypeId())
					: null);

			tax.setTerritoryTypeId(isValid(t.getTerritoryTypeId())
					? TerritoryType.valueOf(t.getTerritoryTypeId().toUpperCase()).name()
					: null);

			tax.setTerritory(t.getTerritory());
			tax.setTaxRegNo(t.getTaxRegNo());
			tax.setTaxRegDate(t.getTaxRegDate());
			tax.setEffectiveFrom(t.getEffectiveFrom());
			tax.setEffectiveTo(t.getEffectiveTo());
			tax.setAddress1(t.getAddress1());
			tax.setAddress2(t.getAddress2());
			tax.setCity(t.getCity());
			tax.setPinCode(t.getPinCode());
			tax.setEmail(t.getEmail());
			tax.setCompany(company);

			taxes.add(tax);
		}

		return taxes;
	}

	private List<CompanyAdditionalInfo> buildAdditionalInfos(Company company, CompanySaveDTO dto) {

		List<CompanyAdditionalInfo> infos = new ArrayList<>();

		if (dto.getAdditionalInfos() == null) return infos;

		for (AdditionalInfoDTO a : dto.getAdditionalInfos()) {

			CompanyAdditionalInfo info = new CompanyAdditionalInfo();

			info.setId(UUID.randomUUID().toString());

			info.setIdentityType(isValid(a.getIdTypeId())
					? masterService.findIdentityTypeById(a.getIdTypeId())
					: null);

			info.setRegistrationNo(a.getRegistrationNo());
			info.setCompany(company);

			infos.add(info);
		}

		return infos;
	}

	private List<CompanyLocalName> buildLocalNames(Company company, CompanySaveDTO dto) {

		List<CompanyLocalName> locals = new ArrayList<>();

		if (dto.getLocalNames() == null) return locals;

		for (LocalNameDTO l : dto.getLocalNames()) {

			CompanyLocalName local = new CompanyLocalName();

			local.setId(UUID.randomUUID().toString());
			local.setLanguage(isValid(l.getLanguageId())
					? masterService.getLanguageById(l.getLanguageId())
					: null);

			local.setName(l.getName());
			local.setCompany(company);

			locals.add(local);
		}

		return locals;
	}

	private List<CompanyDirector> buildDirectors(Company company, CompanySaveDTO dto) {

		List<CompanyDirector> directors = new ArrayList<>();

		if (dto.getDirectors() == null) return directors;

		for (DirectorDTO d : dto.getDirectors()) {

			CompanyDirector director = new CompanyDirector();

			director.setId(UUID.randomUUID().toString());

			director.setDirectorType(isValid(d.getDirectorTypeId())
					? DirectorType.valueOf(d.getDirectorTypeId().toUpperCase())
					: null);

			director.setDirectorName(d.getDirectorName());
			director.setSharePercentage(d.getSharePercentage());
			director.setNoOfShares(d.getNoOfShares());
			director.setCompany(company);

			directors.add(director);
		}

		return directors;
	}

	private List<CompanyJointVenture> buildJointVentures(Company company, CompanySaveDTO dto) {

		List<CompanyJointVenture> ventures = new ArrayList<>();

		if (dto.getJointVentures() == null) return ventures;

		for (JointVentureDTO j : dto.getJointVentures()) {

			CompanyJointVenture venture = new CompanyJointVenture();

			venture.setId(UUID.randomUUID().toString());
			venture.setPartnerId(j.getPartnerId());
			venture.setSharePercentage(j.getSharePercentage());
			venture.setCompany(company);

			ventures.add(venture);
		}

		return ventures;
	}

	private CompanyProfile buildProfile(Company company, CompanySaveDTO dto, List<MultipartFile> files) {

		if (dto.getProfile() == null) return null;

		CompanyProfile profile = new CompanyProfile();

		profile.setId(UUID.randomUUID().toString());
		profile.setOrderNo(dto.getProfile().getOrderNo());
		profile.setDescription(dto.getProfile().getDescription());
		profile.setRemarks(dto.getProfile().getRemarks());
		profile.setAttachmentUrl(saveDocuments(files));
		profile.setCompany(company);

		return profile;
	}

	private boolean isValid(String value) {
		return value != null && !value.trim().isBlank();
	}

	private List<String> saveDocuments(List<MultipartFile> files) {

		List<String> paths = new ArrayList<>();

		if (files == null || files.isEmpty()) return paths;

		Path directory = Paths.get(fileDir);

		try {

			Files.createDirectories(directory);

			for (MultipartFile file : files) {

				if (file.isEmpty()) continue;

				String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
				String uniqueFilename = UUID.randomUUID() + "_" + filename;

				Path targetLocation = directory.resolve(uniqueFilename);

				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

				paths.add(targetLocation.toString());
			}

		} catch (IOException e) {

			throw new RuntimeException("File upload failed", e);
		}

		return paths;
	}

	@Override
	public List<Map<String, String>> getAllCompany() {

		List<Company> companies = companyRepository.findAll();

		return companies.stream().map(company -> {

			Map<String, String> map = new HashMap<>();
			map.put(company.getId(), company.getCompanyName());

			return map;

		}).toList();
	}

	@Override
	public List<Company> getAllCompanyDetails() {
		return companyRepository.findAll();
	}
}