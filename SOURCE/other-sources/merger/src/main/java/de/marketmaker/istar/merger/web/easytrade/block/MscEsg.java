/*
 * MscInstruments.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.CategoryScore;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.CategoryScoreImpl;
import de.marketmaker.istar.instrument.search.SearchRequestResultType;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.EsgProvider;
import de.marketmaker.istar.merger.provider.EsgProvider.Response;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Returns for a given symbol the ESG scores of the related instrument.
 * <p>
 * Scores are provided by Clarity. To each score is a relevance associated. Relevance indicates
 * accuracy and importance of the referenced score. A low disclosure of metrics processed by Clarity
 * will lead to a low figure for relevance, e.g. disclosing only 1-2 of 10 metrics by an assessed
 * company leads to a relevance of 10-20%.
 * </p>
 *
 * @author zzhao
 */
public class MscEsg extends EasytradeCommandController {

  private EasytradeInstrumentProvider instrumentProvider;

  private EsgProvider esgProvider;

  public MscEsg() {
    super(DefaultSymbolCommand.class);
  }

  public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
    this.instrumentProvider = instrumentProvider;
  }

  public void setEsgProvider(EsgProvider esgProvider) {
    this.esgProvider = esgProvider;
  }

  protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
      Object o, BindException errors) {
    final Profile profile = RequestContextHolder.getRequestContext().getProfile();
    requireAnySelector(profile, Selector.ESG_SCORE, Selector.ESG_SCORE_DETAIL);

    final DefaultSymbolCommand cmd = (DefaultSymbolCommand) o;
    final Instrument instrument = getInstrument(cmd);
    final Response resp = this.esgProvider.getEsg(instrument.getId());
    if (!resp.isValid()) {
      throw new NoDataException("no ESG found for " + cmd);
    }

    final CategoryScore esgScore = profile.isAllowed(Selector.ESG_SCORE_DETAIL)
        ? resp.getCategoryScore()
        : new CategoryScoreImpl(
            resp.getCategoryScore().getCode(),
            resp.getCategoryScore().getName(),
            resp.getCategoryScore().getTotal());

    final Map<String, Object> model = new HashMap<>();
    model.put("quote", getQuote(cmd, instrument));
    model.put("score", esgScore);
    model.put("language", LocalizedUtil.getLanguage(esgScore));
    return new ModelAndView("mscesg", model);
  }

  private void requireAnySelector(Profile profile, Selector... selectors) {
    if (Arrays.stream(selectors).anyMatch(profile::isAllowed)) {
      return;
    }
    throw new PermissionDeniedException(selectors);
  }

  private Instrument getInstrument(SymbolCommand cmd) {
    final RequestContext oldRC = RequestContextHolder.getRequestContext();
    try {
      RequestContextHolder.setRequestContext(oldRC.withSearchRequestResultType(
          SearchRequestResultType.QUOTE_ANY));
      return this.instrumentProvider.getInstrument(cmd);
    } finally {
      RequestContextHolder.setRequestContext(oldRC);
    }
  }

  private Quote getQuote(SymbolCommand cmd, Instrument instrument) {
    try {
      final Quote quote =
          this.instrumentProvider.getQuote(instrument, cmd.getMarket(), cmd.getMarketStrategy());
      if (null != quote) {
        return quote;
      }
    } catch (Exception e) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("<getQuote> failed getting quote for " + cmd.getSymbol());
      }
    }

    if (instrument.getQuotes().isEmpty()) {
      return null;
    }

    return instrument.getQuotes().get(0);
  }
}
