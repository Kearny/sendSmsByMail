package satbe.kearny.com.sendalltextosbyemail;

import android.util.Log;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SSLMailSender {
    public static void sendMail(String from, String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("alexandre.liscia@gmail.com", "nhtrgbewywptsxhe");
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body,"text/html; charset=utf8");

            Transport.send(message);

            Log.i("SSLMailSender", "Done");

        } catch (MessagingException e) {
            Log.i("SSLMailSender", e.getMessage(), e);
        }
    }
}