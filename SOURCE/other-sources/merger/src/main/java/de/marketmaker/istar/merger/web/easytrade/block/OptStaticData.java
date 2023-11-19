/*
 * CerInformationen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataProvider;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.OptionDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.opra.OpraItem;

/**
 * Queries <b>key option data</b> identified by a given financial <b>instrument symbol</b>. The
 * subject security is selected based on the given symbol w/o <b>symbol strategy</b> and market w/o
 * <b>market strategy</b>.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OptStaticData extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    protected RatiosProvider ratiosProvider;

    private CommonDataProvider commonDataProvider;

    public OptStaticData() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final SymbolCommand cmd = (SymbolCommand) o;

        final Instrument instrument = this.instrumentProvider.getInstrument(cmd);
        final Quote underlyingQuote = this.instrumentProvider.getUnderlyingQuote(instrument, null);
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("underlyingQuote", underlyingQuote);
        if (instrument instanceof OptionDp2) {
            final OptionDp2 option = (OptionDp2) instrument;
            model.put("strike", option.getStrike() != null && option.getStrike().doubleValue() < 0 ? null : option.getStrike());
        }

        if (InstrumentUtil.isOpraInstrument(instrument)) {
            RatioSearchRequest opraRequest = new RatioSearchRequest(ProfileFactory.valueOf(true));
            opraRequest.setType(InstrumentTypeEnum.OPT);
            opraRequest.addParameter("vwdCode", quote.getSymbolVwdcode());
            List<OpraItem> opraItems = ratiosProvider.getOpraItems(opraRequest).getItems();

            if (opraItems.size() > 0) {
                long contractSize = opraItems.get(0).getContractSize();
                model.put("contractSize", contractSize == 0 ? null : PriceCoder.decode(contractSize));
            }
        }
        else {
            final RatioDataRecord ratioData = this.ratiosProvider.getRatioData(quote,
                    AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.OPT));
            if (null != ratioData) {
                model.put("contractSize", ratioData.getContractSize());
                model.put("contractValue", ratioData.getContractValue());
                model.put("generationNumber", ratioData.getGenerationNumber());
                model.put("versionNumber", ratioData.getVersionNumber());
            }
        }

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {
            final List<CommonDataResponse> commonDataResponses =
                this.commonDataProvider.fetchCommonData(
                    null, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

            if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
                CommonDataResponse commonDataResponse = commonDataResponses.get(0);
                model.put("common", commonDataResponse.getFields());
            }
        }

        return new ModelAndView("optstaticdata", model);
    }
}