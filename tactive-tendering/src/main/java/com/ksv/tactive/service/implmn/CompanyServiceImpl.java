package com.ksv.tactive.service.implmn;
import com.ksv.tactive.company.dao.ComapanyDao;
import com.ksv.tactive.company.entity.*;
import com.ksv.tactive.dto.*;
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

	@Override
	public void saveCompany(CompanySaveDTO dto, List<MultipartFile> files) {

		Company company = dto.getCompanyId() != null
				? companyRepository.findById(dto.getCompanyId())
				.orElseThrow(() -> new RuntimeException("Company not found"))
				: new Company();

		if (company.getId() == null) {
			company.setId(UUID.randomUUID().toString());
		}

		/* ===== BASIC INFO ===== */
		company.setCompanyName(dto.getCompanyName());
		company.setShortName(dto.getShortName());
		company.setComTypeId(dto.getComTypeId());
		company.setComLevelId(dto.getComLevelId());
		company.setComNatureId(dto.getComNatureId());
		company.setBusinessNatureId(dto.getBusinessNatureId());
		company.setCompanyConstitutionId(dto.getCompanyConstitutionId());
		company.setStatusId(dto.getStatusId());
		company.setCurrencyId(dto.getCurrencyId());
		company.setBank(dto.getBank());

		/* ===== ADDRESS ===== */
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

		/* ===== CONTACTS ===== */
		company.getContacts().clear();
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

		/* ===== TAX DETAILS ===== */
		company.getTaxDetails().clear();
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

		/* ===== DIRECTORS ===== */
		company.getDirectors().clear();
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
		/* ===== COMPANY PROFILE ===== */
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
		/* ===== ADDITIONAL INFO ===== */
		company.getAdditionalInfos().clear();
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
		/* ===== JOINT VENTURES ===== */
		company.getJointVentures().clear();
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
		/* ===== LOCAL NAMES ===== */
		company.getLocalNames().clear();
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
		Path directory = Paths.get(fileDir);
		try {
			Files.createDirectories(directory);
			for(MultipartFile file:files){
				Path targetLoc = directory.resolve(StringUtils.cleanPath(file.getOriginalFilename()));
				Files.copy(file.getInputStream(), targetLoc, StandardCopyOption.REPLACE_EXISTING);
				paths.add(targetLoc.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return paths;
	}
}