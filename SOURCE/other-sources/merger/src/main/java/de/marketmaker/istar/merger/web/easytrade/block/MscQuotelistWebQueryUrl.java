/*
 * MscQuotelistWebQueryUrl.java
 *
 * Created on 19.11.2012 15:20:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.HasSymbolArray;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

/**
 * @author Markus Dick
 */
public class MscQuotelistWebQueryUrl extends EasytradeCommandController {
    public static final String BASE_URL = "csv/quotelist-webquery.html";
    private static final String UTF_8 = "UTF-8";
    private static final String LIST_SEPARATOR = ",";

    @SuppressWarnings("UnusedDeclaration")
    public static class Command  implements HasSymbolArray, InitializingBean {
        private String[] symbols;
        private String listid;
        private String[] orderIds;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (this.symbols == null && this.listid == null) {
                throw new BadRequestException("no symbols or listid set");
            }
        }

        /**
         * @return The quote IDs for which Excel Web Query URLs should be generated.
         */
        public String[] getSymbol() {
            return symbols;
        }

        public void setSymbol(String[] symbols) {
            this.symbols = HttpRequestUtil.filterParametersWithText(symbols);
        }

        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
        }

        /**
         * @return The IDs of the table fields, which are selectable in mmf[web]. Usually <code>A</code> through <code>XX</code>. Values must be uppercase and separated by comma.
         */
        public String[] getOrderIds() {
            return orderIds;
        }

        public void setOrderIds(String[] orderIds) {
            this.orderIds = orderIds;
        }
    }

    public MscQuotelistWebQueryUrl() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final Command cmd = (Command)o;
        final String[] symbols = cmd.getSymbol();
        final String listid = cmd.getListid();
        final String[] orderIds = cmd.getOrderIds();

        final MoleculeRequest mr = (MoleculeRequest)request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);
        final List<Locale> locales = mr.getLocales();
        String authentication = mr.getAuthentication();
        String authenticationType = mr.getAuthenticationType();

        //This is the case for i.e. mmgwt requests!
        if(authentication == null && authenticationType == null) {
            HttpSession session = request.getSession(false);
            authentication = (String)session.getAttribute(ProfileResolver.AUTHENTICATION_KEY);
            authenticationType = (String)session.getAttribute(ProfileResolver.AUTHENTICATION_TYPE_KEY);
        }

        final String encodedCredentials = ProfileUtil.encodeCredential(authentication, authenticationType);

        final StringBuilder sb = new StringBuilder(BASE_URL);

        sb.append("?credential=").append(URLEncoder.encode(encodedCredentials, UTF_8));

        sb.append("&locale=");
        if(locales != null && locales.size() > 0) {
            sb.append(locales.get(0).getLanguage());
        }
        else {
            sb.append("de");
        }

        if (symbols != null) {
            sb.append("&symbols=").append(toUrlParamValue(symbols));
        }

        if (listid != null) {
            sb.append("&listid=").append(URLEncoder.encode(listid, UTF_8));
        }

        final String orderIdsParam = toUrlParamValue(orderIds);
        if(StringUtils.hasText(orderIdsParam)) {
            sb.append("&orderIds=").append(orderIdsParam);
        }

        HashMap<String, String> m = new HashMap<>();
        m.put("url", sb.toString());

        return new ModelAndView("mscquotelistwebqueryurl", m);
    }

    private String toUrlParamValue(String[] strings) throws Exception {
        if(strings == null) return "";

        final StringBuilder stringBuilder = new StringBuilder();

        boolean firstIteration = true;
        for(String string : strings) {
            if(firstIteration) {
                firstIteration = false;
            }
            else {
                stringBuilder.append(LIST_SEPARATOR);
            }
            stringBuilder.append(string);
        }

        return URLEncoder.encode(stringBuilder.toString(), UTF_8);
    }
}
