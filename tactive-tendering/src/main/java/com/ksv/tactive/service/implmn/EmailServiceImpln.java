package com.ksv.tactive.service.implmn;

import com.ksv.tactive.project.entity.ContractorFormInvite;
import com.ksv.tactive.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpln implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${contractor.invite.link}")
    private String enrollmentLink;

    @Value("${spring.mail.username}")
    private String sendFrom;


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
                <body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;'>
                    <div style='background-color: #ffffff; max-width: 600px; margin: 20px auto; border: 1px solid #ddd;'>
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
}
