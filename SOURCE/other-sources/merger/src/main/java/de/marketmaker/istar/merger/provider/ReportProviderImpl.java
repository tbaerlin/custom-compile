/*
 * ReportProviderImpl.java
 *
 * Created on 05.03.12 08:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.data.DownloadableItem.Type;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.data.DownloadableItemImpl;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.merger.provider.protobuf.ReportProtos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static de.marketmaker.istar.domain.data.DownloadableItem.Source.VWD;
import static de.marketmaker.istar.merger.provider.ReportProvider.Description.*;

/**
 * @author oflege
 */
public class ReportProviderImpl extends ProtobufDataReader implements ReportProvider {

    private static final Map<DownloadableItem.Source, Map<String, Description>> TYPES;

    private static final MessageSource MESSAGES = MessageSourceFactory.create(ReportProviderImpl.class);

    private static final String SELECTED_MAPPING_STRATEGY = "IsinCode";

    static {
        TYPES = new EnumMap<>(DownloadableItem.Source.class);

        // VWD (V_ISTAR_VWD_FUNDDOCUMENTS, old: V_ISTAR_VWD_FUNDREPORTS)
        {
            final Map<String, Description> types = new HashMap<>();
            types.put("HA", SEMINANNUAL_REPORT);
            types.put("RE", ANNUAL_REPORT);
            types.put("VW", PROSPECTUS);
            types.put("SI", SHORT_PROSPECTUS);
            types.put("KD", KIID);
            TYPES.put(VWD, types);
        }

        // FWW (V_FWW_FUND_REPORTS)
        {
            final Map<String, Description> types = new HashMap<>();
            types.put("HB", SEMINANNUAL_REPORT);
            types.put("RB", ANNUAL_REPORT);
            types.put("VP", PROSPECTUS);
            types.put("KV", SHORT_PROSPECTUS);
            types.put("FS", FACT_SHEET);
            types.put("KI", KIID);
            TYPES.put(DownloadableItem.Source.FWW, types);
        }

        // STOCK_SELECTION (V_ISTAR_STOCKSELECTION_REPORTS)
        {
            final Map<String, Description> types = new HashMap<>();
            types.put("prospectus", PROSPECTUS);
            types.put("termsheet", CONSTITUTIVE_CRITERIA);
            types.put("spectsheet", CONSTITUTIVE_CRITERIA);
            types.put("unfinishedprospectus", UNFINISHED_PROSPECTUS);
            types.put("addendum", ADDENDUM);
            TYPES.put(DownloadableItem.Source.STOCK_SELECTION, types);
        }

        // fund info (V_ISTAR_FUNDINFO_REPORTS)
        {
            final Map<String, Description> types = new HashMap<>();
            types.put("MR", MONTHLY_REPORT);
            types.put("PR", PROSPECTUS);
            types.put("SPR", SHORT_PROSPECTUS);
            types.put("AR", ANNUAL_REPORT);
            types.put("SAR", SEMINANNUAL_REPORT);
            types.put("KID", KIID);
            TYPES.put(DownloadableItem.Source.FUNDINFO, types);
        }

        // SSAT (V_ISTAR_SSAT_FUND_REPORTS)
        {
            final Map<String, Description> types = new HashMap<>();
            types.put("HA", SEMINANNUAL_REPORT);
            types.put("RE", ANNUAL_REPORT); // ?
            types.put("VW", PROSPECTUS);
            types.put("SI", SHORT_PROSPECTUS);
            types.put("KD", KIID);
            TYPES.put(DownloadableItem.Source.SOFTWARESYSTEMSAT, types);
        }
    }

    private static final Map<DownloadableItem.Source, Map<String, DownloadableItem.Type>> TYPE_MAP;

