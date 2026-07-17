package com.example.smartfinancialmanagement;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class InvoiceEmailSender {

    // 💡 වැදගත්: මෙතනට ඔබගේ Gmail ලිපිනය සහ Gmail App Password එක ලබා දෙන්න
    // (සාමාන්‍ය Gmail Password එක මෙතනට වැඩ කරන්නේ නැත. Google Account -> Security -> App Passwords වෙතින් මෙය සදාගත යුතුය)
    private static final String SENDER_EMAIL = "your-business-email@gmail.com";
    private static final String SENDER_PASSWORD = "your-16-digit-app-password";

    public static void sendInvoiceEmail(String recipientEmail, String clientName, String dueDate, double amount) {
        // ඇන්ඩ්‍රොයිඩ් හි මේන් ත්‍රෙඩ් එක බ්ලොක් නොවීම සඳහා වෙනම Background Thread එකක් භාවිතා කරයි
        new Thread(() -> {
            try {
                // SMTP සේවා සැකසුම් (Gmail සඳහා)
                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");

                // ඊමේල් සෙෂන් එකක් ආරම්භ කිරීම
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                // ඊමේල් පණිවිඩය නිර්මාණය කිරීම
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

                // 💡 ඊමේල් මාතෘකාව (Subject)
                message.setSubject("⚠️ Upcoming Invoice Reminder: " + clientName);

                // 💡 ඊමේල් අන්තර්ගතය (HTML පණිවිඩයක් ලෙස ලස්සනට පෙන්වීමට)
                String emailContent = "<h3>Dear Business Owner,</h3>"
                        + "<p>This is an automated reminder that the invoice for <b>" + clientName + "</b> is due tomorrow.</p>"
                        + "<ul>"
                        + "<li><b>Client Name:</b> " + clientName + "</li>"
                        + "<li><b>Due Date:</b> " + dueDate + "</li>"
                        + "<li><b>Total Amount:</b> Rs. " + String.format("%.2f", amount) + "</li>"
                        + "</ul>"
                        + "<p>Please ensure payment reconciliation is processed on time.</p>"
                        + "<br><p>Best Regards,<br><b>Smart Financial Management System</b></p>";

                message.setContent(emailContent, "text/html; charset=utf-8");

                // ඊමේල් එක පිටත් කිරීම
                Transport.send(message);
                System.out.println("✉️ Background Email sent successfully to " + recipientEmail);

            } catch (Exception e) {
                System.out.println("❌ Failed to send email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}