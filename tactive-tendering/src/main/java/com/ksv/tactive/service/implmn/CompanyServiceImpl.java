package com.ksv.tactive.service.implmn;
import com.ksv.tactive.project.dao.ComapanyDao;
import com.ksv.tactive.dto.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.util.CompanyType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements CompanyService {

	@Autowired
	private ComapanyDao companyRepository;
	@Value("${file.company.upload.path}")
	private String fileDir;

	@Autowired
	private MasterService masterService;

	@Override
	public void saveCompany(CompanySaveDTO dto, List<MultipartFile> files) {

		Company company = dto.getCompanyId() != null
				? companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new RuntimeException("Company not found"))
				: new Company();

		if (company.getId() == null) {
			company.setId(UUID.randomUUID().toString());
		}
		company.setCompanyName(dto.getCompanyName());
		company.setShortName(dto.getShortName());
		CompanyType comType = null;
		if (dto.getComTypeId() != null && !dto.getComTypeId().isBlank()) {
			comType = CompanyType.valueOf(dto.getComTypeId().trim().toUpperCase());
		}
		company.setComType(comType);
		company.setCompanyLevel(masterService.getCompanyLevelById(dto.getComLevelId()));
		company.setCompanyNature(masterService.getCompanyNatureById(dto.getComNatureId()));
		company.setBusinessNature(masterService.getBusinessNatureById(dto.getBusinessNatureId()));
		company.setCompanyConstitution(masterService.getCompanyConstitutionById(dto.getCompanyConstitutionId()));
		company.setCompanyStatus(masterService.getCompanyStatusById(dto.getStatusId()));
		company.setCurrency(masterService.getCurrencyById(dto.getCurrencyId()));
		company.setBank(dto.getBank());
		if (dto.getAddress() != null) {
			ComAddressDetails address = new ComAddressDetails();
			address.setId(
					dto.getAddress().getId() != null
							? dto.getAddress().getId()
							: UUID.randomUUID().toString()
			);
			address.setAddress1(dto.getAddress().getAddress1());
			address.setAddress2(dto.getAddress().getAddress2());
			address.setZipcode(dto.getAddress().getZipcode());
			address.setEmail(dto.getAddress().getEmail());
			address.setWebsite(dto.getAddress().getWebsite());
			address.setFaxNo(dto.getAddress().getFaxNo());
			address.setPhone(dto.getAddress().getPhone());
			address.setAddressTypeId(dto.getAddress().getAddressTypeId());
			address.setCompany(company);

			company.setAddressDetails(address);
		}
		if (dto.getContacts() != null) {
			List<CompanyContact> contacts = new ArrayList<>();
			for (ContactDTO c : dto.getContacts()) {
				CompanyContact contact = new CompanyContact();
				contact.setId(c.getId() != null ? c.getId() : UUID.randomUUID().toString());
				contact.setPosition(c.getPosition());
				contact.setName(c.getName());
				contact.setPhoneNo(c.getPhoneNo());
				contact.setEmail(c.getEmail());
				contact.setCompany(company);
				contacts.add(contact);
			}
			company.setContacts(contacts);
		}
		if (dto.getTaxDetails() != null) {
			List<CompanyTax> taxes = new ArrayList<>();
			for (TaxDTO t : dto.getTaxDetails()) {
				CompanyTax tax = new CompanyTax();
				tax.setId(t.getId() != null ? t.getId() : UUID.randomUUID().toString());
				tax.setTaxTypeId(t.getTaxTypeId());
				tax.setTerritoryTypeId(t.getTerritoryTypeId());
				tax.setTerritory(t.getTerritory());
				tax.setTaxRegNo(t.getTaxRegNo());
				tax.setTaxRegDate(t.getTaxRegDate());
				tax.setEffectiveFrom(t.getEffectiveFrom());
				tax.setEffectiveTo(t.getEffectiveTo());
				tax.setPrimary(t.isPrimary());
				tax.setCompany(company);
				taxes.add(tax);
			}
			company.setTaxDetails(taxes);
		}
		if (dto.getDirectors() != null) {
			List<CompanyDirector> directors = new ArrayList<>();
			for (DirectorDTO d : dto.getDirectors()) {
				CompanyDirector director = new CompanyDirector();
				director.setId(d.getId() != null ? d.getId() : UUID.randomUUID().toString());
				director.setDirectorTypeId(d.getDirectorTypeId());
				director.setDirectorName(d.getDirectorName());
				director.setSharePercentage(d.getSharePercentage());
				director.setNoOfShares(d.getNoOfShares());
				director.setCompany(company);
				directors.add(director);
			}
			company.setDirectors(directors);
		}
		if (dto.getProfile() != null) {
			CompanyProfile profile = new CompanyProfile();
			profile.setId(
					dto.getProfile().getId() != null
							? dto.getProfile().getId()
							: UUID.randomUUID().toString()
			);
			profile.setOrderNo(dto.getProfile().getOrderNo());
			profile.setDescription(dto.getProfile().getDescription());
			profile.setRemarks(dto.getProfile().getRemarks());
			profile.setCompany(company);
			List<String> attachmentUrl = saveDocuments(files);
			profile.setAttachmentUrl(attachmentUrl);
			company.setProfile(profile);
		}
		if (dto.getAdditionalInfos() != null) {
			List<CompanyAdditionalInfo> infos = new ArrayList<>();
			for (AdditionalInfoDTO a : dto.getAdditionalInfos()) {
				CompanyAdditionalInfo info = new CompanyAdditionalInfo();
				info.setId(a.getId() != null ? a.getId() : UUID.randomUUID().toString());
				info.setIdTypeId(a.getIdTypeId());
				info.setRegistrationNo(a.getRegistrationNo());
				info.setCompany(company);
				infos.add(info);
			}
			company.setAdditionalInfos(infos);
		}
		if (dto.getJointVentures() != null) {
			List<CompanyJointVenture> ventures = new ArrayList<>();
			for (JointVentureDTO j : dto.getJointVentures()) {
				CompanyJointVenture venture = new CompanyJointVenture();
				venture.setId(j.getId() != null ? j.getId() : UUID.randomUUID().toString());
				venture.setPartnerId(j.getPartnerId());
				venture.setSharePercentage(j.getSharePercentage());
				venture.setCompany(company);
				ventures.add(venture);
			}
			company.setJointVentures(ventures);
		}
		if (dto.getLocalNames() != null) {
			List<CompanyLocalName> localNames = new ArrayList<>();
			for (LocalNameDTO l : dto.getLocalNames()) {
				CompanyLocalName local = new CompanyLocalName();
				local.setId(l.getId() != null ? l.getId() : UUID.randomUUID().toString());
				local.setLanguageId(l.getLanguageId());
				local.setName(l.getName());
				local.setCompany(company);
				localNames.add(local);
			}
			company.setLocalNames(localNames);
		}
		companyRepository.save(company);
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
				Path targetLocation = directory.resolve(filename);
				Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
				paths.add(targetLocation.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not save file", e);
		}
		return paths;
	}
}