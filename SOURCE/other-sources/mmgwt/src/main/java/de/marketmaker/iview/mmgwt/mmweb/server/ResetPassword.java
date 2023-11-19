/*
 * ResetPassword.java
 *
 * Created on 06.08.2008 10:20:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.http.LocalOrAddressRangePredicate;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class ResetPassword {
    private static final Pattern USERID_PATTERN
            = Pattern.compile("[-_\\p{Digit}\\p{javaLetter}]+");

    private static final byte[] key
            = Base64.decodeBase64("AGlP7qQH43fd1ezlS0uQJg==".getBytes(Charset.forName("US-ASCII")));

    static final String DELIMITER = "§";

    protected final Log logger = LogFactory.getLog(getClass());

    public static class Command {
        private String login;

        private String cmd;

        private String module;

        private String locale = "de";

        private Date date;

        public String getUserid() {
            return login;
        }

        public void setUserid(String userid) {
            this.login = userid;
        }

        public void setCmd(String cmd) throws Exception {
            this.cmd = cmd;
        }

        public String getModule() {
            return module;
        }

        public Date getDate() {
            return date;
        }

        // external parameter to allow for zone-independent password reset
        public void setZone(String zone) {
            this.module = zone;
        }

        void setModule(String module) {
            this.module = module;
        }

        void setDate(Date date) {
            this.date = date;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String encrypt() throws Exception {
            String tmp = StringUtils.collectionToDelimitedString(Arrays.asList(
                this.login, this.module, this.locale, Long.toString(this.date.getTime())
            ), DELIMITER);
            tmp = tmp.hashCode() + DELIMITER + tmp;
            final byte[] encrypted = crypt(Cipher.ENCRYPT_MODE, tmp.getBytes(Charset.forName("UTF8")));
            return new String(encodeBase64(encrypted), Charset.forName("US-ASCII"));
        }

        public String decrypt() throws Exception {
            final byte[] bytes = crypt(Cipher.DECRYPT_MODE,
                    decodeBase64(this.cmd.getBytes(Charset.forName("US-ASCII"))));
            return new String(bytes, Charset.forName("UTF8"));
        }

        private byte[] crypt(int mode, byte[] input) throws Exception {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(input);
        }
    }

    private UserServiceIfc userService;

    private LocalOrAddressRangePredicate localIPCheckPredicate;

    @InitBinder
    public void initBinder(WebDataBinder b, HttpServletRequest request) {
         final LocalOrAddressRangePredicate ipPredicate =this.localIPCheckPredicate;

        b.addValidators(new Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return Command.class.isAssignableFrom(clazz);
            }

            @Override
            public void validate(Object target, Errors errors) {
                final Command c = (Command) target;
                // invoked by customer service, just login specified
                if (isXml(request)) {
                    c.module = getModuleName(c, request);
                    doValidate(c, errors);

                    final String remoteAddr = request.getRemoteAddr();
                    try {
                        final InetAddress address = InetAddress.getByName(remoteAddr);
                        if (!ipPredicate.test(address)) {
                            logger.warn("<validate> rejected access for " + remoteAddr);
                            errors.reject("not.allowed");
                        }
                    } catch (UnknownHostException e) {
                        // should not happen anyways
                        throw new IllegalArgumentException(e);
                    }

                    return;
                }

                // link from email sent to user
                try {
                    final String decrypted = c.decrypt();
                    final String[] tokens = decrypted.split(DELIMITER);
                    if (tokens.length != 5) {
                        errors.reject("invalid.command");
                        return;
                    }
                    final int i = decrypted.indexOf(DELIMITER);
                    if (decrypted.substring(i + 1).hashCode()
                            != Integer.parseInt(tokens[0])) {
                        errors.reject("invalid.command");
                        return;
                    }
                    c.login = tokens[1];
                    c.module = tokens[2];
                    c.locale = tokens[3];
                    c.date = new Date(Long.parseLong(tokens[4]));

                    doValidate(c, errors);
                } catch (Exception e) {
                    errors.reject("invalid.command");
                }
            }
        });
    }

    @Required
    public void setUserService(UserServiceIfc userService) {
        this.userService = userService;
    }

    public void setLocalIPCheckPredicate(LocalOrAddressRangePredicate localIPCheckPredicate) {
        this.localIPCheckPredicate = localIPCheckPredicate;
    }

    /**
     * TODO: refactor, *.xml and *.html seem to use different parameters
     */
    @RequestMapping({ "**/reset-password.html", "**/reset-password.xml" })
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
    @Valid Command c, BindingResult e) throws Exception {

        final HashMap<String, Object> model = new HashMap<>();

        model.put(c.getLocale(), Boolean.TRUE);
        model.put("user", c.getUserid());
        if (e.hasErrors()) {
            final DefaultMessageSourceResolvable r = e.getAllErrors().get(0);
            model.put("error", r.getCode());
            model.put("success", Boolean.FALSE);
        }
        else {
            final boolean success = doResetPassword(c);
            model.put("success", success);
            if (success) {
                model.put("password", this.userService.getConfig(c.getModule()).getInitialPassword());
            }
            else {
                model.put("error", "internal.error");
            }
        }

        if (isXml(request)) {
            return new ModelAndView("mmgwt/resetpassword", model);
        }

        response.setContentType("text/html;charset=utf8");
        return new ModelAndView("mmgwt/resetpassword-html", model);
    }

    private boolean isXml(HttpServletRequest request) {
        return request.getRequestURI().endsWith(".xml");
    }

    private boolean doResetPassword(Command c) {
        try {
            return this.userService.resetPassword(c.getUserid(), c.getModule());
        } catch (Exception e) {
            this.logger.error("<doResetPassword> failed", e);
        }
        return false;
    }

    private void doValidate(Command c, Errors errors) {
        if (c.getUserid() == null || !USERID_PATTERN.matcher(c.getUserid()).matches()) {
            this.logger.warn("<isValid> invalid userid '" + c.getUserid() + "'");
            errors.reject("invalid.command");
        }
        if (c.getDate() != null) {
            final ClientConfig config = this.userService.getConfig(c.getModule());
            final VwdProfile p = (VwdProfile) this.userService.getProfileByLogin(c.getUserid(), config);
            final User u = this.userService.getUserByUid(p.getVwdId());
            if (u.getPasswordChangeDate() != null && u.getPasswordChangeDate().after(c.getDate())) {
                errors.reject("invalid.command");
            }
        }
    }

    private String getModuleName(Command c, HttpServletRequest httpServletRequest) {
        if (StringUtils.hasText(c.getModule())) {
            return c.getModule();
        }

        final String uri = httpServletRequest.getRequestURI();
        final int start = uri.indexOf('/', 1);
        final int end = uri.indexOf('/', start + 1);
        return uri.substring(start + 1, end);
    }
}
