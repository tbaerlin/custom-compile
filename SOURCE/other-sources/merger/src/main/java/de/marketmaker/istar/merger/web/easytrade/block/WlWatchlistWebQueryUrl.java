/*
 * WlWatchlistWebQueryUrl.java
 *
 * Created on 13.08.2012 15:51
 *
 * Copyright (c)vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.merger.provider.CachingUserProvider;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

/**
 * @author Markus Dick
 */
public class WlWatchlistWebQueryUrl extends EasytradeCommandController {
    public static final String BASE_URL = "csv/watchlist-webquery.html";

    private CachingUserProvider cachingUserProvider;

    @SuppressWarnings("UnusedDeclaration")
    public static class Command implements UserCommand {
        private String[] watchlistIds;
        private Long companyId;
        private String userId;
        private String orderIds;

        /**
         * @return The watchlist IDs for which Excel Web Query URLs should be generated.
         */
        @NotNull
        public String[] getWatchlistid() {
            return watchlistIds;
        }

        public void setWatchlistid(String[] watchlistIds) {
            this.watchlistIds = watchlistIds;
        }


        @Override
        public void setUserid(String userId) {
            this.userId = userId;
        }

        /**
         * @return The vwd ID of the user who the given watchlist IDs belong to.
         */
        @NotNull
        @Override
        public String getUserid() {
            return userId;
        }

        /**
         * @return The company ID of the given user (usually automatically resolved)
         */
        @MmInternal
        @Override
        public Long getCompanyid() {
            return companyId;
        }

        public void setCompanyid(Long companyId) {
            this.companyId = companyId;
        }

        /**
         * @return The IDs of the table fields, which are selectable in mmf[web]. Usually <code>A</code> through <code>XX</code>. Values must be uppercase and separated by comma.
         */
        public String getOrderids() {
            return orderIds;
        }

        public void setOrderids(String orderIds) {
            this.orderIds = orderIds;
        }
    }

    public WlWatchlistWebQueryUrl() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final Command cmd = (Command)o;
        final String[] watchlistIds = cmd.getWatchlistid();

        final UserContext userContext = this.cachingUserProvider.getUserContext(cmd);
        final User user = userContext.getUser();
        final List<Portfolio> watchlists = user.getWatchlists();
        final HashSet<String> watchlistsOfUser = new HashSet<>();
        for(Portfolio watchlist : watchlists) {
            watchlistsOfUser.add(Long.toString(watchlist.getId()));
        }

        final MoleculeRequest mr = (MoleculeRequest)request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);
        final List<Locale> locales = mr.getLocales();
        final String credentials = getCredentials(user, mr);

        List<String> ids = new ArrayList<>(watchlistIds.length);
        List<String> urls = new ArrayList<>(watchlistIds.length);
        for(String watchlistId : watchlistIds) {
            if(watchlistsOfUser.contains(watchlistId)) {
                ids.add(watchlistId);
                final StringBuilder sb = new StringBuilder(BASE_URL +
                        "?credential=" + URLEncoder.encode(credentials, "UTF-8") +
                        "&userid=" + user.getLogin());

                sb.append("&locale=");
                if(locales != null && locales.size() > 0) {
                    sb.append(locales.get(0).getLanguage());
                }
                else {
                    sb.append("de");
                }
                sb.append("&watchlistid=").append(watchlistId);

                final String orderIds = cmd.getOrderids();
                if(orderIds != null && orderIds.trim().length() > 0) {
                    sb.append("&orderids=");
                    sb.append(URLEncoder.encode(orderIds, "UTF-8"));
                }

                urls.add(sb.toString());
            }
        }

        HashMap<String, Object> m = new HashMap<>();
        m.put("ids", ids);
        m.put("urls", urls);

        return new ModelAndView("wlwatchlistwebqueryurl", m);
    }

    private String getCredentials(User user, MoleculeRequest mr) {
        //This is the case for i.e. mmgwt requests!
        if (mr.getAuthentication() == null && mr.getAuthenticationType() == null) {
            return ProfileUtil.encodeCredential(user.getLogin(), "vwd-ent:ByVwdId");
        }

        return ProfileUtil.encodeCredential(mr.getAuthentication(), mr.getAuthenticationType());
    }

    public void setCachingUserProvider(CachingUserProvider cachingUserProvider) {
        this.cachingUserProvider = cachingUserProvider;
    }
}
