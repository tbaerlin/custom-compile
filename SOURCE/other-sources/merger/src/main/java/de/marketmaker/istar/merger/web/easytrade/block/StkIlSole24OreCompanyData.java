/*
 * StkIlSole24OreCompanyData.java
 *
 * Created on 28.08.2012 08:40:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.provider.VwdItCompanyFundamentalsProvider;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal controller that provides Company Data by <i>Il Sole 24 Ore S.p.A.</i>
 * (aka AMF - Analisi Mercati Finanziari) pages for the mm[web] frontend.
 *
 * @see StkVwdItCompanyRawdata
 * @see StkVwdItCompanyPdf
 * @author Markus Dick
 */
  @MmInternal
  public class StkIlSole24OreCompanyData extends StkIlSole24OreCompanyBase {
    public StkIlSole24OreCompanyData() {
        super(StkConvensysData.Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final StkConvensysData.Command cmd = (StkConvensysData.Command)o;
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final Map<String, Object> model = new HashMap<>();
        final VwdItCompanyFundamentalsProvider provider = this.companyFundamentalsProvider;

        if (profile.isAllowed(Selector.IL_SOLE_COMPANY_DATA_XML)) {
            final String isin = getIsin(cmd);
            if(StringUtils.hasText(isin)) {
                final String data = provider.getConvensysContent(isin, cmd.getContentKey());

                if (data != null) {
                    model.put("data", data);

                    Map<String, Object> additionalInformation = provider.getAdditionalInformation(isin);
                    model.put("additionalInformation", additionalInformation);

                    if(profile.isAllowed(Selector.IL_SOLE_COMPANY_DATA_PDF)) {
                        if(Boolean.TRUE.equals(additionalInformation.get("pdf_exists"))) {
                            model.put("pdfRequest", getRequest(request, isin, null));
                        }
                        if(Boolean.TRUE.equals(additionalInformation.get("org_pdf_exists"))) {
                            model.put("orgPdfRequest", getRequest(request, isin, PARAM_ORG_REQUEST));
                        }
                    }
                }
            }
        }

        return new ModelAndView("stkilsole24orecompanydata", model);
    }
}
