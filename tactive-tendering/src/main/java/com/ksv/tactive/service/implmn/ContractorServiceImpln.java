package com.ksv.tactive.service.implmn;

import com.ksv.tactive.config.UserPrincipal;
import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.dto.ContractorNatureDto;
import com.ksv.tactive.dto.ContractorStatusDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.exception.FormAlreadySubmittedException;
import com.ksv.tactive.location.entity.Cities;
import com.ksv.tactive.location.entity.Country;
import com.ksv.tactive.project.entity.AddressType;
import com.ksv.tactive.project.entity.IdentityType;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.ContractorService;
import com.ksv.tactive.service.EmailService;
import com.ksv.tactive.service.MasterService;
import com.ksv.tactive.service.UserService;
import com.ksv.tactive.user.entity.User;
import com.ksv.tactive.util.ContractorStatus;
import com.ksv.tactive.util.TerritoryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractorServiceImpln implements ContractorService {

    @Autowired
    private MasterService masterService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContractorDao contractorDao;

    @Value("${file.contractor.upload.path}")
    private String fileDir;

    @Autowired
    private ContractorTypeDao conTypeDao;

    @Autowired
    private ContractorNatureDao conNatureDao;

    @Autowired
    private ContractorGradeDao conGradeDao;

    @Autowired
    private ContractorContactsDao contactsDao;

    @Autowired
    private ContractorAddressDao contractorAddressDao;

    @Autowired
    private ContractorTaxDetailsDao taxDetailsDao;

    @Autowired
    private ContractorBankDetailsDao bankDetailsDao;

    @Autowired
    private ContractorAddInfoDao addInfoDao;

    @Autowired
    private ContractorFormInviteDao contractorFormInviteDao;

    @Autowired
    private MessageSource messageSource;

    @Override
    public List<Map<String, Object>>getAllContractor() {
        List<Contractor> contractors = contractorDao.findAll();
        List<Map<String, Object>> contractorDto = new ArrayList<>();
        for(Contractor con : contractors){
            Map<String, Object> contractor = new HashMap<>();
            contractor.put("contractor",con);
            ContractorContacts contact = contactsDao.findByContractorId(con.getId());
            contractor.put("contact",contact);
            contractorDto.add(contractor);
        }
        return contractorDto;
    }

    @Override
    public String addContractor(ContractorInputDto contractorInputDto, ContractorAddress contractorAddress,
                                ContractorContacts contractorContacts, ContractorTaxDetails contractorTaxDetails,
                                ContractorBankDetails contractorBankDetails, ContractorAddInfo contractorAddInfo) {
        Contractor contractor = new Contractor();
        if(contractorInputDto.getId() == null){
            contractor.setId(UUID.randomUUID().toString());
        }
        else{
            contractor.setId(contractorInputDto.getId());
        }
        contractor.setEntityCode(contractorInputDto.getEntityCode());
        contractor.setEntityName(contractorInputDto.getEntityName());
        contractor.setEffectiveDate(contractorInputDto.getEffectiveDate());
        contractor.setAttachmentUrls(contractorInputDto.getPaths());
        ContractorType conType = conTypeDao.findById(contractorInputDto.getContractorTypeId()).orElse(null);
        contractor.setContractorType(conType);
        Set<ContractorNature> conNatures = contractorInputDto.getContractorNatureId().stream()
                .map(id -> conNatureDao.findById(id).orElse(null))
                .collect(Collectors.toSet());
        contractor.setContractorNature(conNatures);
        ContractorGrade conGrade = conGradeDao.findById(contractorInputDto.getContractorGradeId()).orElse(null);
        contractor.setContractorGrade(conGrade);
        contractor.setSubmissionMode(contractorInputDto.getSubbmissionMode());
        if(contractor.getSubmissionMode().equals("MANUAL")){
            contractor.setStatus(ContractorStatus.VERIFIED);
        }
        else{
            contractor.setStatus(ContractorStatus.PENDING);
        }
        Contractor savedContractor = contractorDao.save(contractor);
        if(savedContractor != null){
            contractorAddress.setId(UUID.randomUUID().toString());
            contractorAddress.setContractor(savedContractor);
            saveContratorAddress(contractorAddress,contractorInputDto);
            contractorContacts.setId(UUID.randomUUID().toString());
            contractorContacts.setContractor(savedContractor);
            saveContractorContacts(contractorContacts);
            contractorTaxDetails.setId(UUID.randomUUID().toString());
            contractorTaxDetails.setContractor(savedContractor);
            saveContractorTaxDetails(contractorTaxDetails,contractorInputDto);
            contractorBankDetails.setId(UUID.randomUUID().toString());
            contractorBankDetails.setContractor(savedContractor);
            saveContractorBankDetails(contractorBankDetails);
            contractorAddInfo.setId(UUID.randomUUID().toString());
            contractorAddInfo.setContractor(savedContractor);
            saveContractorAddInfo(contractorAddInfo,contractorInputDto);
        }
        return "";
    }

    private void saveContratorAddress(ContractorAddress contractorAddress, ContractorInputDto contractorInputDto) {
        Country country = masterService.findCountryById(contractorAddress.getCountry());
        Cities city = masterService.findCityById(contractorAddress.getCity());
        AddressType addressType = masterService.findAddressTypeById(contractorInputDto.getAddressTypeId());
        contractorAddress.setAddressType(addressType);
        contractorAddress.setCountry(country.getId());
        contractorAddress.setCity(city.getId());
        contractorAddressDao.save(contractorAddress);
    }

    private void saveContractorContacts(ContractorContacts contractorContacts) {
        contactsDao.save(contractorContacts);
    }

    private void saveContractorTaxDetails(ContractorTaxDetails contractorTaxDetails, ContractorInputDto contractorInputDto) {
        TaxType taxType = masterService.findTaxTypeById(contractorInputDto.getTaxTypeId());
        contractorTaxDetails.setTaxType(taxType);
        contractorTaxDetails.setTerritory(TerritoryType.valueOf(contractorInputDto.getTerritoryTypeId()).name());
        taxDetailsDao.save(contractorTaxDetails);
    }
    private void saveContractorBankDetails(ContractorBankDetails contractorBankDetails) {
        bankDetailsDao.save(contractorBankDetails);
    }
    private void saveContractorAddInfo(ContractorAddInfo contractorAddInfo, ContractorInputDto contractorInputDto) {
        IdentityType identityType =
                masterService.findIdentityTypeById(contractorInputDto.getIdTypeId());
        contractorAddInfo.setIdentityType(identityType);
        addInfoDao.save(contractorAddInfo);
    }
    @Override
    public List<String> saveDocuments(List<MultipartFile> files) {
        if(files == null){
            return null;
        }
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
    @Override
    public void addContractorType(ContractorType contractorType) {
        if (conTypeDao.existsByTypeIgnoreCase(contractorType.getType())) {
            throw new DuplicateEntryException(messageSource.getMessage("data.duplicate",null,Locale.ENGLISH));
        }
        contractorType.setId(UUID.randomUUID().toString());
        contractorType.setActive(true);
        conTypeDao.save(contractorType);
    }
    @Override
    public void editContractorType(ContractorType contractorType) {
        ContractorType existing = conTypeDao.findById(contractorType.getId())
                .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
        if (!existing.getType().equalsIgnoreCase(contractorType.getType())
                && conTypeDao.existsByTypeIgnoreCase(contractorType.getType())) {
            throw new DuplicateEntryException(messageSource.getMessage("data.duplicate",null,Locale.ENGLISH));
        }
        existing.setType(contractorType.getType());
        existing.setActive(contractorType.isActive());
        conTypeDao.save(existing);
    }
    @Override
    public void addContractorNature(ContractorNatureDto contractorNatureDto) {
        if (contractorNatureDto.getEntityTypeId() == null) {
            throw new DataNotFoundException(
                    messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
            );
        }
        ContractorType contractorType = conTypeDao.findById(contractorNatureDto.getEntityTypeId())
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
                ));
        if (conNatureDao.existsByNatureIgnoreCaseAndContractorType_Id(
                contractorNatureDto.getNature(),
                contractorType.getId())) {
            throw new DuplicateEntryException(
                    messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
            );
        }
        ContractorNature contractorNature = new ContractorNature();
        contractorNature.setId(UUID.randomUUID().toString());
        contractorNature.setNature(contractorNatureDto.getNature());
        contractorNature.setContractorType(contractorType);
        contractorNature.setActive(contractorNatureDto.isActive());
        conNatureDao.save(contractorNature);
    }
    @Override
    public void editContractorNature(ContractorNatureDto contractorNatureDto) {
        ContractorNature existing = conNatureDao.findById(contractorNatureDto.getId())
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
                ));
        ContractorType contractorType = conTypeDao.findById(contractorNatureDto.getEntityTypeId())
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
                ));
        if (!existing.getNature().equalsIgnoreCase(contractorNatureDto.getNature())
                && conNatureDao.existsByNatureIgnoreCaseAndContractorType_Id(
                contractorNatureDto.getNature(),
                contractorType.getId())) {
            throw new DuplicateEntryException(
                    messageSource.getMessage("data.duplicate", null, Locale.ENGLISH)
            );
        }
        existing.setNature(contractorNatureDto.getNature());
        existing.setContractorType(contractorType);
        existing.setActive(contractorNatureDto.isActive());
        conNatureDao.save(existing);
    }
    @Override
    public void addContractorGrade(ContractorGrade contractorGrade) {
        if (conGradeDao.existsByGradeNameIgnoreCase(contractorGrade.getGradeName())) {
            throw new DuplicateEntryException(messageSource.getMessage("data.duplicate",null,Locale.ENGLISH));
        }
        contractorGrade.setId(UUID.randomUUID().toString());
        contractorGrade.setActive(true);
        conGradeDao.save(contractorGrade);
    }
    @Override
    public void editContractorGrade(ContractorGrade contractorGrade) {
        ContractorGrade existing = conGradeDao.findById(contractorGrade.getId())
                .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("data.not.found",null,Locale.ENGLISH)));
        if (!existing.getGradeName().equalsIgnoreCase(contractorGrade.getGradeName())
                && conGradeDao.existsByGradeNameIgnoreCase(contractorGrade.getGradeName())) {
            throw new DuplicateEntryException(messageSource.getMessage("data.duplicate",null,Locale.ENGLISH));
        }
        existing.setGradeName(contractorGrade.getGradeName());
        existing.setActive(contractorGrade.isActive());
        conGradeDao.save(existing);
    }
    @Override
    public List<ContractorType> getAllContractorType() {
        return conTypeDao.findAll();
    }
    @Override
    public List<ContractorNature> getAllContractorNature() {
        return conNatureDao.findAll();
    }
    @Override
    public List<ContractorGrade> getAllContractorGrade() {
        return conGradeDao.findAll();
    }
    @Override
    public List<ContractorNature> getContractorNatureByContractorType(String contractorTypeId) {
        return conNatureDao.findByContractorTypeId(contractorTypeId);
    }
    @Override
    public List<ContractorStatusDto> getAllContractorStatus() {
        return Arrays.stream(ContractorStatus.values())
                .map(status -> new ContractorStatusDto(status.name(), status.getLabel()))
                .toList();
    }
    @Override
    public String inviteContractor(ContractorFormInvite contractorFormInvite) {
        String message = "";
        if(contractorFormInvite.getEmail() == null){
            throw new IllegalArgumentException(messageSource.getMessage("contractor.email.not.found",null,Locale.ENGLISH));
        }
        contractorFormInvite.setId(UUID.randomUUID().toString());
        contractorFormInvite.setContractorStatus(ContractorStatus.INVITED);
        contractorFormInvite.setInvitedBy(userService.findLoggedInUser().getId());
        contractorFormInvite.setInvitedOn(Date.valueOf(LocalDate.now()));
        contractorFormInvite.setUsername(contractorFormInvite.getEmail());
        ContractorFormInvite savedInvite = contractorFormInviteDao.save(contractorFormInvite);
        if(savedInvite != null){
            message = emailService.sentEnrollmentInvite(contractorFormInvite);
        }
        return message;
    }

    @Override
    public List<ContractorFormInvite> getAllinvitedContractor() {
        List<ContractorFormInvite> invites = contractorFormInviteDao.findAll();
        if (invites != null && !invites.isEmpty()) {
            return invites.stream()
                    .filter(invite -> invite.getContractorStatus() != ContractorStatus.VERIFIED)
                    .sorted(Comparator.comparingInt(invite -> {
                        ContractorStatus status = invite.getContractorStatus();
                        if (status == ContractorStatus.PENDING) return 1;
                        if (status == ContractorStatus.INVITED) return 2;
                        return 3;
                    }))
                    .toList();
        }
        return invites;
    }

    @Override
    public String generateToken(String id) {
        ContractorFormInvite formInvite = contractorFormInviteDao.findById(id)
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("contractor.not.invited", null, Locale.ENGLISH)));
        if (formInvite.isSubmitted()) {
            throw new FormAlreadySubmittedException("Contractor details already saved");
        }
        return userService.getTempToken(formInvite);
    }
}
