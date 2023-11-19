package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.merger.provider.news.QuotaProvider;
import de.marketmaker.istar.merger.web.StringWriterResponse;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * this class generates and sends an email with the content of a chosen news item
 */
public class NwsEmail extends EasytradeCommandController {

    private static final String VALID_EMAIL_REGEX =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(VALID_EMAIL_REGEX);

    private static final String LIST_SEPARATOR = ";";

    private static final boolean STRICT_ADDRESS_CHECK = true;

    private static final String MESSAGE_FORMAT = ""
            + "{0,choice,0#no messages|1#one message|1<{0,number,integer} messages} could be sent"
            + "{1,choice,0#|1#, one message could not be sent|1<, {1,number,integer} messages could not be sent}";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd. MMM. yyyy HH:mm");

    private static final String TEMPLATE_NAME = "nwsemail-web";


    private NewsProvider newsProvider;

    private AbstractTemplateViewResolver viewResolver;

    private JavaMailSender mailSender;

    private QuotaProvider quotaProvider = QuotaProvider.NO_PERMISSION;


    public static class Command {
        private String vwdId;
        private String newsId;
        private boolean ccToSender;
        private String message;
        private String from;
        private String replyTo;
        private String module;
        private Locale locale;
        private String recipients;
        private String recipientNames;


        /**
         * @return text message that will be included in the email
         */
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @return vwdId
         */
        public String getVwdId() {
            return vwdId;
        }

        public void setVwdId(String vwdId) {
            this.vwdId = vwdId;
        }

