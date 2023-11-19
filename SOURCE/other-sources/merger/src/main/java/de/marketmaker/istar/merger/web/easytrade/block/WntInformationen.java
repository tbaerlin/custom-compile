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
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.RatioFormatter;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.MasterDataWarrant;
import de.marketmaker.istar.domain.instrument.DerivativeWithStrike;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Option;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.Warrant;
import de.marketmaker.istar.merger.IllegalInstrumentTypeException;
import de.marketmaker.istar.merger.provider.ProviderPreference;
import de.marketmaker.istar.merger.provider.certificatedata.WarrantDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.OPT;
import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.WNT;

/**
 * Queries key warrant data identified by a given financial instrument symbol.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WntInformationen extends EasytradeCommandController {
    private final RatioFormatter ratioFormatter = new RatioFormatter();

    private WarrantDataProvider warrantDataProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private CommonDataProvider commonDataProvider;

    public void setWarrantDataProvider(WarrantDataProvider warrantDataProvider) {
        this.warrantDataProvider = warrantDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }

    public static class Command extends DefaultSymbolCommand implements ProviderSelectionCommand {
        private String providerPreference;

        @RestrictedSet("VWD,SEDEX")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    public WntInformationen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final Instrument instrument = quote.getInstrument();

        final Map<String, Object> model = new HashMap<>();

        if (instrument instanceof Warrant || instrument instanceof Option) {
            final DerivativeWithStrike dws = (DerivativeWithStrike) instrument;
            model.put("splitratio", formatSubscriptionRatio(dws.getSubscriptionRatio()));
            model.put("strike", getStrikeIfPositive(dws.getStrike()));
        } else {
            throw new IllegalInstrumentTypeException(instrument, OPT, WNT);
        }

        final ProviderPreference providerPreference = cmd.getProviderPreference() == null ? ProviderPreference.VWD
                : ProviderPreference.valueOf(cmd.getProviderPreference());
        final MasterDataWarrant masterData = this.warrantDataProvider.getMasterData(instrument.getId(), providerPreference);
        final Quote benchmarkQuote = getBenchmarkQuote(instrument, masterData);

        model.put("quote", quote);
        model.put("benchmarkQuote", benchmarkQuote);
        model.put("masterData", masterData);

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {
            final String language = LocalizedUtil.getLanguage(masterData);
            final List<CommonDataResponse> commonDataResponses =
                this.commonDataProvider.fetchCommonData(
                    language, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

            if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
                CommonDataResponse commonDataResponse = commonDataResponses.get(0);
                model.put("common", commonDataResponse.getFields());
            }
        }

        return new ModelAndView("wntinformationen", model);
    }

    private Quote getBenchmarkQuote(Instrument instrument, MasterDataWarrant masterData) {
        final String marketStrategy = masterData.getCurrencyStrike() != null
                ? masterData.getCurrencyStrike()
                : null;

        return this.instrumentProvider.getUnderlyingQuote(instrument, marketStrategy);
    }

    private String formatSubscriptionRatio(final BigDecimal ratio) {
        return ratio != null ? this.ratioFormatter.format(ratio) : "n/a";
    }

    private BigDecimal getStrikeIfPositive(final BigDecimal strike) {
        return (strike == null || strike.signum() < 0) ? null : strike;
    }
}
