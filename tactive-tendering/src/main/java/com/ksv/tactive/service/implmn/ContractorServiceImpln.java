package com.ksv.tactive.service.implmn;

import com.ksv.tactive.dto.*;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.exception.FormAlreadySubmittedException;
import com.ksv.tactive.dto.Country;
import com.ksv.tactive.project.entity.AddressType;
import com.ksv.tactive.project.entity.IdentityType;
import com.ksv.tactive.project.dao.*;
import com.ksv.tactive.project.entity.*;
import com.ksv.tactive.service.*;
import com.ksv.tactive.util.ContractorStatus;
import com.ksv.tactive.util.TaxType;
import com.ksv.tactive.util.TerritoryType;
import jakarta.transaction.Transactional;
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
import java.sql.Timestamp;
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
    private TenderInviteDao tenderInviteDao;

    @Autowired
    private MessageSource messageSource;

    private UUID convertToUUID(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(id);
    }
    @Override
    public List<Map<String, Object>>getAllContractor() {
        List<Contractor> contractors = contractorDao.findAll()
                .stream()
                .filter(contractor -> contractor.getStatus() == ContractorStatus.VERIFIED)
                .toList();
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
    @Transactional
    public String addContractor(ContractorDTO dto) {
        Contractor contractor;
        UUID contractorId = convertToUUID(dto.getId());
        if (contractorId != null) {
            contractor = contractorDao.findById(contractorId)
                    .orElseThrow(() -> new RuntimeException("Contractor not found"));
        } else {
            contractor = new Contractor();
        }
        contractor.setEntityCode(dto.getEntityCode());
        contractor.setEntityName(dto.getEntityName());
        contractor.setEffectiveDate(dto.getEffectiveDate());
        contractor.setAttachmentUrls(dto.getAttachmentUrls());
        contractor.setInviteId(dto.getInviteId());
        UUID typeId = convertToUUID(dto.getContractorTypeId());
        if (typeId != null) {
            contractor.setContractorType(conTypeDao.findById(typeId).orElse(null));
        } else {
            contractor.setContractorType(null);
        }
        UUID gradeId = convertToUUID(dto.getContractorGradeId());
        if (gradeId != null) {
            contractor.setContractorGrade(conGradeDao.findById(gradeId).orElse(null));
        } else {
            contractor.setContractorGrade(null);
        }
        Set<ContractorNature> natures =
                dto.getContractorNatureIds() != null
                        ? dto.getContractorNatureIds().stream()
                        .map(this::convertToUUID)
                        .filter(Objects::nonNull)
                        .map(id -> conNatureDao.findById(id))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet())
                        : new HashSet<>();
        contractor.setContractorNature(natures);
        contractor.setSubmissionMode(dto.getSubmissionMode());
        contractor.setStatus(
                "MANUAL".equalsIgnoreCase(dto.getSubmissionMode())
                        ? ContractorStatus.VERIFIED
                        : ContractorStatus.PENDING
        );

        Map<UUID, ContractorContacts> existingContacts =
                contractor.getContractorContacts().stream()
                        .collect(Collectors.toMap(ContractorContacts::getId, c -> c));

        List<ContractorContacts> updatedContacts = new ArrayList<>();

        if (dto.getContacts() != null) {
            for (ContactDTO c : dto.getContacts()) {
                ContractorContacts entity;
                UUID id = convertToUUID(c.getId());

                if (id != null) {
                    entity = existingContacts.getOrDefault(id, new ContractorContacts());
                } else {
                    entity = new ContractorContacts();
                }

                entity.setName(c.getName());
                entity.setDesignation(c.getPosition());
                entity.setPhoneNumber(c.getPhoneNo());
                entity.setEmail(c.getEmail());
                entity.setContractor(contractor);

                updatedContacts.add(entity);
            }
        }

        contractor.getContractorContacts().clear();
        contractor.getContractorContacts().addAll(updatedContacts);

        Map<UUID, ContractorAddress> existingAddresses =
                contractor.getContractorAddresses().stream()
                        .collect(Collectors.toMap(ContractorAddress::getId, a -> a));

        List<ContractorAddress> updatedAddresses = new ArrayList<>();

        if (dto.getAddresses() != null) {
            for (AddressDTO a : dto.getAddresses()) {
                ContractorAddress entity;
                UUID id = convertToUUID(a.getId());

                if (id != null) {
                    entity = existingAddresses.getOrDefault(id, new ContractorAddress());
                } else {
                    entity = new ContractorAddress();
                }

                entity.setAddress1(a.getAddress1());
                entity.setAddress2(a.getAddress2());
                entity.setCity(a.getCityId());
                entity.setZipCode(a.getZipcode());
                entity.setPhoneNumber(a.getPhone());
                entity.setEmail(a.getEmail());

                if (a.getAddressTypeId() != null) {
                    entity.setAddressType(
                            masterService.findAddressTypeById(a.getAddressTypeId())
                    );
                }

                entity.setCountry(a.getCountryId());
                entity.setContractor(contractor);

                updatedAddresses.add(entity);
            }
        }

        contractor.getContractorAddresses().clear();
        contractor.getContractorAddresses().addAll(updatedAddresses);

        Map<UUID, ContractorTaxDetails> existingTax =
                contractor.getContractorTaxDetails().stream()
                        .collect(Collectors.toMap(ContractorTaxDetails::getId, t -> t));

        List<ContractorTaxDetails> updatedTax = new ArrayList<>();
        if (dto.getTaxDetails() != null) {
            for (TaxDTO t : dto.getTaxDetails()) {
                ContractorTaxDetails entity;
                UUID id = convertToUUID(t.getId());
                if (id != null) {
                    entity = existingTax.getOrDefault(id, new ContractorTaxDetails());
                } else {
                    entity = new ContractorTaxDetails();
                }
                if (t.getTaxTypeId() != null && !t.getTaxTypeId().isEmpty()) {
                    entity.setTaxType(TaxType.valueOf(t.getTaxTypeId().toUpperCase()));
                }
                if (t.getTerritoryTypeId() != null && !t.getTerritoryTypeId().isEmpty()) {
                    entity.setTerritoryType(TerritoryType.valueOf(t.getTerritoryTypeId().toUpperCase()));
                }
                entity.setTerritory(t.getTerritory());
                entity.setTaxRegNumber(t.getTaxRegNo());
                if (t.getTaxRegDate() != null && !t.getTaxRegDate().isEmpty()) {
                    entity.setTaxRegDate(Date.valueOf(t.getTaxRegDate()));
                }
                entity.setAddress1(t.getAddress1());
                entity.setAddress2(t.getAddress2());
                entity.setCity(t.getCity());
                entity.setPinCode(t.getPinCode());
                entity.setEmail(t.getEmail());
                entity.setContractor(contractor);
                updatedTax.add(entity);
            }
        }
        contractor.getContractorTaxDetails().clear();
        contractor.getContractorTaxDetails().addAll(updatedTax);
        Map<UUID, ContractorBankDetails> existingBanks =
                contractor.getContractorBankDetails().stream()
                        .collect(Collectors.toMap(ContractorBankDetails::getId, b -> b));
        List<ContractorBankDetails> updatedBanks = new ArrayList<>();
        if (dto.getBankDetails() != null) {
            for (BankDTO b : dto.getBankDetails()) {
                ContractorBankDetails entity;
                UUID id = convertToUUID(b.getId());
                if (id != null) {
                    entity = existingBanks.getOrDefault(id, new ContractorBankDetails());
                } else {
                    entity = new ContractorBankDetails();
                }
                entity.setAccHolderName(b.getAccHolderName());
                entity.setAccNumber(b.getAccNumber());
                entity.setBankName(b.getBankName());
                entity.setBranch(b.getBranch());
                entity.setBankAddress(b.getBankAddress());
                entity.setContractor(contractor);
                updatedBanks.add(entity);
            }
        }
        contractor.getContractorBankDetails().clear();
        contractor.getContractorBankDetails().addAll(updatedBanks);
        contractorDao.save(contractor);
        return contractorId == null
                ? "Contractor created successfully"
                : "Contractor updated successfully";
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
        ContractorType contractorType = conTypeDao.findById(convertToUUID(contractorNatureDto.getEntityTypeId()))
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
        contractorNature.setNature(contractorNatureDto.getNature());
        contractorNature.setContractorType(contractorType);
        contractorNature.setActive(contractorNatureDto.isActive());
        conNatureDao.save(contractorNature);
    }
    @Override
    public void editContractorNature(ContractorNatureDto contractorNatureDto) {
        ContractorNature existing = conNatureDao.findById(convertToUUID(contractorNatureDto.getId()))
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("data.not.found", null, Locale.ENGLISH)
                ));
        ContractorType contractorType = conTypeDao.findById(convertToUUID(contractorNatureDto.getEntityTypeId()))
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
        return conNatureDao.findByContractorTypeId(UUID.fromString(contractorTypeId));
    }
    @Override
    public List<EnumDto> getAllContractorStatus() {
        return Arrays.stream(ContractorStatus.values())
                .map(status -> new EnumDto(status.name(), status.getLabel()))
                .toList();
    }
    @Override
    public String inviteContractor(ContractorFormInvite contractorFormInvite) {
        String message = "";
        if(contractorFormInvite.getEmail() == null){
            throw new IllegalArgumentException(messageSource.getMessage("contractor.email.not.found",null,Locale.ENGLISH));
        }
        ContractorFormInvite existing = contractorFormInviteDao.findByEmail(contractorFormInvite.getEmail());
        if(existing != null){
            throw new DuplicateEntryException(messageSource.getMessage("data.duplicate",null,Locale.ENGLISH));
        }
        contractorFormInvite.setContractorStatus(ContractorStatus.INVITED);
        contractorFormInvite.setInvitedBy(userService.findLoggedInUser().getId().toString());
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
        if (!invites.isEmpty()) {
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
    public ContractorFormInvite getInviteById(String id) {
        return contractorFormInviteDao.findById(convertToUUID(id))
                .orElseThrow(() -> new DataNotFoundException(messageSource.getMessage("contractor.not.found",null, Locale.ENGLISH)));
    }

    @Override
    public String generateToken(String id) {
        ContractorFormInvite formInvite = contractorFormInviteDao.findById(convertToUUID(id))
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("contractor.not.invited", null, Locale.ENGLISH)));
        if (formInvite.isSubmitted()) {
            throw new FormAlreadySubmittedException(messageSource.getMessage("contractor.details.saved", null, Locale.ENGLISH));
        }
        return userService.getTempToken(formInvite);
    }
    @Override
    public ContractorResDto getContractorById(String contractorId) {
        ContractorResDto contractorResDto = new ContractorResDto();
        Contractor contractor = contractorDao.findById(convertToUUID(contractorId))
                .orElseThrow(()->new DataNotFoundException(messageSource.getMessage("contractor.not.found",null,Locale.ENGLISH)));
        contractorResDto.setContractor(contractor);
        ContractorContacts contacts = contactsDao.findByContractorId(contractor.getId());
        contractorResDto.setContractorContacts(contacts);
        ContractorAddress address = contractorAddressDao.findByContractorId(contractor.getId());
        contractorResDto.setContractorAddress(address);
        ContractorTaxDetails taxDetails = taxDetailsDao.findByContractorId(contractor.getId());
        contractorResDto.setContractorTaxDetails(taxDetails);
        ContractorBankDetails bankDetails = bankDetailsDao.findByContractorId(contractor.getId());
        contractorResDto.setContractorBankDetails(bankDetails);
        ContractorAddInfo addInfo = addInfoDao.findByContractorId(contractor.getId());
        contractorResDto.setContractorAddInfo(addInfo);
        return contractorResDto;
    }
    @Override
    public String verifyContractor(String contractorId, String contractorStatus) {
        Contractor contractor = contractorDao.findById(convertToUUID(contractorId))
                .orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("contractor.not.found",null,Locale.ENGLISH)));
        String inviteId = contractor.getInviteId();
        ContractorFormInvite formInvite = contractorFormInviteDao.findById(convertToUUID(inviteId))
                .orElseThrow(()-> new DataNotFoundException(messageSource.getMessage("contractor.not.found", null, Locale.ENGLISH)));
        if(contractorStatus.equals("VERIFIED")){
            contractor.setStatus(ContractorStatus.VERIFIED);
            formInvite.setContractorStatus(ContractorStatus.VERIFIED);
        }
        else if(contractorStatus.equals("REJECTED")){
            contractor.setStatus(ContractorStatus.REJECTED);
            formInvite.setContractorStatus(ContractorStatus.REJECTED);
        }
        contractorDao.save(contractor);
        return contractorStatus.equals("VERIFIED") ?
                messageSource.getMessage("contractor.approved.success", null, Locale.ENGLISH) :
                messageSource.getMessage("contractor.reject.success", null, Locale.ENGLISH);
    }
    @Override
    public Contractor findContractorById(String contractorId) {
        return contractorDao.findById(convertToUUID(contractorId))
                .orElseThrow(()->new DataNotFoundException(messageSource.getMessage("contractor.not.found",null,Locale.ENGLISH)));
    }
    @Override
    public String generateTenderToken(String contractorId, String tenderId) {
        TenderInvites tenderInvite = tenderInviteDao.findByContractor_IdAndTender_Id(UUID.fromString(contractorId), UUID.fromString(tenderId))
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("tender.invite.not.found", null, Locale.ENGLISH)));
        if (tenderInvite.isSubmitted()) {
            throw new FormAlreadySubmittedException(
                    messageSource.getMessage("tender.quote.already.submitted", null, Locale.ENGLISH));
        }
        if (tenderInvite.getTokenExpiry() != null &&
                tenderInvite.getTokenExpiry().before(new java.sql.Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Link has expired");
        }

        return userService.getTempTokenForTender(tenderInvite);
    }

    @Override
    public String submitOffer(OfferDTO offerDTO) {
        TenderInvites tenderInvite = tenderInviteDao.findByContractor_IdAndTender_Id(UUID.fromString(offerDTO.getContractorId()), UUID.fromString(offerDTO.getTenderId()))
                .orElseThrow(() -> new DataNotFoundException(
                        messageSource.getMessage("tender.invite.not.found", null, Locale.ENGLISH)));
        tenderInvite.setSubmitted(true);
        tenderInvite.setTokenExpiry(new Timestamp(System.currentTimeMillis()));
        tenderInviteDao.save(tenderInvite);
        return messageSource.getMessage("offer.submitted", null, Locale.ENGLISH);

    }

    @Override
    public Contractor getContractorByInviteId(String inviteId) {
        return contractorDao.findByInviteId(inviteId);
    }
}
