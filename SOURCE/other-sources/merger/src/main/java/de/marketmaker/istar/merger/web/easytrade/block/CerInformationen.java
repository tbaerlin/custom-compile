/*
 * CerInformationen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

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

import de.marketmaker.istar.merger.provider.ProviderPreference;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.RatioFormatter;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.instrument.Certificate;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.IllegalInstrumentTypeException;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.CER;

/**
 * Provides data describing a given certificate.
 * <p>
 * Depending on the block using this service, different set of data is presented.
 * <table border="1">
 * <tr><th>Block</th><th>Data</th></tr>
 * <tr><td>CER_StaticData</td><td>basic data set including basic underlying instrument data</td></tr>
 * <tr><td>CER_StaticDataExtension</td><td>additional data given as attributes.
 * <tr><td>CER_DetailedStaticData</td><td>additional data given as attributes.
 * Each attribute is composed of name, type and value. These additional data is context dependent
 * configured</td></tr>
 * <tr><td>CER_DetailedStaticData</td><td>detailed data set including detailed underlying instrument data</td></tr>
 * </table>
 * </p>
 * <p>
 * Some of the provided data are localized.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 228545.qid
 */
public class CerInformationen extends EasytradeCommandController {
    private final RatioFormatter ratioFormatter = new RatioFormatter();

    private CertificateDataProvider certificateDataProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private CommonDataProvider commonDataProvider;

    private String template = "cerinformationen";

    public static class Command extends DefaultSymbolCommand implements ProviderSelectionCommand {
        private String providerPreference;

        @RestrictedSet("VWD,SMF,SEDEX")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    public CerInformationen() {
        super(Command.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (!(quote.getInstrument() instanceof Certificate)) {
            throw new IllegalInstrumentTypeException(quote.getInstrument(), CER);
        }
        final Certificate certificate = (Certificate) quote.getInstrument();

        final ProviderPreference providerPreference = cmd.getProviderPreference() == null
                ? ProviderPreference.VWD
                : ProviderPreference.valueOf(cmd.getProviderPreference());
        final MasterDataCertificate masterData =
                this.certificateDataProvider.getMasterData(certificate.getId(), providerPreference);

        final String marketStrategy = masterData.getCurrencyStrike() != null
                ? masterData.getCurrencyStrike()
                : null;

        final Quote underlyingQuote = this.instrumentProvider.getUnderlyingQuote(certificate, marketStrategy);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final String language = LocalizedUtil.getLanguage(masterData);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("benchmarkQuote", underlyingQuote);
        model.put("splitratio", certificate.getSubscriptionRatio() != null
                ? this.ratioFormatter.format(certificate.getSubscriptionRatio())
                : "n/a");
        model.put("masterData", masterData);
        model.put("masterDataType", masterData.getType() != null ? masterData.getType().getDefault() : null);
        model.put("language", language);
        model.put("isDZUser", profile.isAllowed(Selector.DZ_BANK_USER));
        model.put("isWGZUser", profile.isAllowed(Selector.WGZ_BANK_USER));

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {
            final List<CommonDataResponse> commonDataResponses =
                this.commonDataProvider.fetchCommonData(
                    language, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

            if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
                CommonDataResponse commonDataResponse = commonDataResponses.get(0);
                model.put("common", commonDataResponse.getFields());
            }
        }

        return new ModelAndView(this.template, model);
    }
}
