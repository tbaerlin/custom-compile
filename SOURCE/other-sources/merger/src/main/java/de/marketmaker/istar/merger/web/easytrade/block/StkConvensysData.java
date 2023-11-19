/*
 * StkKennzahlenBilanz.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.ConvensysRawdata;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.CompanyFundamentalsProvider;
import de.marketmaker.istar.merger.provider.ConvensysRawdataRequest;
import de.marketmaker.istar.merger.provider.ConvensysRawdataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Internal controller to provide convensys pages for mm[web] frontend.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
  @MmInternal
  public class StkConvensysData extends EasytradeCommandController {
    public static class Command extends DefaultSymbolCommand {
        private String contentKey = null;

        @NotNull
        public String getContentKey() {
            return contentKey;
        }

        /**
         * Is used in iview/ConvensysPortraitSnippet with dzbank/profiles or dzbank/profiles-en.
         * @return
         */
        @MmInternal
        public void setContentKey(String contentKey) {
            this.contentKey = contentKey;
        }
    }

    public StkConvensysData() {
        super(Command.class);
    }

    protected StkConvensysData(Class aClass) {
        super(aClass);
    }

    private CompanyFundamentalsProvider companyFundamentalsProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setCompanyFundamentalsProvider(
            CompanyFundamentalsProvider companyFundamentalsProvider) {
        this.companyFundamentalsProvider = companyFundamentalsProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected CompanyFundamentalsProvider getCompanyFundamentalsProvider() {
        return this.companyFundamentalsProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        return new ModelAndView("stkconvensysdata", getData((Command) o));
    }

  protected Map<String, Object> getData(final Command cmd) {
    final Map<String, Object> model = new HashMap<>();
    final String isin = getIsin(cmd, model);
    if (StringUtils.hasText(isin)) {
      final String data =
          getCompanyFundamentalsProvider().getConvensysContent(isin, cmd.getContentKey());
      if (data != null) {
        model.put("data", data);
      }
    }
    return model;
  }

  protected String getIsin(final DefaultSymbolCommand cmd, Map<String, Object> model) {
    final Quote quote = getQuote(cmd);
    if (quote != null) {
      model.put("instrument", quote.getInstrument());
      return quote.getInstrument().getSymbolIsin();
    }
    return null;
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

    protected String getRawdata(Instrument instrument, boolean keydata, boolean transformed, boolean metadata) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final ConvensysRawdataRequest request = new ConvensysRawdataRequest(profile, instrument, keydata, transformed, metadata);

        final ConvensysRawdataResponse response = this.companyFundamentalsProvider.getRawdata(request);

        final ConvensysRawdata rawdata = response.getRawdata();

        if (rawdata == null) {
            return null;
        }

        final boolean convensys = profile.isAllowed(Selector.CONVENSYS_I)
                || profile.isAllowed(Selector.CONVENSYS_II);

        final String xsd = getXsd(keydata);

        if (!convensys ||xsd == null) {
            return rawdata.getContent();
        }

        return rawdata.getContent(xsd);
    }

    protected String getXsd(final boolean keydata) {
        return null;
    }
}
