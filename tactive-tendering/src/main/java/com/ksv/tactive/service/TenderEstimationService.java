package com.ksv.tactive.service;

import com.ksv.tactive.dto.TenderEstResourceDto;
import com.ksv.tactive.dto.TenderResourceDto;
import com.ksv.tactive.project.entity.InternalBoq;

import java.util.List;
import java.util.UUID;

public interface TenderEstimationService {
    String addResourcesToTender(TenderResourceDto tenderResourceDto);
    String updateResources(TenderResourceDto tenderResourceDto);
    List<TenderEstResourceDto> getEstimatedResourcesByBoq(String boqId);
    List<TenderEstResourceDto> getEstimatedResourcesByInternalBoq(String internalBoqId);
    String deleteTenderEstimationForBoq(String tenderEstimationId, String boqId);
    TenderEstResourceDto getEstimatedResourceById(String tenderEstimationId);
    TenderEstResourceDto getEstimatedResourceByIdAndBoqId(String tenderEstimationId, String boqId);
    String addGlobalValue(String projectId, com.ksv.tactive.project.entity.GlobalValue globalValue);
    List<com.ksv.tactive.project.entity.GlobalValue> viewAllGlobalValueByProjectId(String projectId);
    String editGlobalValue(String id, com.ksv.tactive.project.entity.GlobalValue globalValue);
    List<InternalBoq> getInternalBoqByProject(UUID projectId);
    InternalBoq getInternalBoqById(String internalBoqId);
}
