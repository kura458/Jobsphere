package com.jobsphere.jobsite.utils;

import com.jobsphere.jobsite.constant.OtpType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class EmailTemplateBuilder {

    public String buildOtpEmail(String otpCode, OtpType otpType) {
        String templateName = getTemplateName(otpType);

        try {
            String template = loadTemplate("email/" + templateName + "-email.html");
            return template.replace("{{otpCode}}", otpCode);
        } catch (Exception e) {
            log.warn("Template {} not found, using fallback", templateName);
            return buildFallbackEmail(otpCode, otpType);
        }
    }

    public String buildVerificationCodeEmail(String companyName, String verificationCode) {
        try {
            String template = loadTemplate("email/company-verification-code-email.html");
            return template.replace("{{companyName}}", companyName)
                    .replace("{{verificationCode}}", verificationCode);
        } catch (Exception e) {
            log.warn("Company verification code template not found, using fallback");
            return """
                    <div style="font-family:system-ui;max-width:600px;margin:auto">
                        <h2 style="color:#1d4ed8">Verification for %s</h2>
                        <p>Use the following code to verify your company on JobSphere:</p>
                        <div style="font-size:36px;font-weight:bold;color:#1d4ed8;margin:20px 0;letter-spacing:5px">
                            %s
                        </div>
                    </div>
                    """.formatted(companyName, verificationCode);
        }
    }

    public String buildApprovalEmail(String companyName) {
        try {
            String template = loadTemplate("email/company-approval-email.html");
            return template.replace("{{companyName}}", companyName);
        } catch (Exception e) {
            log.warn("Company approval email template not found, using fallback");
            return """
                    <div style="font-family:system-ui;max-width:600px;margin:auto">
                        <h2 style="color:#10b981">Verification Approved!</h2>
                        <p>Congratulations, <strong>%s</strong> has been successfully verified on JobSphere.</p>
                        <p>You now have full access to employer features.</p>
                    </div>
                    """.formatted(companyName);
        }
    }

    public String buildRejectionEmail(String companyName, String rejectionReason) {
        try {
            String template = loadTemplate("email/company-rejection-email.html");
            return template.replace("{{companyName}}", companyName)
                    .replace("{{rejectionReason}}", rejectionReason);
        } catch (Exception e) {
            log.warn("Company rejection email template not found, using fallback");
            return """
                    <div style="font-family:system-ui;max-width:600px;margin:auto">
                        <h2 style="color:#ef4444">Verification Update</h2>
                        <p>We were unable to verify <strong>%s</strong>.</p>
                        <p><strong>Reason:</strong> %s</p>
                        <p>You can submit a new request with corrected information.</p>
                    </div>
                    """.formatted(companyName, rejectionReason);
        }
    }

    public String buildApplicationStatusEmail(String jobTitle, String status, String rejectionReason) {
        String statusColor = switch (status.toUpperCase()) {
            case "APPROVED", "ACCEPTED" -> "#10b981";
            case "REJECTED" -> "#ef4444";
            case "INTERVIEW", "INTERVIEWING" -> "#3b82f6";
            default -> "#1d4ed8";
        };

        StringBuilder reasonHtml = new StringBuilder();
        if ("REJECTED".equalsIgnoreCase(status) && rejectionReason != null && !rejectionReason.isBlank()) {
            reasonHtml
                    .append("""
                            <div style="margin-top:20px;padding:20px;background-color:#fef2f2;border-radius:12px;border:1px solid #fee2e2">
                                <strong style="color:#991b1b;display:block;margin-bottom:8px;font-size:14px;text-transform:uppercase;letter-spacing:1px">Feedback from Employer</strong>
                                <p style="color:#b91c1c;margin:0;font-style:italic;line-height:1.6">"%s"</p>
                            </div>
                            """
                            .formatted(rejectionReason));
        }

        return """
                <div style="font-family:system-ui,'Segoe UI',Roboto,sans-serif;max-width:600px;margin:auto;border:1px solid #e2e8f0;border-radius:24px;overflow:hidden;background-color:#ffffff">
                    <div style="background:linear-gradient(135deg, #1d4ed8, #1e40af);padding:40px 20px;text-align:center">
                        <h1 style="color:#ffffff;margin:0;font-size:28px;font-weight:900;letter-spacing:-0.5px">JobSphere</h1>
                        <p style="color:rgba(255,255,255,0.7);margin-top:10px;text-transform:uppercase;font-weight:700;font-size:12px;letter-spacing:2px">Application Update</p>
                    </div>
                    <div style="padding:40px 30px">
                        <p style="font-size:16px;color:#475569;margin-bottom:30px">Hello,</p>
                        <p style="font-size:18px;color:#1e293b;line-height:1.5;margin-bottom:24px">
                            The status of your application for <strong style="color:#1d4ed8">%s</strong> has been updated to:
                        </p>
                        <div style="display:inline-block;padding:12px 24px;background-color:%s;color:#ffffff;border-radius:12px;font-weight:900;text-transform:uppercase;letter-spacing:1px;font-size:14px;margin-bottom:30px shadow:0 4px 12px rgba(0,0,0,0.1)">
                            %s
                        </div>
                        %s
                        <div style="margin-top:40px;padding-top:30px;border-top:1px solid #f1f5f9;text-align:center">
                            <p style="color:#64748b;font-size:14px;margin-bottom:20px">Log in to your dashboard to view more details and next steps.</p>
                            <a href="https://jobsite.com/seeker/applications" style="display:inline-block;padding:16px 32px;background-color:#1d4ed8;color:#ffffff;text-decoration:none;border-radius:14px;font-weight:700;font-size:14px">Go to Dashboard</a>
                        </div>
                    </div>
                    <div style="background-color:#f8fafc;padding:30px;text-align:center;border-top:1px solid #f1f5f9">
                        <p style="color:#94a3b8;font-size:12px;margin:0">© 2024 JobSphere. All rights reserved.</p>
                    </div>
                </div>
                """
                .formatted(jobTitle, statusColor, status, reasonHtml.toString());
    }

    private String loadTemplate(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/" + path);
        byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String getEmailSubject(OtpType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION -> "Verify Your JobSphere Account";
            case PASSWORD_RESET -> "Reset Your JobSphere Password";
            case ADMIN_LOGIN -> "Your Admin Login Code - JobSphere";
        };
    }

    private String getTemplateName(OtpType otpType) {
        return switch (otpType) {
            case PASSWORD_RESET -> "reset-password";
            case ADMIN_LOGIN -> "otp";
            default -> "verification";
        };
    }

    private String buildFallbackEmail(String otpCode, OtpType otpType) {
        String purpose = switch (otpType) {
            case EMAIL_VERIFICATION -> "email verification";
            case PASSWORD_RESET -> "password reset";
            case ADMIN_LOGIN -> "admin login";
        };

        return """
                <div style="font-family:system-ui;max-width:600px;margin:auto">
                    <h2 style="color:#1d4ed8">Your %s Code</h2>
                    <div style="font-size:36px;font-weight:bold;color:#1d4ed8;margin:20px 0">
                        %s
                    </div>
                    <p>Valid for 10 minutes</p>
                </div>
                """.formatted(purpose, otpCode);
    }
}