package com.workstudy.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String senderEmail;

    public void sendMfaCode(String toEmail, String otpCode) {
        System.out.println("===================================================================");
        System.out.println("MFA REQUEST INTERCEPTED: A user is trying to log into " + toEmail);
        System.out.println("YOUR 6-DIGIT AUTHORIZATION CODE IS: [ " + otpCode + " ]");
        System.out.println("===================================================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (senderEmail != null && !senderEmail.isEmpty()) {
                message.setFrom(senderEmail);
            } else {
                message.setFrom("workstudy-noreply@example.com");
            }
            message.setTo(toEmail);
            message.setSubject("WorkStudy Security: Your One-Time Login Code");
            message.setText("Hello,\n\nWe detected a login attempt to your WorkStudy account.\n\nYour 6-Digit Secure Code is: " + otpCode + "\n\nThis code will expire shortly. Do not share it with anyone.\n\nThank you!");
            
            mailSender.send(message);
            System.out.println(">> Email successfully dispatched to " + toEmail + " via Google SMTP!");
        } catch (Exception e) {
            System.err.println(">> Notice: SMTP Email failed to send. Check application.properties if you want real emails.");
            System.err.println(">> (But you can use the code printed above to instantly log in for testing!)");
        }
    }

    public void sendStatusUpdateEmail(String toEmail, String studentName, String jobTitle, String status) {
        System.out.println("===================================================================");
        System.out.println("APPLICATION UPDATE NOTIFICATION FOR: " + toEmail);
        System.out.println("STATUS: [ " + status + " ] for Job: " + jobTitle);
        System.out.println("===================================================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (senderEmail != null && !senderEmail.isEmpty()) {
                message.setFrom(senderEmail);
            } else {
                message.setFrom("workstudy-noreply@example.com");
            }
            message.setTo(toEmail);
            message.setSubject("Application Status Update: " + jobTitle);
            
            String statusText = status.equalsIgnoreCase("APPROVED") ? "Congratulations! Your application has been APPROVED." : "We regret to inform you that your application has been REJECTED.";

            message.setText("Hello " + studentName + ",\n\n" + statusText + "\n\nRegarding the position: " + jobTitle + "\n\nThank you,\nWorkStudy Administration");
            
            mailSender.send(message);
            System.out.println(">> Status update email successfully dispatched to " + toEmail);
        } catch (Exception e) {
            System.err.println(">> Notice: SMTP Email failed to send. Check application.properties.");
            e.printStackTrace();
        }
    }
}
