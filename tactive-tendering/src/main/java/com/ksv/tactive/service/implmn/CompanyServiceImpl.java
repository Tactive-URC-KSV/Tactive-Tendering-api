package com.ksv.tactive.service.implmn;

import com.ksv.tactive.dataloader.LocationMemoryEngine;
import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.dao.CompanyDao;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.CompanyService;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.service.UserService;
import com.ksv.tactive.user.entity.User;
import com.ksv.tactive.util.*;
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

	@Autowired
	private UserService userService;

	@Autowired
	private LocationMemoryEngine locationMemoryEngine;

	@Value("${file.company.upload.path}")
	private String fileDir;

	@Override
	public void saveCompany(CompanySaveDTO dto, List<MultipartFile> files) {
		boolean isEdit = dto.getCompanyId() != null && !dto.getCompanyId().isBlank();
		Company company = buildCompany(dto);

		if (!"COMPANY".equalsIgnoreCase(dto.getComTypeId())) {

			if (company.getAddressDetails() == null) {
				company.setAddressDetails(new ArrayList<>());
			} else {
				company.getAddressDetails().clear();
			}
			company.getAddressDetails().addAll(buildAddress(company, dto));

			if (company.getContacts() == null) {
				company.setContacts(new ArrayList<>());
			} else {
				company.getContacts().clear();
			}
			company.getContacts().addAll(buildContacts(company, dto));

			if (company.getTaxDetails() == null) {
				company.setTaxDetails(new ArrayList<>());
			} else {
				company.getTaxDetails().clear();
			}
			company.getTaxDetails().addAll(buildTaxes(company, dto));

			if (company.getAdditionalInfos() == null) {
				company.setAdditionalInfos(new ArrayList<>());
			} else {
				company.getAdditionalInfos().clear();
			}
			company.getAdditionalInfos().addAll(buildAdditionalInfos(company, dto));

			if (company.getLocalNames() == null) {
				company.setLocalNames(new ArrayList<>());
			} else {
				company.getLocalNames().clear();
			}
			company.getLocalNames().addAll(buildLocalNames(company, dto));

			if (company.getDirectors() == null) {
				company.setDirectors(new ArrayList<>());
			} else {
				company.getDirectors().clear();
			}
			company.getDirectors().addAll(buildDirectors(company, dto));

			if (company.getJointVentures() == null) {
				company.setJointVentures(new ArrayList<>());
			} else {
				company.getJointVentures().clear();
			}
			company.getJointVentures().addAll(buildJointVentures(company, dto));

			if (company.getProfile() == null || company.getProfile().isEmpty()) {
				company.setProfile(buildProfile(company, dto, files));
			} else {
				CompanyProfile existingProfile = company.getProfile().get(0);
				CompanyProfile newProfile = buildProfile(company, dto, files).get(0);

				existingProfile.setAttachmentUrl(newProfile.getAttachmentUrl());
				existingProfile.setDescription(newProfile.getDescription());
				existingProfile.setOrderNo(newProfile.getOrderNo());
				existingProfile.setRemarks(newProfile.getRemarks());
			}
		}

		Company savedCompany = companyRepository.save(company);

		if (!isEdit) {
			createUser(savedCompany);
		}
	}

	private void createUser(Company savedCompany) {
		List<CompanyContact> contacts = savedCompany.getContacts();
		if (contacts == null) return;

		for (CompanyContact contact : contacts) {

			User existingUser = userService.findByEmail(contact.getEmail());
			if (existingUser != null) {
				continue;
			}
			User user = new User();
			user.setName(contact.getName());
			user.setEmail(contact.getEmail());
			user.setCompanyId(savedCompany.getId().toString());

			userService.addUser(user);
		}
	}
	private Company buildCompany(CompanySaveDTO dto) {
		Company company = (dto.getCompanyId() != null && !dto.getCompanyId().isBlank())
				? companyRepository.findById(UUID.fromString(dto.getCompanyId()))
				.orElseThrow(() -> new RuntimeException("Company not found"))
				: new Company();
		company.setCompanyName(dto.getCompanyName());
		company.setShortName(dto.getShortName());
		company.setFinStartMonth(dto.getFinStartMonth());
		if (isValid(dto.getComTypeId())) {
			company.setComType(CompanyType.valueOf(dto.getComTypeId().toUpperCase()));
		}
		if ("COMPANY".equalsIgnoreCase(dto.getComTypeId())) {
			if (isValid(dto.getComLevelId())) {
				company.setCompanyLevel(
						CompanyLevel.valueOf(dto.getComLevelId().toUpperCase())
				);
			}
			if (isValid(dto.getParentCompanyId())) {
				Company parent = companyRepository.findById(UUID.fromString(dto.getParentCompanyId()))
						.orElseThrow(() -> new RuntimeException("Parent company not found"));
				company.setParentCompany(parent);
			}
			return company;
		}
		if (isValid(dto.getComNatureId())) {
			company.setCompanyNature(
					CompanyNature.valueOf(dto.getComNatureId().toUpperCase())
			);
		}
		if (isValid(dto.getComLevelId())) {
			company.setCompanyLevel(
					CompanyLevel.valueOf(dto.getComLevelId().toUpperCase())
			);
		}
		if (isValid(dto.getBusinessNatureId())) {
			company.setBusinessNature(
					masterService.getBusinessNatureById(dto.getBusinessNatureId())
			);
		}
		if (isValid(dto.getCompanyConstitutionId())) {
			company.setCompanyConstitution(
					masterService.getCompanyConstitutionById(dto.getCompanyConstitutionId())
			);
		}
		if (isValid(dto.getCurrencyId())) {
			company.setCurrency(
					masterService.getCurrencyById(dto.getCurrencyId())
			);
		}
		if (isValid(dto.getStatusId())) {
			company.setCompanyStatus(
					CompanyStatus.valueOf(dto.getStatusId().toUpperCase())
			);
		}
		if (isValid(dto.getLanguageId())) {
			company.setLanguage(
					masterService.getLanguageById(dto.getLanguageId())
			);
		}
		return company;
	}
	private List<ComAddressDetails> buildAddress(Company company, CompanySaveDTO dto) {
		List<ComAddressDetails> addresses = new ArrayList<>();
		if (dto.getAddress() == null) return addresses;
		for (AddressDTO a : dto.getAddress()) {
			ComAddressDetails address = new ComAddressDetails();
			address.setAddress1(a.getAddress1());
			address.setAddress2(a.getAddress2());
			address.setZipcode(a.getZipcode());
			address.setEmail(a.getEmail());
			address.setPhone(a.getPhone());
			address.setWebsite(a.getWebsite());
			address.setFaxNo(a.getFaxNo());
			address.setPrimaryAddress(a.isPrimary());
			if (isValid(a.getCountryId())) {
				Country country = locationMemoryEngine.findAllCountries().stream()
						.filter(c -> c.getId().equals(a.getCountryId()))
						.findFirst()
						.orElse(null);
				if (country != null) {
					address.setCountry(country.getCountry());
				}
			}
			if (isValid(a.getCountryId()) && isValid(a.getStateId())) {
				States state = locationMemoryEngine
						.findStatesByCountryId(a.getCountryId())
						.stream()
						.filter(s -> s.getId().equals(a.getStateId()))
						.findFirst()
						.orElse(null);

				if (state != null) {
					address.setState(state.getState());
				}
			}
			if (isValid(a.getStateId()) && isValid(a.getCityId())) {
				Cities city = locationMemoryEngine
						.findCitiesByStateId(a.getStateId())
						.stream()
						.filter(c -> c.getId().equals(a.getCityId()))
						.findFirst()
						.orElse(null);

				if (city != null) {
					address.setCity(city.getCity());
				}
			}
			if (isValid(a.getAddressTypeId())) {
				address.setAddressType(
						masterService.findAddressTypeById(a.getAddressTypeId())
				);
			}
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

			contact.setName(c.getName());
			contact.setPhoneNo(c.getPhoneNo());
			contact.setEmail(c.getEmail());
			contact.setDesignation(masterService.getDesignationById(c.getPosition()));
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
			tax.setTaxType(TaxType.valueOf(t.getTaxTypeId().toUpperCase()));
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

			venture.setPartnerId(j.getPartnerId());
			venture.setSharePercentage(j.getSharePercentage());
			venture.setCompany(company);

			ventures.add(venture);
		}

		return ventures;
	}

	private List<CompanyProfile> buildProfile(Company company, CompanySaveDTO dto, List<MultipartFile> files) {

		List<CompanyProfile> profiles = new ArrayList<>();
		if (dto.getProfile() == null) return null;
		for (CompanyProfileDTO p : dto.getProfile()) {
			CompanyProfile profile = new CompanyProfile();
			profile.setCompany(company);
			profile.setDescription(p.getDescription());
			profile.setRemarks(p.getRemarks());
			profile.setOrderNo(p.getOrderNo());
			profile.setAttachmentUrl(saveDocuments(files));
			profiles.add(profile);
		}

		return profiles;
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
			map.put(company.getId().toString(), company.getCompanyName());

			return map;

		}).toList();
	}

	@Override
	public List<Company> getAllCompanyDetails() {
		return companyRepository.findAll();
	}
}