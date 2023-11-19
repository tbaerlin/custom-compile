/*
 * BndStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataProvider;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.MasterDataBond;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.WMDataProvider;
import de.marketmaker.istar.merger.provider.WMDataRequest;
import de.marketmaker.istar.merger.provider.bonddata.BondDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Provides detailed information of a given bond.
 * <p>
 * The static data contained are:
 * <ul>
 * <li>instrument and quote data of the given bond</li>
 * <li>instrument and quote data of the current benchmark for the given bond</li>
 * <li>selected sets of master bond data</li>
 * <li>selected sets of rating data of the given bond</li>
 * </ul>
 * Among selected sets of master bond data, <code>bondType</code> and <code>interestTypeName</code>
 * are localized.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 1635358.qid
 */
public class BndDetailedStaticData extends EasytradeCommandController {
    private final static long DAX_QUOTEID = 106547L;

    private EasytradeInstrumentProvider instrumentProvider;

    private BondDataProvider bondDataProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private RatiosProvider ratiosProvider;

    private WMDataProvider wmDataProvider;

    private CommonDataProvider commonDataProvider;

    public BndDetailedStaticData() {
        super(DefaultSymbolCommand.class);
    }

    public void setWmDataProvider(WMDataProvider wmDataProvider) {
        this.wmDataProvider = wmDataProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setBondDataProvider(BondDataProvider bondDataProvider) {
        this.bondDataProvider = bondDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final long iid = quote.getInstrument().getId();

        final Long benchmarkQuoteId = this.indexCompositionProvider.getBenchmarkId(quote.getInstrument());
        final Quote quoteBenchmark = this.instrumentProvider.identifyQuotes(Arrays.asList(benchmarkQuoteId == null ? DAX_QUOTEID : benchmarkQuoteId)).get(0);
        final MasterDataBond masterData = this.bondDataProvider.getMasterData(iid);
        final String language = LocalizedUtil.getLanguage(masterData);

        final WMDataRequest wmDataRequest =
            new WMDataRequest(RequestContextHolder.getRequestContext().getProfile(), iid);
        final WMData wmData = this.wmDataProvider.getData(wmDataRequest).getData(iid);

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.BND);
        final RatioDataRecord ratioData = this.ratiosProvider.getRatioData(quote, fields);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("quoteBenchmark", quoteBenchmark);
        model.put("masterData", masterData);
        model.put("ratioData", ratioData);
        model.put("wmName", getWmName(quote));
        model.put("language", LocalizedUtil.getLanguage(masterData));
        model.put("wmFieldByName", getFieldsByName(wmData));
        model.put("referenceQuoteInterest", (wmData != null) ? getReference(wmData.getField("GD808E")) : null);

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {
          final List<CommonDataResponse> commonDataResponses =
              this.commonDataProvider.fetchCommonData(
                  language, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

          if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
            CommonDataResponse commonDataResponse = commonDataResponses.get(0);
            model.put("common", commonDataResponse.getFields());
          }
        }

        // TODO: Zinssatz-Referenz auflösen

        return new ModelAndView("bnddetailedstaticdata", model);
    }

    private Map<String, WMData.Field> getFieldsByName(WMData wmData) {
        if (wmData == null) {
            return Collections.emptyMap();
        }
        final Map<String, WMData.Field> result = new HashMap<>();
        for (final WMData.Field field : wmData.getFields()) {
            result.put(field.getName(), field);
        }
        return result;
    }

    private Quote getReference(WMData.Field gd808E) {
        if (gd808E == null) {
            return null;
        }

        final Quote quote;
        try {
            quote = this.instrumentProvider.identifyQuote((String) gd808E.getValue(), SymbolStrategyEnum.WKN, null, null);
        } catch (Exception e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getReference> failed", e);
            }
            return null;
        }

        return quote != null && quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.ZNS ? quote : null;
    }

    private String getWmName(Quote quote) {
        final String nameLang = quote.getSymbolWmWpNameLang();
        final String nameZusatz = quote.getSymbolWmWpNameZusatz();
        if (StringUtils.hasText(nameLang)) {
            return StringUtils.hasText(nameZusatz) ? nameLang + " " + nameZusatz : nameLang;
        }

        return quote.getSymbolWmWpNameKurz();
    }
}