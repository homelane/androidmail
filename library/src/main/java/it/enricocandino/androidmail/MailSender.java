package it.enricocandino.androidmail;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.security.Security;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import it.enricocandino.androidmail.model.Attachment;
import it.enricocandino.androidmail.model.Mail;
import it.enricocandino.androidmail.model.Recipient;
import it.enricocandino.androidmail.provider.GmailProvider;
import it.enricocandino.androidmail.provider.JSSEProvider;
import it.enricocandino.androidmail.provider.MailProvider;

/**
 * Copyright (c) 2016 Enrico Candino
 * <p/>
 * Distributed under the MIT License.
 */
public class MailSender extends javax.mail.Authenticator {

    static {
        Security.addProvider(new JSSEProvider());
    }

    private String user;
    private String password;
    private Session session;

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;

        MailProvider mailProvider = new GmailProvider();
        session = Session.getDefaultInstance(mailProvider.getProperties(), this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public void sendMail(Mail mail) throws Exception {

        MimeMessage message = new MimeMessage(session);

        message.setSender(new InternetAddress(mail.getSender()));
        message.setSubject(mail.getSubject());

        for (Recipient recipient : mail.getRecipients()) {
            Message.RecipientType recipientType = null;
            switch (recipient.getType()) {
                case TO:
                    recipientType = Message.RecipientType.TO;
                    break;
                case CC:
                    recipientType = Message.RecipientType.CC;
                    break;
                case BCC:
                    recipientType = Message.RecipientType.BCC;
                    break;
            }
            message.addRecipient(recipientType, new InternetAddress(recipient.getAddress()));
        }

        Multipart multipart = new MimeMultipart();

        if (mail.getText() != null) {
            MimeBodyPart textBodyPart = new MimeBodyPart();
            ByteArrayDataSource textDatasource = new ByteArrayDataSource(mail.getText().getBytes(), "text/plain");
            DataHandler handler = new DataHandler(textDatasource);
            textBodyPart.setDataHandler(handler);
            multipart.addBodyPart(textBodyPart);
        }

        if (mail.getHtml() != null) {
            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            ByteArrayDataSource htmlDatasource = new ByteArrayDataSource(mail.getHtml().getBytes(), "text/html");
            DataHandler handler = new DataHandler(htmlDatasource);
            htmlBodyPart.setDataHandler(handler);
            multipart.addBodyPart(htmlBodyPart);
        }

        if (!mail.getAttachments().isEmpty()) {
            for (Attachment attachment : mail.getAttachments()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment.getPath());
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(attachment.getFilename());
                multipart.addBodyPart(attachmentBodyPart);
            }
        }

        message.setContent(multipart);

        Transport.send(message);
    }

    public void sendMailAsync(Mail mail, OnMailSentListener listener) {
        new MailTask(listener).execute(mail);
    }

    public interface OnMailSentListener {
        void onSuccess();

        void onError(Exception error);
    }

    private class MailTask extends AsyncTask<Mail, Void, Exception> {

        private final WeakReference<OnMailSentListener> listener;

        public MailTask(OnMailSentListener listener) {
            this.listener = new WeakReference<>(listener);
        }

        @Override
        protected Exception doInBackground(Mail... params) {

            try {
                sendMail(params[0]);
            } catch (Exception e) {
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception error) {

            if (listener.get() != null) {
                if (error == null) {
                    listener.get().onSuccess();
                } else {
                    listener.get().onError(error);
                }
            }
        }
    }
}
