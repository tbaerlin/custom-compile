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
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataProvider;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.MasterDataBond;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.bonddata.BondDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;

/**
 * Provides basic bonds related static data along with common static data for every instrument.
 * <p>
 * Among the returned static data, <code>bond type</code> is localized. For more detailed static data
 * for a given bond, use {@see BND_DetailedStaticData} instead.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 1635358.qid
 */
public class BndStammdaten extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private BondDataProvider bondDataProvider;

    private CommonDataProvider commonDataProvider;

    public BndStammdaten() {
        super(DefaultSymbolCommand.class);
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
        final MasterDataBond masterData = this.bondDataProvider.getMasterData(quote.getInstrument().getId());
        final String language = LocalizedUtil.getLanguage(masterData);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("masterData", masterData);
        model.put("language", language);

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {
            final List<CommonDataResponse> commonDataResponses =
                this.commonDataProvider.fetchCommonData(
                    language, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

            if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
                CommonDataResponse commonDataResponse = commonDataResponses.get(0);
                model.put("common", commonDataResponse.getFields());
            }
        }

        return new ModelAndView("bndstammdaten", model);
    }

}