    /*
     * Declaration of report type to description mapping, in case same identifier
     * has different meaning for different sources.
     */
    static {
        TYPE_MAP = new EnumMap<>
                (DownloadableItem.Source.class);

        // for fund info
        HashMap<String, DownloadableItem.Type> types = new HashMap<>();
        types.put("AR", DownloadableItem.Type.Accounts);
        types.put("SAR", DownloadableItem.Type.SemiAnnual);
        types.put("PR", DownloadableItem.Type.Prospectus);
        types.put("SPR", DownloadableItem.Type.ProspectusSimplified);
        types.put("MR", DownloadableItem.Type.FactSheet);
        types.put("KID", DownloadableItem.Type.KIID);
        types.put("PIB", DownloadableItem.Type.PIB);
        TYPE_MAP.put(DownloadableItem.Source.FUNDINFO, types);

        // for software systems at
        types = new HashMap<>();
        types.put("SI", DownloadableItem.Type.ProspectusSimplified);
        types.put("KD", DownloadableItem.Type.KIID);
        types.put("RE", DownloadableItem.Type.Accounts);
        types.put("VW", DownloadableItem.Type.Prospectus);
        types.put("HA", DownloadableItem.Type.SemiAnnual);
        types.put("FS", DownloadableItem.Type.FactSheet);
        TYPE_MAP.put(DownloadableItem.Source.SOFTWARESYSTEMSAT, types);

        // for VWD
        types = new HashMap<>();
        types.put("SI", DownloadableItem.Type.ProspectusSimplified);
        types.put("KD", DownloadableItem.Type.KIID);
        types.put("RE", DownloadableItem.Type.Accounts);
        types.put("VW", DownloadableItem.Type.Prospectus);
        types.put("HA", DownloadableItem.Type.SemiAnnual);
        TYPE_MAP.put(VWD, types);

        // for FWW
        types = new HashMap<>();
        types.put("RB", DownloadableItem.Type.Accounts);
        types.put("HB", DownloadableItem.Type.SemiAnnual);
        types.put("VP", DownloadableItem.Type.Prospectus);
        types.put("KV", DownloadableItem.Type.ProspectusSimplified);
        types.put("KI", DownloadableItem.Type.KIID);
        types.put("FS", DownloadableItem.Type.FactSheet);
        TYPE_MAP.put(DownloadableItem.Source.FWW, types);

        // for stock selection
        types = new HashMap<>();
        types.put("ANNUAL_REPORT", DownloadableItem.Type.Annual);
        types.put("SEMIANNUAL_REPORT", DownloadableItem.Type.SemiAnnual);
        types.put("PROSPECTUS", DownloadableItem.Type.Prospectus);
        types.put("UNFINISHEDPROSPECTUS", DownloadableItem.Type.ProspectusUnfinished);
        types.put("SIMPLIFIED_PROSPECTUS", DownloadableItem.Type.ProspectusSimplified);
        types.put("ADDENDUM", DownloadableItem.Type.Addendum);
        types.put("TERMSHEET", DownloadableItem.Type.TermSheet);
        types.put("SPECTSHEET", DownloadableItem.Type.SpectSheet);
        TYPE_MAP.put(DownloadableItem.Source.STOCK_SELECTION, types);

        // for dz bank
        types = new HashMap<>();
        types.put("Produktinfo", DownloadableItem.Type.PIB);
        types.put("Endgültige Bedingungen", DownloadableItem.Type.Unknown);
        types.put("Union PIF", DownloadableItem.Type.Unknown);
        TYPE_MAP.put(DownloadableItem.Source.DZBANK, types);

        // for fida
        types = new HashMap<>();
        types.put("KD", DownloadableItem.Type.KIID);
        TYPE_MAP.put(DownloadableItem.Source.FIDA, types);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DownloadableItem.Source source;

    public void setSource(String source) {
        setSource(DownloadableItem.Source.valueOf(source));
    }

    public void setSource(DownloadableItem.Source source) {
        this.source = source;
    }

    @Override
    public DownloadableItem.Source getSource() {
        return this.source;
    }

    @Override
    public List<DownloadableItem> getReports(long instrumentid) {
        return getReports(instrumentid, Locale.GERMAN);
    }

    @Override
    public List<DownloadableItem> getReports(long id, Locale locale) {
        ReportProtos.Report.Builder builder = ReportProtos.Report.newBuilder();
        try {
            if (build(id, builder) && builder.isInitialized()) {
                return asReportList(builder.build(), locale);
            }
        } catch (InvalidProtocolBufferException e) {
            this.logger.error("<getReports> failed to deserialize data for " + id, e);
        }
        return Collections.emptyList();
    }

    private List<DownloadableItem> asReportList(ReportProtos.Report reports, Locale locale) {
        final List<DownloadableItem> result
                = new ArrayList<>(reports.getItemsCount());

        final InstrumentTypeEnum instrumentType = reports.hasInstrumenttype()
                ? getInstrumentType(reports.getInstrumenttype()) : null;

        for (int i = 0; i < reports.getItemsCount(); i++) {
            ReportProtos.ReportItem item = reports.getItems(i);

            if (VWD.equals(this.source) && !SELECTED_MAPPING_STRATEGY.equals(item.getMappedby())) {
                continue;
            }

            for (String country : getCountries(item)) {
                final DownloadableItemImpl di
                        = new DownloadableItemImpl(null,
                        getType(item),
                        getDescription(item, locale),
                        toUrl(item.getUrl()),
                        toDateTime(item.hasDate() ? item.getDate() : 0),
                        item.hasFilesize() ? item.getFilesize() : null,
                        this.source,
                        country,
                        item.hasLanguage() ? item.getLanguage() : null,
                        instrumentType,
                        reports.hasMarketAdmission() ? reports.getMarketAdmission() : null,
                        reports.hasPermissionType() ? reports.getPermissionType() : null);
                result.add(di);
            }
        }
        return result;
    }

    private Set<String> getCountries(ReportProtos.ReportItem item) {

        // No country available
        if (!item.hasCountry() && !item.hasCountries()) {
            return Collections.singleton(null);
        }

        // Both available -> combine them and make distinct
        if (item.hasCountry() && item.hasCountries()) {
            return Stream.of(item.getCountry(), item.getCountries())
                    .flatMap(c -> Stream.of(c.split(",")))
                    .collect(Collectors.toSet());
        }

        // Only old Country field used
        if (item.hasCountry()) {
            return Collections.singleton(item.getCountry());
        }

        // Only new countries field used
        return Stream.of(item.getCountries().split(","))
                .collect(Collectors.toSet());
    }

    private String getDescription(ReportProtos.ReportItem item, Locale locale) {
        final String type = item.getType();
        if (DownloadableItem.Source.DZBANK == this.source || DownloadableItem.Source.FIDA == this.source) {
            return type;
        }

        final Map<String, Description> types = TYPES.get(this.source);
        if (null == type || type.trim().length() == 0 || types == null) {
            this.logger.warn("<getType> no report description found under '" + type + "' for source " + source);
            return type;
        }

        final Description description = types.get(type);
        return (description != null) ? MESSAGES.getMessage(description.message, null, locale) : type;
    }

    private InstrumentTypeEnum getInstrumentType(String instrumentType) {
        try {
            return InstrumentTypeEnum.valueOf(instrumentType);
        } catch (IllegalArgumentException e) {
            this.logger.warn("<getInstrumentType> no such type " + instrumentType);
            return null;
        }
    }


    private DownloadableItem.Type getType(ReportProtos.ReportItem item) {
        final String type = item.getType();
        final Map<String, DownloadableItem.Type> types = TYPE_MAP.get(this.source);
        if (null == type || type.trim().length() == 0 || types == null) {
            this.logger.warn("<getType> no report type found under '" + type + "' for source " + source);
            return DownloadableItem.Type.Unknown;
        }

        DownloadableItem.Type result = types.get(type);
        if (result == null) {
            result = types.get(type.toUpperCase());
        }
        if (result == null) {
            if (this.source != DownloadableItem.Source.DZBANK) {
                // DZBANK types may contain wkn etc...
                this.logger.warn("<getType> no report type found under '" + type + "' for source " + source);
            }
            return DownloadableItem.Type.Unknown;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        final ReportProviderImpl rp = new ReportProviderImpl();
        rp.setActiveMonitor(new ActiveMonitor());
        final File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        rp.setFile(new File(dir, "istar-vwd-fund-documents.buf"));
        rp.setSource("VWD");
        rp.afterPropertiesSet();

        final List<DownloadableItem> reports = rp.getReports(228992263L);
        final Map<Type, List<DownloadableItem>> byType = reports.stream()
            .collect(Collectors.groupingBy(DownloadableItem::getType));
        byType.forEach((k,v)->{
            final Map<String, List<DownloadableItem>> byLocale = v.stream()
                .collect(Collectors.groupingBy(di -> di.getLanguage() + "-" + di.getCountry()));
            byLocale.forEach((l,a)->{
                a.sort(Collections.reverseOrder(Comparator.comparing(DownloadableItem::getDate)));
                System.out.println(k+","+l+","+a.get(0).getDate());
            });
        });
    }
}
