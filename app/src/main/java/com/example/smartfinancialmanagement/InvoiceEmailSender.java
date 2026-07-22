package com.example.smartfinancialmanagement;

import java.util.Properties;
import javax.mail.Authenticator;
import java.util.Locale;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class InvoiceEmailSender {

    // Google Account -> Security -> 2-Step Verification -> App Passwords
    private static final String SENDER_EMAIL = "banukakeerthisinghe@gmail.com";
    private static final String SENDER_PASSWORD = "dfnpnstbhdqdbgdm";

    /**
     * Sends an HTML-formatted payment reminder email asynchronously over SMTP.
     */
    public static void sendInvoiceEmail(String recipientEmail, String clientName, String dueDate, double amount) {
        // Run network execution inside an isolated background channel to prevent NetworkOnMainThreadExceptions
        new Thread(() -> {
            try {
                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");

                // Enforce secure Modern Transport Layer Security baselines
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

                message.setSubject("⚠️ Upcoming Invoice Reminder: " + (clientName != null ? clientName : "Client Update"));

                String formattedClient = clientName != null ? clientName : "N/A";
                String formattedDate = dueDate != null ? dueDate : "N/A";

                String emailContent = "<h3>Dear Business Owner,</h3>"
                        + "<p>This is an automated reminder that the invoice for <b>" + formattedClient + "</b> is due tomorrow.</p>"
                        + "<ul>"
                        + "<li><b>Client Name:</b> " + formattedClient + "</li>"
                        + "<li><b>Due Date:</b> " + formattedDate + "</li>"
                        + "<li><b>Total Amount:</b> Rs. " + String.format(Locale.getDefault(), "%,.2f", amount) + "</li>"
                        + "</ul>"
                        + "<p>Please ensure payment reconciliation is processed on time.</p>"
                        + "<br><p>Best Regards,<br><b>Smart Financial Management System</b></p>";

                message.setContent(emailContent, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("✉️ Background Email sent successfully to " + recipientEmail);

            } catch (Exception e) {
                System.out.println("❌ Failed to send email via protocol runner: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}