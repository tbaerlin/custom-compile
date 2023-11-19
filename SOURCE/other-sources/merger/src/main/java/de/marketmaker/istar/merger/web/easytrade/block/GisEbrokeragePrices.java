/*
 * GisEbrokeragePrices.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.ProfileAdapter;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfileAdapterFactory;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendar;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendarProvider;
import de.marketmaker.istar.merger.provider.calendar.TradingDay;
import de.marketmaker.istar.merger.provider.profile.CounterService;
import de.marketmaker.istar.merger.provider.profile.RealtimeDisclaimerProvider;
import de.marketmaker.istar.merger.web.AuditTrailFilter;
import de.marketmaker.istar.merger.web.DataBinderUtils;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * Handling realtime/delayed and counting, see: smb://kenzo/entwicklung/sbs/projekte/2008-01 DZ-BANK/tp-6/ebrokerage-realtimekurse/abstimmung-ebrokerage_kurse_parameter_und_permissionierung_20140827.xlsx
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class GisEbrokeragePrices extends EasytradeCommandController {

    private static final String VIEW_NAME = "gisebrokerageprices";

    private static final String QUALITY = "4";

    @SuppressWarnings("UnusedDeclaration")
    public static class Command {
        private static final Set<String> KUNDE_SET_FOR_V1 = new HashSet<>(Arrays.asList("2290", "8805", "8840"));

        private String bpl;

        private String gbr = "XH"; // default according to spec

        private String kunde;

        private String wkn;

        private String xun;

        private String realtime;

        private String src;

        private String version;

        @Override
        public String toString() {
            return "xun=" + this.xun
                    + ", gbr=" + this.gbr
                    + ", kunde=" + this.kunde
                    + ", wkn=" + this.wkn
                    + ", bpl=" + this.bpl
                    + ", realtime=" + this.realtime
                    + ", src=" + this.src
                    + ", version=" + this.version;
        }

        public String getBpl() {
            return bpl;
        }

        public void setBpl(String bpl) {
            this.bpl = bpl;
        }

        public String getGbr() {
            return gbr;
        }

        public void setGbr(String gbr) {
            this.gbr = gbr;
        }

        public String getKunde() {
            return kunde;
        }

        public void setKunde(String kunde) {
            this.kunde = kunde;
        }

        public String getWkn() {
            return wkn;
        }

        public void setWkn(String wkn) {
            this.wkn = wkn;
        }

        public String getXun() {
            return xun;
        }

        public void setXun(String xun) {
            this.xun = xun;
        }

        @RestrictedSet("0,1")
        public String getRealtime() {
            return realtime;
        }

        public void setRealtime(String realtime) {
            this.realtime = realtime;
        }

        @RestrictedSet("v1,v2")
        public String getVersion() {
            if (this.src == null) {
                return this.realtime == null || isSpecialV1Request() ? "v1" : "v2";
            }
            return "PE".equals(this.src) ? "v1" : "v2";
        }

        private boolean isSpecialV1Request() {
            return "XC".equals(this.gbr) && KUNDE_SET_FOR_V1.contains(this.kunde);
        }

        boolean isVersion1() {
            return "v1".equals(getVersion());
        }

        boolean isVersion2() {
            return "v2".equals(getVersion());
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }
    }

    private static final DataBinderUtils.Mapping PARAMETER_MAPPINGS
            = new DataBinderUtils.Mapping()
            .add("Xun", "xun")
            .add("kunde").add("gbr").add("wkn").add("bpl").add("realtime").add("version").add("src");

    private final VwdProfileAdapterFactory profileFactory = new VwdProfileAdapterFactory();

    private final ResponseExtractor<ProfileAdapter> profileHandler
            = response -> {
        try {
            return profileFactory.read(response.getBody());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    };

    private RealtimeDisclaimerProvider realtimeDisclaimerProvider;

    private CounterService accessCounterV1;

    private CounterService accessCounterV2;

    private EasytradeInstrumentProvider instrumentProvider;

    private IntradayProvider intradayProvider;

    private EntitlementProviderVwd entitlementProvider;

    private ProfileProvider profileProvider;

    private TradingCalendarProvider tradingCalendarProvider;

    public GisEbrokeragePrices() {
        super(Command.class);
    }

    protected ServletRequestDataBinder createBinder(HttpServletRequest request,
            Object object) throws Exception {
        return DataBinderUtils.createBinder(object, PARAMETER_MAPPINGS);
    }

    public void setTradingCalendarProvider(TradingCalendarProvider tradingCalendarProvider) {
        this.tradingCalendarProvider = tradingCalendarProvider;
    }

    public void setEntitlementProvider(EntitlementProviderVwd entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setRealtimeDisclaimerProvider(
            RealtimeDisclaimerProvider realtimeDisclaimerProvider) {
        this.realtimeDisclaimerProvider = realtimeDisclaimerProvider;
    }

    public void setAccessCounterV1(CounterService accessCounterV1) {
        this.accessCounterV1 = accessCounterV1;
    }

    public void setAccessCounterV2(CounterService accessCounterV2) {
        this.accessCounterV2 = accessCounterV2;
    }

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    ProfileResponse getProfile(ProfileRequest request) {
        return profileProvider.getProfile(request);
    }

    RealtimeDisclaimerProvider.Status getDisclaimerStatus(Boolean melNG, String rzid, String rzbk,
            String kdnr) {
        return this.realtimeDisclaimerProvider.getDisclaimerStatus(melNG, rzid, rzbk, kdnr);
    }

    Instrument identifyInstrument(String symbol) {
        return instrumentProvider.identifyInstrument(symbol, SymbolStrategyEnum.AUTO);
    }

    List<PriceRecord> getPriceRecords(List<Quote> quotes) {
        return intradayProvider.getPriceRecords(quotes);
    }

    int getEntitlement(String key, int fid) {
        return entitlementProvider.getEntitlement(key, fid);
    }

    ProfileAdapter countAccess(Command cmd, String selector) {
        final CounterService counterService = getCounterService(cmd);
        return counterService.countAccess(cmd.getGbr() + cmd.getKunde(),
                cmd.getXun(), selector, QUALITY, this.profileHandler);
    }

    TradingDay getTradingDay(Quote q) {
        final TradingCalendar calendar = this.tradingCalendarProvider.calendar(q.getMarket());
        if (calendar != null) {
            return calendar.getTradingDay(new LocalDate(), q);
        }
        return null;
    }

    private CounterService getCounterService(Command cmd) {
        if (cmd.isVersion1()) {
            return this.accessCounterV1;
        }
        if (cmd.isVersion2()) {
            return this.accessCounterV2;
        }
        throw new IllegalStateException("unknown version: " + cmd.getVersion());
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final AuditTrailFilter.Trail audit = AuditTrailFilter.getTrail(request);

        final Map<String, ?> model;
        try {
            model = new GisEbrokeragePricesMethod(this, cmd, audit).invoke();
        } catch (Exception e) {
            audit.add(e);
            throw e;
        }

        return new ModelAndView(VIEW_NAME, model);
    }
}