        /**
         * @return id of the news that will be send to the recipients
         */
        public String getNewsId() {
            return newsId;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setNewsId(String newsId) {
            this.newsId = newsId;
        }

        /**
         * @return true if the sender of the message will get a carbon copy of each email
         */
        public boolean isCcToSender() {
            return ccToSender;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setCcToSender(boolean ccToSender) {
            this.ccToSender = ccToSender;
        }

        /**
         * @return the from address used in the email
         */
        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        /**
         * @return the reply to address used in the emails
         */
        public String getReplyTo() {
            return replyTo;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }

        /**
         * @return the module for the email template lookup
         */
        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        /**
         * @return the locale used in the email
         */
        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        /**
         * @return one or more recipients email addresses, elements are separated by semicolon
         *         the number must match the number of recipientNames
         */
        public String getRecipients() {
            return recipients;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRecipients(String recipients) {
            this.recipients = recipients;
        }

        /**
         * @return one or more recipients names, elements are separated by semicolon
         *         the number must match the number of recipients
         */
        public String getRecipientNames() {
            return recipientNames;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRecipientNames(String recipientNames) {
            this.recipientNames = recipientNames;
        }

    }

    public NwsEmail() {
        super(Command.class);
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    public void setViewResolver(AbstractTemplateViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setQuotaProvider(QuotaProvider quotaProvider) {
        this.quotaProvider = quotaProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object object, BindException errors) throws Exception {
        final Command command = (Command) object;
        final String vwdId = command.getVwdId();

        final InternetAddress sender;  // mandatory
        if (!isEmailSyntaxValid(command.getFrom())) {
            errors.reject("invalid.from", "from is invalid");
            return null;
        }
        sender = new InternetAddress(command.getFrom(), STRICT_ADDRESS_CHECK);

        final InternetAddress reply;  // optional
        if (StringUtils.isEmpty(command.getReplyTo())) {
            reply = null;
        } else {
            if (!isEmailSyntaxValid(command.getReplyTo())) {
                errors.reject("invalid.replyTo", "error validating replyTo address");
                return null;
            }
            reply = new InternetAddress(command.getReplyTo(), STRICT_ADDRESS_CHECK);
        }

        final List<InternetAddress> recipients;
        try {
            recipients = getValidatedRecipients(command.getRecipients(), command.getRecipientNames());
        } catch (Exception ex) {
            errors.reject("invalid.receiver", "error validating recipients");
            return null;
        }

        if (!this.quotaProvider.acquire(vwdId, recipients.size())) {
            logger.info("email quota limit was exceeded by '" + vwdId + "'");
            errors.reject("invalid.quota", "exceeded quota limit");
            return null;
        }

        final Map<String, Object> emailModel = new HashMap<>();
        final NewsRecord newsRecord;
        try {
            newsRecord = getNewsRecord(command.getNewsId());
            if (newsRecord != null) {
                emailModel.put("news", newsRecord);
                emailModel.put("newsdate", DATE_TIME_FORMATTER.print(newsRecord.getTimestamp()));
                emailModel.put("command", command);
            } else {
                errors.reject("invalid.news", "news for id '" + command.getNewsId() + "' is null");
                return null;
            }
        } catch (Exception ex) {
            errors.reject("invalid.news", "news for id '" + command.getNewsId() + "' not found");
            return null;
        }

        final int[] statistics = {0, 0}; // statistics[0]: success; statistics[1]: failed count
        recipients.forEach(new Consumer<InternetAddress>() {
            @Override
            public void accept(InternetAddress recipient) {
                emailModel.put("recipient", recipient);
                try {
                    sendSingleEmail(request, TEMPLATE_NAME, emailModel, mimeMessage -> {
                        mimeMessage.setRecipient(Message.RecipientType.TO, recipient);
                        if (command.isCcToSender()) {
                            mimeMessage.setRecipient(Message.RecipientType.CC, sender);
                        }
                        if (reply != null) {
                            mimeMessage.setReplyTo(new Address[] {reply});
                        }
                        mimeMessage.setSubject(newsRecord.getHeadline());
                        mimeMessage.setFrom(sender);
                    });
                    statistics[0]++;
                } catch (Exception ex) {
                    logger.warn("exception while sending user created email", ex);
                    statistics[1]++;
                }
            }
        });

        final Map<String, Object> model = new HashMap<>();
        model.put("success", statistics[1] == 0);
        model.put("message",  MessageFormat.format(MESSAGE_FORMAT, statistics[0], statistics[1]));
        return new ModelAndView("nwsemail", model);
    }

    private NewsRecord getNewsRecord(String newsId) throws Exception {
        final NewsRequest request = new NewsRequest();
        request.setNewsids(Collections.singletonList(newsId));
        request.setWithText(true);
        final NewsResponse response = this.newsProvider.getNews(request, false);
        if (response.isValid() && !response.getRecords().isEmpty()) {
            return response.getRecords().get(0);
        }
        return null;
    }

    private void sendSingleEmail(HttpServletRequest request, String viewName,
                                 Map<String, Object> model, MimeMessagePreparator preparator) throws Exception {
        this.mailSender.send(new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                preparator.prepare(mimeMessage);
                mimeMessage.setText(renderEmail(request, viewName, model));
            }
        });
    }

    private String renderEmail(HttpServletRequest request, String viewName, Map<String, Object> model) throws Exception {
        final View view = this.viewResolver.resolveViewName(viewName, Locale.GERMANY);
        final StringWriterResponse stringWriterResponse = new StringWriterResponse();
        view.render(model, request, stringWriterResponse);
        return stringWriterResponse.toString().trim();
    }

    private List<InternetAddress> getValidatedRecipients(String emails, String addresses)
            throws UnsupportedEncodingException, AddressException {
        String[] emailStrings = emails.split(LIST_SEPARATOR);
        String[] nameStrings = addresses.split(LIST_SEPARATOR);
        if (emailStrings.length != nameStrings.length) {
            throw new IllegalArgumentException("counts don't match for recipients and recipientNames");
        }
        ArrayList<InternetAddress> result = new ArrayList<>();
        for (int i = 0; i < emailStrings.length; i++) {
            if (!isEmailSyntaxValid(emailStrings[i])) {
                throw new IllegalArgumentException("unsupported email syntax: '" + emailStrings[i] + "'");
            }
            InternetAddress address = new InternetAddress(emailStrings[i], nameStrings[i]);
            result.add(address);
        }
        if (result.size() == 0) {
            throw new IllegalArgumentException("no receiver found");
        }
        return result;
    }

    private boolean isEmailSyntaxValid(String address) {
        return EMAIL_PATTERN.matcher(address).matches();
    }

}
