package com.example.smartfinancialmanagement;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

public class InvoiceEmailSender {

    // (සාමාන්‍ය Gmail Password එක මෙතනට වැඩ කරන්නේ නැත. Google Account -> Security -> App Passwords වෙතින් මෙය සාදාගත යුතුය)
    private static final String SENDER_EMAIL = "your-business-email@gmail.com";
    private static final String SENDER_PASSWORD = "your-16-digit-app-password";

    public static void sendInvoiceEmail(String recipientEmail, String clientName, String dueDate, double amount) {
        new Thread(() -> {
            try {

                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");


                properties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

                message.setSubject("⚠️ Upcoming Invoice Reminder: " + clientName);

                String emailContent = "<h3>Dear Business Owner,</h3>"
                        + "<p>This is an automated reminder that the invoice for <b>" + clientName + "</b> is due tomorrow.</p>"
                        + "<ul>"
                        + "<li><b>Client Name:</b> " + clientName + "</li>"
                        + "<li><b>Due Date:</b> " + dueDate + "</li>"
                        + "<li><b>Total Amount:</b> Rs. " + String.format(Locale.getDefault(), "%,.2f", amount) + "</li>"
                        + "</ul>"
                        + "<p>Please ensure payment reconciliation is processed on time.</p>"
                        + "<br><p>Best Regards,<br><b>Smart Financial Management System</b></p>";

                message.setContent(emailContent, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("✉️ Background Email sent successfully to " + recipientEmail);

            } catch (Exception e) {
                System.out.println("❌ Failed to send email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}