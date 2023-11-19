/*
 * StkVwdItCompanyPdf.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkVwdItCompanyPdf extends StkIlSole24OreCompanyBase {
    public static class Command extends DefaultSymbolCommand {
        private boolean org;

        public boolean isOrg() {
            return org;
        }

        public void setOrg(boolean org) {
            this.org = org;
        }
    }

    public StkVwdItCompanyPdf() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        try {
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            if (!profile.isAllowed(Selector.IL_SOLE_COMPANY_DATA_PDF)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }

            final Command cmd = (Command) o;
            final String isin = getIsin(cmd);

            if (StringUtils.hasText(isin)) {
                final byte[] pdf = cmd.isOrg()
                        ? this.companyFundamentalsProvider.getOrganisationStructurePdf(isin)
                        : this.companyFundamentalsProvider.getPortraitPdf(isin);

                if (pdf != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/pdf");
                    response.setContentLength(pdf.length);
                    response.getOutputStream().write(pdf);
                    return null;
                }
            }

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } catch (Exception e) {
            throw new InternalFailure("failed", e);
        }
    }
}