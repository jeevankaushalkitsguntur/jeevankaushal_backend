package com.kits.jklub.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    private static final String FROM_EMAIL = "jeevankaushal.kitsguntur@gmail.com";

    /**
     * Send OTP Email
     */
    public void sendOtpEmail(String recipientEmail, String otp) {

        System.out.println("🚀 SendGrid OTP Email Triggered");

        try {
            Email from = new Email(FROM_EMAIL);
            Email to = new Email(recipientEmail);

            String subject = "Jeevan Kaushal Club Verification Code";

            String body = "Hello,\n\n"
                    + "Your verification code is: " + otp + "\n\n"
                    + "This code will expire in 20 minutes.\n"
                    + "If you didn’t request this, please ignore this email.\n\n"
                    + "Regards,\n"
                    + "Jeevan Kaushal Club Team";

            Content content = new Content("text/plain", body);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("✅ Email sent via SendGrid: " + response.getStatusCode());

        } catch (Exception e) {
            System.out.println("❌ SendGrid Email FAILED");
            e.printStackTrace();
        }
    }

    /**
     * Send Contact Message (optional - replaces your old Gmail logic)
     */
    public void sendContactMessage(String senderName, String senderEmail, String messageBody) {

        System.out.println("🚀 SendGrid Contact Email Triggered");

        try {
            Email from = new Email(FROM_EMAIL);
            Email to = new Email(FROM_EMAIL); // sending to yourself

            String subject = "New Contact Message from: " + senderName;

            String body = "Sender Name: " + senderName + "\n"
                    + "Sender Email: " + senderEmail + "\n\n"
                    + "Message:\n" + messageBody;

            Content content = new Content("text/plain", body);

            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("✅ Contact Email Sent: " + response.getStatusCode());

        } catch (Exception e) {
            System.out.println("❌ Contact Email FAILED");
            e.printStackTrace();
        }
    }
}