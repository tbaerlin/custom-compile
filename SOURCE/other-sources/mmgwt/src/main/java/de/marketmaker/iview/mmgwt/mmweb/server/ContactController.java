/*
 * ContactController.java
 *
 * Created on 06.08.2008 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.UserMasterData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.activation.FileDataSource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 * generate a contact request email,
 * the form provided contact details are wrapped in a command object,
 * additional user data are fetched from a UserService
 */
@Controller
public class ContactController {
    private final Log logger = LogFactory.getLog(this.getClass());

    public static final String SUBMIT_OK = "SUBMIT_OK";

    public static final String SUBMIT_FAIL = "SUBMIT_FAIL";


    private String[] cc;

    private String from;

    private String replyTo;

    private String[] to;

    private JavaMailSenderImpl mailSender;

    private UserServiceIfc userService;


    public static class Command {

        private String login = "";

        private String vwdId = "";

        private String genoId = "";

        private String gisCustomerId = "";

        private boolean callback;

        private String email = "";

        private String phone = "";

        private String remarks = "";

        private String when = "";


        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getVwdId() {
            return vwdId;
        }

        public void setVwdId(String vwdId) {
            this.vwdId = vwdId;
        }

        public String getGenoId() {
            return genoId;
        }

        public void setGenoId(String genoId) {
            this.genoId = genoId;
        }

        public String getGisCustomerId() {
            return gisCustomerId;
        }

        public void setGisCustomerId(String gisCustomerId) {
            this.gisCustomerId = gisCustomerId;
        }

        public boolean isCallback() {
            return callback;
        }

        public void setCallback(boolean callback) {
            this.callback = callback;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getWhen() {
            return when;
        }

        public void setWhen(String when) {
            this.when = when;
        }

        @Override
        public String toString() {
            return "Command{" +
                    "vwdId='" + vwdId + '\'' +
                    ", genoId='" + genoId + '\'' +
                    ", gisCustomerId='" + gisCustomerId + '\'' +
                    ", callback=" + callback +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", remarks='" + remarks + '\'' +
                    ", when='" + when + '\'' +
                    '}';
        }

    }

    public class FormSubmitMethod {

        private static final String NEWLINE = "\r\n";

        private final Command command;

        private final HttpServletRequest request;

        private final HttpServletResponse response;

        private File attached;

        private String filename;

        private UserMasterData userMasterData;

        public FormSubmitMethod(HttpServletRequest request, HttpServletResponse response, Command command) {
            this.request = request;
            this.response = response;
            this.command = command;
        }

        public void invoke() {
            try {
                sendMail();
                sendResponse(HttpServletResponse.SC_OK, SUBMIT_OK);
            } catch (Exception ex) {
                logger.error("<invoke> failed, help request could not be emailed, command object was: " + command, ex);
                sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        SUBMIT_FAIL + "\n\n" + ex.getClass().getSimpleName() + " " + ex.getMessage());
            } finally {
                cleanup();
            }
        }

        private void cleanup() {
            if (this.attached != null) {
                if (!this.attached.delete()) {
                    logger.warn("<cleanup> trouble deleting file at " + this.attached);
                }
            }
        }

