/*
 * NewsEmailController.java
 *
 * Created on 10.02.2009 09:34:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.common.util.ProcessExecutor;
import de.marketmaker.istar.common.validator.ClassValidatorFactory;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class NewsEmailController implements InitializingBean {
    private final Log logger = LogFactory.getLog(NewsEmailController.class);

    public static class Command {
        private boolean ccToSender;

        private String vwdId;

        private String message;

        private String module;

        private String locale;

        private String newsId;

        private String[] recipientNames;

        private String[] recipients;

        private String from;

        private String replyTo;

        @NotNull
        public String getVwdId() {
            return vwdId;
        }

        public String getMessage() {
            return message;
        }

        @NotNull
        public String getModule() {
            return module;
        }

        public String getLocale() {
            return locale;
        }

        @NotNull
        public String getNewsId() {
            return newsId;
        }

        public String[] getRecipientNames() {
            return recipientNames;
        }

        @NotNull
        public String[] getRecipients() {
            return recipients;
        }

        @NotNull
        public String getFrom() {
            return from;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public boolean isCcToSender() {
            return ccToSender;
        }

        public void setCcToSender(boolean ccToSender) {
            this.ccToSender = ccToSender;
        }

        public void setVwdId(String vwdId) {
            this.vwdId = vwdId;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public void setNewsId(String newsId) {
            this.newsId = newsId;
        }

        public void setRecipientNames(String[] recipientNames) {
            this.recipientNames = recipientNames;
        }

        public void setRecipients(String[] recipients) {
            this.recipients = recipients;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }

        @Override
        public String toString() {
            return new StringBuilder(200).append("Command[")
                    .append("module=").append(this.module)
                    .append(", vwdId=").append(this.vwdId)
                    .append(", from=").append(this.from)
                    .append(", replyTo=").append(this.replyTo)
                    .append(", recipients=").append(Arrays.toString(this.recipients))
                    .append(", recipientNames=").append(Arrays.toString(this.recipientNames))
                    .append(", newsId=").append(this.newsId)
                    .append(", message='").append(this.message).append("']")
                    .toString();
        }

        private InternetAddress[] getRecipientAddresses() throws Exception {
            final InternetAddress[] result = new InternetAddress[this.recipientNames.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = MailSupport.toAddress(this.recipients[i], this.recipientNames[i]);
            }
            return result;
        }
    }

    private class FormSubmitMethod {
        private Command command;

        private HttpServletRequest request;

        private HttpServletResponse response;

        public FormSubmitMethod(HttpServletRequest request, HttpServletResponse response,
                                Command command) {
            this.request = request;
            this.response = response;
            this.command = command;
        }

        public void invoke() {
            try {
                doInvoke();
            }
            catch (Exception e) {
                if (e.getCause() instanceof javax.mail.internet.AddressException) {
                    logger.warn("<invoke> failed due to address problems: "
                            + e.getMessage() + ", for " + this.command);
                }
                else {
                    logger.error("<invoke> failed for " + this.command, e);
                }
                sendResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "");
            }
        }

        private void doInvoke() throws Exception {
            final ClientConfig config = userService.getConfig(this.command.getModule());
            final UserMasterData user = getUserMasterData(this.command.getVwdId(), config.getAppId());
            if (user == null) {
                logger.warn("<doInvoke> vwdId unbekannt: " + this.command);
                sendResult(HttpServletResponse.SC_BAD_REQUEST, "vwdId unbekannt: " + command.getVwdId());
                return;
            }
            final NewsRecord news = getNews(this.command.getNewsId(), user.getVwdId(), config.getAppId());
            if (news == null) {
                logger.warn("<doInvoke> newsId unbekannt: " + this.command);
                sendResult(HttpServletResponse.SC_NOT_FOUND, "newsId unbekannt: " + command.getNewsId());
                return;
            }

            final Map<String, Object> model = new HashMap<>();
            model.put("command", command);
            model.put("user", user);
            model.put("news", news);
            model.put("newsdate", dateTimeFormatter.print(news.getTimestamp()));

            final MailSupport.Builder b = mailSupport.newBuilder();
            b.setSubject(defaultSubject);
            b.setView(request, "nwsemail", config.getModuleName(), model);
            b.setFrom(MailSupport.toAddress(command.getFrom(), user.getFirstName() + " " + user.getLastName()));
            if (command.getReplyTo() != null) {
                final InternetAddress replyTo = MailSupport.toAddress(command.getReplyTo(), user.getFirstName() + " " + user.getLastName());
                b.setReplyTo(replyTo);
                if (command.isCcToSender()) {
                    b.addCc(replyTo);
                }
            }
            b.addTo(command.getRecipientAddresses());
            b.send();

            sendResult(HttpServletResponse.SC_OK, "OK");
        }

        private void sendResult(int code, String result) {
            try {
                this.response.setStatus(code);
                this.response.setContentType("text/plain;charset=UTF-8");
                this.response.getWriter().print(result);
            }
            catch (IOException e) {
                logger.warn("<sendResult> failed", e);
            }
        }
    }

    private String defaultSubject = "GIS WebInvestor Nachrichten-Infodienst";

    private NewsProvider newsProvider;

    private UserServiceIfc userService;

    private ProcessExecutor processExecutor;

    private MailSupport mailSupport;

    /**
     * full path to linux's "host" command
     */
    private String hostCommand;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd. MMM. yyyy HH:mm");

    public void setHostCommand(String hostCommand) {
        if (StringUtils.hasText(hostCommand)) {
            this.hostCommand = hostCommand;
        }
    }

    public void setDefaultSubject(String defaultSubject) {
        this.defaultSubject = defaultSubject;
    }

    public void setMailSupport(MailSupport mailSupport) {
        this.mailSupport = mailSupport;
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    public void setNewsDatePattern(String pattern) {
        this.dateTimeFormatter = DateTimeFormat.forPattern(pattern);
    }

    public void setUserService(UserServiceIfc userService) {
        this.userService = userService;
    }

    public void setProcessExecutor(ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.processExecutor == null || this.hostCommand == null) {
            this.logger.info("<afterPropertiesSet> will not check email domains");
        }
    }

    @RequestMapping("**/newsemail.frm")
    protected ModelAndView sendEmail(HttpServletRequest request,
                                  HttpServletResponse response, Command cmd, BindingResult e) throws Exception {
        final FormSubmitMethod method = new FormSubmitMethod(request, response, cmd);
        if (e.hasErrors()) {
            final DefaultMessageSourceResolvable r = e.getAllErrors().get(0);
            method.sendResult(HttpServletResponse.SC_BAD_REQUEST, r.getCode() + ": " + r.getDefaultMessage());
            return null;
        }
        if (!isAddressOk(cmd.getFrom())) {
            final String msg = "invalid sender/from: '" + cmd.getFrom() + "'";
            this.logger.warn("<handle> " + msg);
            method.sendResult(HttpServletResponse.SC_BAD_REQUEST, msg);
            return null;
        }
        if (!isAddressOk(cmd.getReplyTo())) {
            final String msg = "invalid sender/replyTo: '" + cmd.getReplyTo() + "'";
            this.logger.warn("<handle> " + msg);
            method.sendResult(HttpServletResponse.SC_BAD_REQUEST, msg);
            return null;
        }
        method.invoke();
        return null;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(ClassValidatorFactory.forClass(Command.class));
    }

    private NewsRecord getNews(String newsid, String vwdId, String appId) throws Exception {
        final NewsRequest request = new NewsRequest();
        request.setNewsids(Collections.singletonList(newsid));
        request.setWithText(true);
        final Profile profile = this.userService.getProfileByVwdId(vwdId, appId);
        RequestContextHolder.setRequestContext(new RequestContext(profile, LbbwMarketStrategy.INSTANCE));
        try {
            final NewsResponse response = newsProvider.getNews(request, false);
            if (response.isValid() && !response.getRecords().isEmpty()) {
                return response.getRecords().get(0);
            }
            return null;            
        }
        finally {
            RequestContextHolder.setRequestContext(null);
        }
    }

    private UserMasterData getUserMasterData(final String vwdId, String appId) {
        return this.userService.getUserMasterData(vwdId, appId);
    }

    private boolean isAddressOk(String sender) {
        final int p = sender.indexOf('@');
        if (p == -1 || p != sender.lastIndexOf('@') || sender.substring(p + 1).indexOf('.') == -1) {
            return false;
        }
        if (this.processExecutor == null || this.hostCommand == null) {
            return true;
        }

        final String domain = sender.substring(p + 1);
        final ProcessExecutor.Result result =
                this.processExecutor.execute(this.hostCommand, "-t", "mx", domain);
        return result != null && result.getReturnCode() == 0;
    }
}
