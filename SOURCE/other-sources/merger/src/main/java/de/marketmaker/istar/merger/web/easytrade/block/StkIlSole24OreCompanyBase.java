/*
 * StkIlSole24OreCompanyBase.java
 *
 * Created on 30.08.2012 09:18:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.VwdItCompanyFundamentalsProvider;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Common base class for controllers that provide Il Sole 24 Ore S.p.A. data.
 * @see StkIlSole24OreCompanyData
 * @see StkVwdItCompanyRawdata
 * @see StkVwdItCompanyPdf
 *
 * @author Markus Dick
 */
public abstract class StkIlSole24OreCompanyBase extends EasytradeCommandController {
    public static final String PARAM_ORG_REQUEST = "org=true";

    protected VwdItCompanyFundamentalsProvider companyFundamentalsProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public StkIlSole24OreCompanyBase(Class<? extends SymbolCommand> c) {
        super(c);
    }

    public void setInstrumentProvider(
            EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCompanyFundamentalsProvider(
            VwdItCompanyFundamentalsProvider companyFundamentalsProvider) {
        this.companyFundamentalsProvider = companyFundamentalsProvider;
    }

    protected String getIsin(final DefaultSymbolCommand cmd) {
        final Quote quote = getQuote(cmd);
        return (quote != null) ? quote.getInstrument().getSymbolIsin() : null;
    }

    protected Quote getQuote(final SymbolCommand cmd) {
        // isin may not be accessible under the current profile; but since we just want to use
        // isin as a primary key and not show it to the user, we use an "allow everything" profile
        // to access the isin.
        try {
            return RequestContextHolder.callWith(ProfileFactory.valueOf(true), () -> {
                return instrumentProvider.getQuote(cmd);
            });
        } catch (Exception e) {
            if (e instanceof MergerException) {
                throw (MergerException) e;
            }
            this.logger.warn("<getQuote> failed", e);
            return null;
        }
    }

    protected String getRequest(HttpServletRequest request, String isin, String additionalParameter) {
        final Zone zone = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        final String encodedCredentials = encodeCredentials(request);

        try {
            final StringBuilder sb = new StringBuilder("/dm-resources/" + zone.getName() + "/vwd-it-company.pdf"
                    + "?symbol=" + isin);
            if (additionalParameter != null) {
                sb.append("&").append(additionalParameter);
            }
            sb.append("&credential=").append(URLEncoder.encode(encodedCredentials, "UTF-8"));
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeCredentials(HttpServletRequest request) {
        final MoleculeRequest mr = (MoleculeRequest) request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);

        String authentication = mr.getAuthentication();
        String authenticationType = mr.getAuthenticationType();

        //This is the case for i.e. mmgwt requests!
        if(authentication == null && authenticationType == null) {
            authentication = HttpRequestUtil.getValue(request, ProfileResolver.AUTHENTICATION_KEY);
            authenticationType = HttpRequestUtil.getValue(request, ProfileResolver.AUTHENTICATION_TYPE_KEY);
        }

        return ProfileUtil.encodeCredential(authentication, authenticationType);
    }
}
