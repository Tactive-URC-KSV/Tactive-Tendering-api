package com.ksv.tactive.service.implmn;

import com.ksv.tactive.contractor.dao.*;
import com.ksv.tactive.contractor.entity.*;
import com.ksv.tactive.dto.ContractorInputDto;
import com.ksv.tactive.dto.ContractorNatureDto;
import com.ksv.tactive.exception.DataNotFoundException;
import com.ksv.tactive.exception.DuplicateEntryException;
import com.ksv.tactive.location.entity.Cities;
import com.ksv.tactive.location.entity.Country;
import com.ksv.tactive.master.entity.AddressType;
import com.ksv.tactive.master.entity.IdentityType;
import com.ksv.tactive.master.entity.TaxType;
import com.ksv.tactive.master.entity.TerritoryType;
import com.ksv.tactive.service.ContractorService;
import com.ksv.tactive.service.MasterService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContractorServiceImpln implements ContractorService {

    @Autowired
    private MasterService masterService;

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
    private MessageSource messageSource;

    @Override
    public List<Contractor> getAllContractor() {
        return contractorDao.findAll();
    }

    @Override
    public String addContractor(ContractorInputDto contractorInputDto, ContractorAddress contractorAddress,
                                ContractorContacts contractorContacts, ContractorTaxDetails contractorTaxDetails,
                                ContractorBankDetails contractorBankDetails, ContractorAddInfo contractorAddInfo) {
        Contractor contractor = new Contractor();
        contractor.setId(UUID.randomUUID().toString());
        contractor.setEntityCode(contractorInputDto.getEntityCode());
        contractor.setEntityName(contractor.getEntityName());
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
        Contractor savedContractor = contractorDao.save(contractor);
        if(savedContractor != null){
            contractorAddress.setId(UUID.randomUUID().toString());
            contractorAddress.setContractor(savedContractor);
            saveContratorAddress(contractorAddress);
            contractorContacts.setId(UUID.randomUUID().toString());
            contractorContacts.setContractor(savedContractor);
            saveContractorContacts(contractorContacts);
            contractorTaxDetails.setId(UUID.randomUUID().toString());
            contractorTaxDetails.setContractor(savedContractor);
            saveContractorTaxDetails(contractorTaxDetails);
            contractorBankDetails.setId(UUID.randomUUID().toString());
            contractorBankDetails.setContractor(savedContractor);
            saveContractorBankDetails(contractorBankDetails);
            contractorAddInfo.setId(UUID.randomUUID().toString());
            contractorAddInfo.setContractor(savedContractor);
            saveContractorAddInfo(contractorAddInfo);
        }
        return "";
    }

    private void saveContratorAddress(ContractorAddress contractorAddress) {
        Country country = masterService.findCountryById(contractorAddress.getCountry());
        Cities city = masterService.findCityById(contractorAddress.getCity());
        AddressType addressType = masterService.findAddressTypeById(contractorAddress.getAddressType());
        contractorAddress.setAddressType(addressType.getId());
        contractorAddress.setCountry(country.getId());
        contractorAddress.setCity(city.getId());
        contractorAddressDao.save(contractorAddress);
    }

    private void saveContractorContacts(ContractorContacts contractorContacts) {
        contactsDao.save(contractorContacts);
    }

    private void saveContractorTaxDetails(ContractorTaxDetails contractorTaxDetails) {
        TaxType taxType = masterService.findTaxTypeById(contractorTaxDetails.getTaxType());
        TerritoryType territoryType = masterService.findterritoryTypeId(contractorTaxDetails.getTerritoryType());
        String territoryId = contractorTaxDetails.getTerritory();
    }

    private void saveContractorBankDetails(ContractorBankDetails contractorBankDetails) {
        bankDetailsDao.save(contractorBankDetails);
    }

    private void saveContractorAddInfo(ContractorAddInfo contractorAddInfo) {
        if (contractorAddInfo.getInfoType() == null) {
            return;
        }
        IdentityType identityType =
                masterService.findIdentityTypeById(contractorAddInfo.getInfoType());
        contractorAddInfo.setInfoType(identityType.getId());
        addInfoDao.save(contractorAddInfo);
    }


    @Override
    public List<String> saveDocuments(List<MultipartFile> files) {
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
        System.err.println(contractorType.getType());
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
}
