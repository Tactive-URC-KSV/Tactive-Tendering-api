package com.ksv.tactive.service;

import com.ksv.tactive.project.entity.ContractorFormInvite;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    String sentEnrollmentInvite(ContractorFormInvite contractorFormInvite);
}
