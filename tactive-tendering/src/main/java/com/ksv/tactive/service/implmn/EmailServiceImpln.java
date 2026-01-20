package com.ksv.tactive.service.implmn;

import com.ksv.tactive.dto.BOQDto;
import com.ksv.tactive.fileservice.ExcelFileService;
import com.ksv.tactive.project.dao.ContractorContactsDao;
import com.ksv.tactive.project.entity.Contractor;
import com.ksv.tactive.project.entity.ContractorContacts;
import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.project.entity.Tender;
import com.ksv.tactive.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpln implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${contractor.invite.link}")
    private String enrollmentLink;

    @Value("${spring.mail.username}")
    private String sendFrom;

    @Autowired
    private ContractorContactsDao contactsDao;

    @Autowired
    private ExcelFileService excelFileService;

    @Override
    public String sentEnrollmentInvite(ContractorFormInvite contractorFormInvite) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sendFrom);
            helper.setTo(contractorFormInvite.getEmail());
            helper.setSubject("Action Required: Vendor Enrollment Invitation");
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>
                    <div style='background-color: #f4f4f4; max-width: 600px; margin: 20px auto; border: 1px solid #ddd;'>
                        <div style='background-color: #005197; padding: 25px; text-align: center;'>
                            <h2 style='color: #ffffff; margin: 0;'>Vendor Portal</h2>
                        </div>
                        <div style='padding: 30px; color: #333333; line-height: 1.6;'>
                            <p>Dear %s,</p>
                            <p>You have been formally invited to enroll as a verified vendor in our network. 
                            To maintain the integrity of our secure onboarding process, please proceed using the button below. 
                            This invitation is unique to your organization and subject to strict security protocols.</p>
                            
                            <p style='background-color: #fff3cd; border: 1px solid #ffeeba; padding: 10px; color: #856404; font-size: 14px;'>
                                <strong>⚠ Security Warning:</strong> Do not share this link with anyone. 
                                Access is monitored, and the link is valid for your specific session only.
                            </p>

                            <div style='text-align: center; margin: 30px 0;'>
                                <a href='%s' style='background-color: #005197; color: #ffffff; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 4px;'>
                                    Complete Enrollment
                                </a>
                            </div>
                            
                            <p style='font-size: 12px; color: #777;'>If you did not request this invitation, please contact our security team immediately.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(contractorFormInvite.getName(), enrollmentLink+"?id="+contractorFormInvite.getId());
            helper.setText(htmlContent, true);
            mailSender.send(message);
            return "Invited Successfully";
        } catch (MessagingException e) {
            return e.getMessage();
        }
    }

    @Override
    public String sendquoteMail(List<Contractor> contractors, Tender tender) {
        Set<BOQDto> boqDtos = tender.getBoq().stream().map(b -> {
            BOQDto dto = new BOQDto();
            dto.setBoqId(b.getId());
            dto.setBoqCode(b.getBoqCode());
            dto.setBoqName(b.getBoqName());
            dto.setQuantity(b.getQuantity());
            dto.setUom(b.getUom());
            return dto;
        }).collect(Collectors.toSet());

        byte[] excelFile = excelFileService.generateQuoteAttachement(boqDtos);

        int successCount = 0;
        for (Contractor contractor : contractors) {
            ContractorContacts contractorContacts = contactsDao.findByContractorId(contractor.getId());

            if (contractorContacts == null || contractorContacts.getEmail() == null) {
                continue;
            }

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(sendFrom);
                helper.setTo(contractorContacts.getEmail());
                helper.setSubject("Invitation to Quote: " + tender.getTenderName());

                String quoteLink = enrollmentLink + "/contractor-tender-quotation"
                        + "?id=" + contractor.getId()
                        + "&tenderId=" + tender.getId();

                String htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>
                <div style='background-color: #f4f4f4; max-width: 600px; margin: 20px auto; border: 1px solid #ddd;'>
                    <div style='background-color: #005197; padding: 25px; text-align: center;'>
                        <h2 style='color: #ffffff; margin: 0;'>Request for Quotation</h2>
                    </div>
                    <div style='padding: 30px; color: #333333; line-height: 1.6;'>
                        <p>Dear %s,</p>
                        <p>You have been shortlisted and invited to submit a quotation for the following tender:</p>
                        
                        <div style='background-color: #e9ecef; padding: 15px; border-radius: 4px; margin: 15px 0;'>
                            <strong>Tender Name:</strong> %s<br>
                            <strong>Reference No:</strong> %s
                        </div>

                        <p>To review the requirements and submit your best offer, please proceed to the secure portal using the button below. 
                        <strong>Please find the BOQ details attached.</strong></p>

                        <div style='text-align: center; margin: 30px 0;'>
                            <a href='%s' style='background-color: #005197; color: #ffffff; padding: 12px 25px; text-decoration: none; font-weight: bold; border-radius: 4px;'>
                                View Tender & Submit Quote
                            </a>
                        </div>
                        
                        <p style='font-size: 12px; color: #777;'>
                            <strong>Note:</strong> This link is unique to your organization. Please do not forward this email to others.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                        contractorContacts.getName(),
                        tender.getTenderName(),
                        tender.getTenderNumber(),
                        quoteLink
                );

                helper.setText(htmlContent, true);

                if (excelFile != null && excelFile.length > 0) {
                    helper.addAttachment("BOQ_Details.xlsx", new ByteArrayResource(excelFile));
                }

                mailSender.send(message);
                successCount++;
            } catch (MessagingException e) {
                return "Failed to Float";
            }
        }

        return successCount + " Quotation Invitations Sent Successfully";
    }
}
