package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataProvider;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataRequest;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataResponse;
import de.marketmaker.istar.merger.web.ProfileResolver;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created on 27.01.14 09:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class MscCheckMandantId extends EasytradeCommandController {

    private UserMasterDataProvider userMasterDataProvider;

    public void setUserMasterDataProvider(UserMasterDataProvider userMasterDataProvider) {
        this.userMasterDataProvider = userMasterDataProvider;
    }

    public static class Command {
        private String vwdId;
        private String mandantId;

        @NotNull
        public String getVwdId() {
            return this.vwdId;
        }

        public void setVwdId(String vwdId) {
            this.vwdId = vwdId;
        }

        @NotNull
        public String getMandantId() {
            return this.mandantId;
        }

        public void setMandantId(String mandantId) {
            this.mandantId = mandantId;
        }
    }

    MscCheckMandantId() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        final String appId = request.getParameter(ProfileResolver.APPLICATION_ID_KEY);
        final boolean matches = doesMandantIdMatch(cmd.getMandantId(), cmd.getVwdId(), appId);
        response.getWriter().write(String.valueOf(matches));
        return null;
    }

    private boolean doesMandantIdMatch(String mandantId, String vwdId, String appId) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(vwdId);
        }
        catch (Exception e) {
            return false;
        }

        final UserMasterDataRequest request = UserMasterDataRequest.forVwdId(vwdId);
        if (StringUtils.hasText(appId)) {
            request.setAppId(appId);
        }
        final UserMasterDataResponse data = this.userMasterDataProvider.getUserMasterData(request);
        return data.isValid() && mandantId.equals(data.getMasterData().getMandatorId());
    }
}