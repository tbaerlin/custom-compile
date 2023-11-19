/*
 * MailSupport.java
 *
 * Created on 12.10.2010 14:17:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import de.marketmaker.istar.merger.web.StringWriterResponse;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Supports sending emails based on template views. Use this class as follows
 * <pre>
 * 
 * </pre>
 * @author oflege
 */
public class MailSupport {
    static InternetAddress[] toAddresses(String email) throws Exception {
        return toAddresses(email.split(";"));
    }

    static InternetAddress[] toAddresses(String[] email) throws Exception {
        InternetAddress[] result = new InternetAddress[email.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = toAddress(email[i].trim());
        }
        return result;
    }

    static InternetAddress toAddress(String email) throws Exception {
        final int p = email.indexOf(' ');
        if (p > 0) {
            return toAddress(email.substring(0, p), email.substring(p).trim());
        }
        return new InternetAddress(email);
    }

    static InternetAddress toAddress(String email, String name) throws Exception {
        return new InternetAddress(email, name);
    }

    public class Builder implements MimeMessagePreparator {
        private InternetAddress from;

        private InternetAddress replyTo;

        private List<InternetAddress> to = new ArrayList<>();

        private List<InternetAddress> cc = new ArrayList<>();

        private String text;

        private String subject;

        private Builder() {
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void setFrom(InternetAddress from) {
            this.from = from;
        }

        public void setReplyTo(InternetAddress replyTo) {
            this.replyTo = replyTo;
        }

        public void addTo(InternetAddress... to) {
            this.to.addAll(Arrays.asList(to));
        }

        public void addCc(InternetAddress... cc) {
            this.cc.addAll(Arrays.asList(cc));
        }

        void setView(HttpServletRequest request, String viewName, String module,
                Map<String, Object> model) throws Exception {
            final String content = renderView(request, viewName, module, model);
            this.text = parseContent(content);
        }

        private String parseContent(String content) throws Exception {
            final StringBuilder sb = new StringBuilder(content.length());
            final Scanner sc = new Scanner(content);
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                if (sb.length() == 0) {
                    if (line.startsWith("Subject:")) {
                        this.subject = line.substring("Subject:".length()).trim();
                        continue;
                    }
                    else if (line.startsWith("From:")) {
                        this.from = toAddress(line.substring("From:".length()).trim());
                        continue;
                    }
                    else if (line.startsWith("ReplyTo:")) {
                        this.replyTo = toAddress(line.substring("ReplyTo:".length()).trim());
                        continue;
                    }
                    else if (line.startsWith("To:") && this.to.isEmpty()) {
                        addTo(toAddresses(line.substring("To:".length())));
                        continue;
                    }
                    else if (line.startsWith("Cc:") && this.cc.isEmpty()) {
                        addCc(toAddresses(line.substring("Cc:".length())));
                        continue;
                    }
                }
                sb.append(line).append("\r\n");
            }
            return sb.toString().trim();
        }

        private String renderView(HttpServletRequest request, String viewName, String module,
                Map<String, Object> model) throws Exception {
            final View view = resolveView(viewName, module);
            final StringWriterResponse sw = new StringWriterResponse();
            view.render(model, request, sw);
            return sw.toString().trim();
        }

        private View resolveView(String viewName, String module) throws Exception {
            try {
                return resolveViewName(getViewName(viewName, module));
            }
            catch (Exception e) {
                return resolveViewName(getViewName(viewName, null));
            }
        }

        private String getViewName(String template, String module) {
            StringBuilder sb = new StringBuilder().append("mmgwt/");
            if (module != null) {
                sb.append(module).append("/");
            }
            return sb.append(template).toString();
        }

        public void send() throws Exception {
            if (this.from == null) {
                throw new Exception("from is null");
            }
            if (this.to.isEmpty()) {
                throw new Exception("to is empty");
            }
            if (!StringUtils.hasText(this.subject)) {
                throw new Exception("subject undefined");
            }
            MailSupport.this.send(this);
        }

        public void prepare(MimeMessage message) throws Exception {
            final MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            helper.setFrom(this.from);
            helper.setTo(this.to.toArray(new InternetAddress[this.to.size()]));
            if (!this.cc.isEmpty()) {
                helper.setCc(this.cc.toArray(new InternetAddress[this.cc.size()]));
            }
            helper.setReplyTo(this.replyTo != null ? this.replyTo : this.from);
            helper.setSubject(this.subject);
            helper.setText(this.text, this.text.trim().startsWith("<"));
        }
    }

    private JavaMailSenderImpl mailSender;

    private ViewResolver viewResolver;

    public Builder newBuilder() {
        return new Builder();
    }

    private View resolveViewName(String name) throws Exception {
        return this.viewResolver.resolveViewName(name, Locale.getDefault());
    }

    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public void setViewResolver(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

    private void send(MimeMessagePreparator preparator) {
        this.mailSender.send(preparator);
    }
}
