package com.ksv.tactive.controller;

import com.ksv.tactive.dto.PaginatedResponse;
import com.ksv.tactive.dto.TenderEstResourceDto;
import com.ksv.tactive.dto.TenderResourceDto;
import com.ksv.tactive.project.entity.GlobalValue;
import com.ksv.tactive.project.entity.InternalBoq;
import com.ksv.tactive.service.TenderEstimationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tactive")
public class TenderEstimationController {

    @Autowired
    private TenderEstimationService tenderEstimationService;

    @PostMapping("tenderEstimation/addResources")
    public ResponseEntity<?> addResourcesToTender(@RequestBody TenderResourceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenderEstimationService.addResourcesToTender(dto));
    }

    @PostMapping("tenderEstimation/updateResources")
    public ResponseEntity<?> updateResources(@RequestBody TenderResourceDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.updateResources(dto));
    }

    @GetMapping("/tender-estimation/complex/project/{projectId}")
    public ResponseEntity<?> getInternalBoqByProject(@PathVariable String projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.getInternalBoqByProject(UUID.fromString(projectId)));
    }

    @GetMapping("tenderEstimation/estimatedResources/{boqId}")
    public ResponseEntity<?> getEstimatedResourcesByBoq(@PathVariable String boqId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.getEstimatedResourcesByBoq(boqId));
    }

    @DeleteMapping("tenderEstimation/deleteResourceFromBoq/{tenderEstimationId}/{boqId}")
    public ResponseEntity<?> deleteResourceFromBoq(@PathVariable String tenderEstimationId, @PathVariable String boqId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.deleteTenderEstimationForBoq(tenderEstimationId, boqId));
    }

    @GetMapping("tender/estimatedResource/{tenderEstimationId}")
    public ResponseEntity<?> getEstimatedResourceById(@PathVariable String tenderEstimationId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.getEstimatedResourceById(tenderEstimationId));
    }

    @GetMapping("tenderEstimation/internal-boq/{internalBoqId}")
    public ResponseEntity<?> getInternalBoqById(@PathVariable String internalBoqId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.getInternalBoqById(internalBoqId));
    }

    @GetMapping("tenderEstimation/internal-boq-resources/{internalBoqId}")
    public ResponseEntity<?> getEstimatedResourcesByInternalBoq(@PathVariable String internalBoqId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.getEstimatedResourcesByInternalBoq(internalBoqId));
    }

    @GetMapping("estimatedResource/{tenderEstimationId}")
    public ResponseEntity<?> getEstimatedResourceByIdLegacy(@PathVariable String tenderEstimationId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.getEstimatedResourceById(tenderEstimationId));
    }

    @PostMapping("globalValue/{projectId}")
    public ResponseEntity<?> addGlobalValue(@PathVariable String projectId, @RequestBody GlobalValue globalValue) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenderEstimationService.addGlobalValue(projectId, globalValue));
    }

    @GetMapping("globalValue/{projectId}")
    public ResponseEntity<?> viewAllGlobalValueByProjectId(@PathVariable String projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.viewAllGlobalValueByProjectId(projectId));
    }

    @PutMapping("globalVaue/edit/{projectId}")
    public ResponseEntity<?> editGlobalValue(@PathVariable String projectId, @RequestBody GlobalValue globalValue) {
        return ResponseEntity.status(HttpStatus.OK).body(tenderEstimationService.editGlobalValue(projectId, globalValue));
    }
}