        private void sendMail() throws IOException {
            this.userMasterData = getUserMasterData(this.command.getVwdId());
            if (this.userMasterData == null) {
                throw new IllegalArgumentException("vwdId unbekannt: " + this.command.getVwdId());
            }

            if (request instanceof MultipartHttpServletRequest) {
                final MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) this.request;
                final MultipartFile multipartFile = multipartRequest.getFile("attachment");
                if (multipartFile != null && multipartFile.getSize() > 0) {
                    this.filename = multipartFile.getOriginalFilename();
                    this.attached = File.createTempFile("attached", "uploaded");
                    FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(this.attached));
                    sendMultipart();
                    return;
                }
            }
            sendPlain();
        }

        private String getSubject() {
            return "vwd-ID " + this.userMasterData.getVwdId() + " / " + getServiceName() + " " + getGenoId();
        }

        private String getGenoId() {
            final String result = this.userMasterData.getGenoId();
            return  result != null ? result : "(ohne GenoId)";
        }

        private String getServiceName() {
            String text = this.userMasterData.nodeText("Mandator/Services/ServiceType[@id='10']/Service/Name");
            if (text != null) {
                return text;
            }
            text = this.userMasterData.nodeText("Services/ServiceArt[@id='10']/Service/Name");
            if (text != null) {
                return text;
            }
            return "(ohne Service)";
        }


        private String getContent() {
            final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("E, dd.MM.yyyy HH:mm").withLocale(Locale.GERMAN);
            return Arrays.stream(new String[]{
                    // session data
                    "Datum:            ", dateFormatter.print(new DateTime()), NEWLINE,
                    "Benutzername:     ", this.userMasterData.getFirstName(), " ", userMasterData.getLastName(), NEWLINE,
                    "Institutsname:    ", this.userMasterData.getCentralBank(), NEWLINE,
                    NEWLINE,
                    // form data
                    "Login:            ", this.command.getLogin(), NEWLINE,
                    "GenoId:           ", this.command.getGenoId()!=null?this.command.getGenoId():"", NEWLINE,
                    "Gis-Kundennummer: ", this.command.getGisCustomerId()!=null?this.command.getGisCustomerId():"", NEWLINE,
                    "Telefon-Nr.:      ", this.command.getPhone(), NEWLINE,
                    "E-Mail:           ", this.command.getEmail(), NEWLINE,
                    NEWLINE,
                    "Rückrufwunsch:    ", (this.command.isCallback() ? "Ja" : "Nein"), NEWLINE,
                    "Terminvorschlag:  ", this.command.getWhen(), NEWLINE,
                    NEWLINE,
                    "Beschreibung:     ", this.command.getRemarks(), NEWLINE,
                    NEWLINE,
            }).sequential().collect(Collectors.joining(""));
        }

        private void sendResponse(int code, String result) {
            try {
                this.response.setStatus(code);
                this.response.setContentType("text/plain;charset=UTF-8");
                this.response.getWriter().print(result);
            } catch (IOException e) {
                logger.warn("<sendResponse> failed", e);
            }
        }

        private void sendMultipart() {
            ContactController.this.mailSender.send(new MimeMessagePreparator() {
                @Override
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
                    helper.setFrom(from);
                    helper.setReplyTo(replyTo);
                    helper.setTo(to);
                    if (cc != null) {
                        helper.setCc(cc);
                    }
                    helper.setSubject(getSubject());
                    helper.setText(getContent());
                    helper.addAttachment(filename, new FileDataSource(attached));
                }
            });
        }

        private void sendPlain() {
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setReplyTo(replyTo);
            message.setTo(to);
            if (cc != null) {
                message.setCc(cc);
            }
            message.setSubject(getSubject());
            message.setText(getContent());
            ContactController.this.mailSender.send(message);
        }
    }

    public void setCc(String[] cc) {
        this.cc = Arrays.copyOf(cc, cc.length);
    }

    @Required
    public void setFrom(String from) {
        this.from = from;
    }

    @Required
    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public void setTo(String[] to) {
        this.to = Arrays.copyOf(to, to.length);
    }

    @Required
    public void setUserService(UserServiceIfc userService) {
        this.userService = userService;
    }

    // configured in Settings.properties: contactFormUri=/dmxml-1/dzbank/contact.frm
    @RequestMapping("/dzbank/contact.frm")
    protected void handleContactForm(HttpServletRequest request, HttpServletResponse response, Command command) throws Exception {
        new FormSubmitMethod(request, response, command).invoke();
    }

    private UserMasterData getUserMasterData(final String vwdId) {
        return this.userService.getUserMasterData(vwdId);
    }

}
