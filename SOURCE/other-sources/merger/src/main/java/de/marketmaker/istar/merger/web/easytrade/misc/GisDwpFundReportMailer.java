/*
 * GisDwpFundReportMailer.java
 *
 * Created on 12.03.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.net.MalformedURLException;
import java.net.URL;

import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.ClassValidator;
import de.marketmaker.istar.common.validator.ClassValidatorFactory;
import de.marketmaker.istar.common.validator.NotNull;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class GisDwpFundReportMailer {
    public static class Command {
        private String symbol;
        private String type;
        private String url;
        private String cust_id;
        private String emailNameTo;
        private String emailNameFrom;
        private String emailTo;
        private String emailBody;

        @NotNull
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        @NotNull
        public String getCust_id() {
            return cust_id;
        }

        public void setCust_id(String cust_id) {
            this.cust_id = cust_id;
        }

        @NotNull
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @NotNull
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @NotNull
        public String getEmailNameTo() {
            return emailNameTo;
        }

        public void setEmailNameTo(String emailNameTo) {
            this.emailNameTo = emailNameTo;
        }

        @NotNull
        public String getEmailNameFrom() {
            return emailNameFrom;
        }

        public void setEmailNameFrom(String emailNameFrom) {
            this.emailNameFrom = emailNameFrom;
        }

        @NotNull
        public String getEmailTo() {
            return emailTo;
        }

        public void setEmailTo(String emailTo) {
            this.emailTo = emailTo;
        }

        @NotNull
        public String getEmailBody() {
            return emailBody;
        }

        public void setEmailBody(String emailBody) {
            this.emailBody = emailBody;
        }

        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String from;

    private String replyTo;

    private JavaMailSenderImpl mailSender;

    private FundReportCounter fundReportCounter;

    private final ClassValidator validator = ClassValidatorFactory.forClass(Command.class);

    @Required
    public void setFrom(String from) {
        this.from = from;
    }

    @Required
    public void setMailSender(org.springframework.mail.javamail.JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public void setFundReportCounter(FundReportCounter fundReportCounter) {
        this.fundReportCounter = fundReportCounter;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(this.validator);
    }

    @RequestMapping(value = "**/wpdirect/sendemail.frm")
    protected ModelAndView handle(@Valid Command cmd, HttpServletResponse response)
            throws Exception {
        final boolean success = send(cmd);

        response.setContentType("text/html; charset=UTF-8");
        return new ModelAndView("wpdirect/wpdirect_mail_result", "success", success);
    }

    private boolean send(final Command cmd) {
        if (!this.fundReportCounter.countAccess(cmd.getCust_id())) {
            return false;
        }

        try {
            this.mailSender.send(new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    GisDwpFundReportMailer.this.prepare(mimeMessage, cmd);
                }
            });
            return true;
        }
        catch (Exception e) {
            this.logger.warn("<send> failed for " + cmd, e);
            return false;
        }
    }

    private void prepare(MimeMessage mimeMessage, Command cmd)
            throws MessagingException, MalformedURLException {
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(this.from);
        if (this.replyTo != null) {
            helper.setReplyTo(this.replyTo);
        }
        helper.setTo(cmd.getEmailNameTo() + "<" + cmd.getEmailTo() + ">");
        helper.setSubject("Eine Information Ihres Wertpapierberaters " + cmd.getEmailNameFrom());
        helper.setText(cmd.getEmailBody() + "\r\n \r\n");

        helper.addAttachment(cmd.getSymbol() + "_" + cmd.getType() + ".pdf",
                new URLDataSource(new URL(cmd.getUrl())));
    }

    public static void main(String[] args) {
        Command command = new Command();
        System.out.println(command.toString());
    }
}
