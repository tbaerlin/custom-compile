/*
 * EerReleaseDetail.java
 *
 * Created on 21.03.12 10:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.econoday;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.econoday.EconodayProvider;
import de.marketmaker.istar.merger.provider.econoday.ReleaseDetail;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Provides detailed information of given releases of economic calendar events.
 * <p>
 * Each event release of economic calendar is identified by an unique id. Given those ids this service
 * will deliver the detailed information including:
 * <ul>
 * <li>highlights in text</li>
 * <li>consensus notes in text</li>
 * <li>assessments in value</li>
 * <li>URLs of available charts and/or grids</li>
 * </ul>
 * </p>
 *
 * @author zzhao
 */
public class EdDetail extends EasytradeCommandController {

    public static class Command {
        private int[] releaseId;

        /**
         * An array of release ids, for which the release details are queried.
         *
         * @return an array of release ids.
         */
        public int[] getReleaseId() {
            return releaseId;
        }

        public void setReleaseId(int[] releaseId) {
            this.releaseId = releaseId;
        }
    }

    private EconodayProvider econodayProvider;

    public EdDetail() {
        super(Command.class);
    }

    public void setEconodayProvider(EconodayProvider econodayProvider) {
        this.econodayProvider = econodayProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!profile.isAllowed(Selector.ECONODAY)) {
            throw new PermissionDeniedException(Selector.ECONODAY.getId());
        }

        final MoleculeRequest mr =
                (MoleculeRequest) request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);
        final String encodedCredentials
                = ProfileUtil.encodeCredential(mr.getAuthentication(), mr.getAuthenticationType());
        final String credential = "&credential=" + URLEncoder.encode(encodedCredentials, "UTF-8");

        final Command cmd = (Command) o;
        final Map<Integer, ReleaseDetail> details = this.econodayProvider.getReleaseDetails(cmd.getReleaseId());

        final HashMap<String, Object> model = new HashMap<>(5);
        model.put("details", details);
        model.put("credential", credential);
        model.put("imgUrlPrefix", "econoday.jpg?id=");
        return new ModelAndView("eddetail", model);
    }
}
