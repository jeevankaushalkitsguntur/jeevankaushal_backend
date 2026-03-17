package com.kits.jklub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service dedicated to sending contact messages to a specified club email address
 * and handles sending OTP emails and generic simple messages.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // The recipient email address as specified for the contact form
    private static final String RECIPIENT_EMAIL = "jeevankaushal.kitsguntur@gmail.com";

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Resolves the "cannot find symbol" error in PasswordResetController.
     * A generic method to send a simple text email.
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Sends the contact message details (name, sender email, message) to the club's inbox.
     * @param senderName The name of the person sending the message.
     * @param senderEmail The email of the person sending the message (used for "Reply To").
     * @param messageBody The content of the user's message.
     */
    public void sendContactMessage(String senderName, String senderEmail, String messageBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(RECIPIENT_EMAIL);
        message.setSubject("Hey, there is a New Message from: " + senderName);
        message.setReplyTo(senderEmail);

        String fullBody = String.format(
                "Sender Name: %s\n" +
                        "Sender Email: %s\n" +
                        "--------------------------------------------------\n" +
                        "Message:\n%s",
                senderName, senderEmail, messageBody
        );

        message.setText(fullBody);
        mailSender.send(message);
    }

    /**
     * Sends the generated OTP to the user's email address with custom formatting.
     * @param recipientEmail The email address to send the OTP to.
     * @param otp The 6-digit OTP string.
     */
    public void sendOtpEmail(String recipientEmail, String otp) {

            System.out.println("🚀 EMAIL METHOD TRIGGERED");

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(recipientEmail);
                message.setSubject("OTP TEST");
                message.setText("Your OTP: " + otp);

                mailSender.send(message);

                System.out.println("✅ EMAIL SENT SUCCESSFULLY");
            } catch (Exception e) {
                System.out.println("❌ EMAIL FAILED");
                e.printStackTrace();
            }
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Jklub One-Time Password (OTP) for Verification");
        System.out.println("Sending OTP to: " + recipientEmail);

        String fullBody = String.format(
                "Dear User,\n\n" +
                        "Your One-Time Password (OTP) for verification is: %s\n\n" +
                        "This OTP is valid for 5 minutes.\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Thanks,\n" +
                        "Jeevan Kaushal Club Team",
                otp
        );

        message.setText(fullBody);
        mailSender.send(message);
    }
}