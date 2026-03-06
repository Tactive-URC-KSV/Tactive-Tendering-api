package com.ksv.tactive.service;

import com.ksv.tactive.project.entity.Contractor;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.OfferNegotiation;
import com.ksv.tactive.project.entity.Tender;
import com.ksv.tactive.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailService {
    String sentEnrollmentInvite(ContractorFormInvite contractorFormInvite);
    String sendquoteMail(List<Contractor> contractors, Tender tender);
    String sentNegotiateMail(OfferNegotiation negotiation, String mailId);
    void sendOtp(User user, String otp);
    void sentLoginCredentials(User user, String password);
}
