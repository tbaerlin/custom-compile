/*
 * DelegateReportProviderImpl.java
 *
 * Created on 16.05.12 16:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

import static de.marketmaker.istar.domain.data.DownloadableItem.Source;
import static de.marketmaker.istar.domain.data.DownloadableItem.Type;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toList;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.funddata.FidaProfiler;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.joda.time.DateTime;

/**
 * @author zzhao
 */
public class ReportServiceDelegate {

  private static final List<Source> SOURCE_LIST = Collections.unmodifiableList(Arrays.asList(
      Source.VWD,
      Source.SOFTWARESYSTEMSAT,
      Source.FWW,
      Source.STOCK_SELECTION,
      Source.FUNDINFO,
      Source.FIDA
  ));

  private static final Comparator<DownloadableItem> DATE_SOURCE_COMPARATOR = (o1, o2) -> {
    final DateTime date1 = o1.getDate();
    final DateTime date2 = o2.getDate();
    if (null == date1 && null == date2) {
      return 0;
    } else if (null == date1) {
      return -1;
    } else if (null == date2) {
      return 1;
    } else {
      final int result = date1.compareTo(date2);
      if (result == 0) {
        return SOURCE_LIST.indexOf(o1.getSource()) - SOURCE_LIST.indexOf(o2.getSource());
      }
      return -result; // newer report first
    }
  };

  static final ReportSelectionStrategy DEFAULT_SELECTION_STRATEGY =
      new ReportSelectionStrategy.IncludeOne(SOURCE_LIST);

  private static final Comparator<DownloadableItem> TYPE_COMPARATOR = (o1, o2) -> {
    final Type type1 = o1.getType();
    final Type type2 = o2.getType();
    if (type1 == null && type2 == null) {
      return 0;
    }
    if (type1 == null) {
      return -1;
    }
    if (type2 == null) {
      return 1;
    }
    return type1.name().compareTo(type2.name());
  };

  private static final Comparator<DownloadableItem> SOURCE_COMPARATOR = (o1, o2) -> {
    final Source source1 = o1.getSource();
    final Source source2 = o2.getSource();
    if (source1 == null && source2 == null) {
      return 0;
    }
    if (source1 == null) {
      return -1;
    }
    if (source2 == null) {
      return 1;
    }
    return SOURCE_LIST.indexOf(o1.getSource()) - SOURCE_LIST.indexOf(o2.getSource());
  };

  private static final Comparator<DownloadableItem> COUNTRY_COMPARATOR = (o1, o2) -> {
    final String country1 = o1.getCountry();
    final String country2 = o2.getCountry();
    if (country1 == null && country2 == null) {
      return 0;
    }
    if (country1 == null) {
      return -1;
    }
    if (country2 == null) {
      return 1;
    }
    return country1.compareTo(country2);
  };

  private static final Comparator<DownloadableItem> LANGUAGE_COMPARATOR = (o1, o2) -> {
    final String language1 = o1.getLanguage();
    final String language2 = o2.getLanguage();
    if (language1 == null && language2 == null) {
      return 0;
    }
    if (language1 == null) {
      return -1;
    }
    if (language2 == null) {
      return 1;
    }
    return language1.compareTo(language2);
  };
  /**
   * The newer date on top.
   */
  private static final Comparator<DownloadableItem> DATE_COMPARATOR = (o1, o2) -> {
    final DateTime date1 = o1.getDate();
    final DateTime date2 = o2.getDate();
    if (date1 == null && date2 == null) {
      return 0;
    }
    if (date1 == null) {
      return 1;
    }
    if (date2 == null) {
      return -1;
    }
    return date2.compareTo(date1);
  };

  private static final Comparator<DownloadableItem> ALL_COMPARATOR =
      TYPE_COMPARATOR.thenComparing(SOURCE_COMPARATOR)
          .thenComparing(COUNTRY_COMPARATOR)
          .thenComparing(LANGUAGE_COMPARATOR)
          .thenComparing(DATE_COMPARATOR);

  private static final String EMPTY_KEY = "";
  private ReportService reportService;

  public void setReportService(ReportService reportService) {
    this.reportService = reportService;
  }

  public List<DownloadableItem> getReports(long instrumentId, InstrumentTypeEnum type,
      ReportConstraint constraint) {
    final Profile profile = RequestContextHolder.getRequestContext().getProfile();
    final Locale locale = RequestContextHolder.getRequestContext().getLocale();
    final ReportRequest req = createReportRequest(instrumentId, type, profile, locale);

    final ReportResponse resp = this.reportService.getReports(req);
    if (!resp.isValid()) {
      return Collections.emptyList();
    }

    final Map<Source, List<DownloadableItem>> reports = resp.getReports();
    if (reports.isEmpty()) {
      return Collections.emptyList();
    }

    return selectItems(reports, profile, constraint);
  }

  public boolean isReportsAvailable(long instrumentId, InstrumentTypeEnum type,
      ReportConstraint constraint) {
    return !getReports(instrumentId, type, constraint).isEmpty();
  }

  private List<DownloadableItem> selectItems(Map<Source, List<DownloadableItem>> reports,
      Profile profile, ReportConstraint constraint) {

    // group by type key & source
    final Map<String, Map<Source, List<DownloadableItem>>> typeKeySourceMap =
        reports.values().stream()
            .flatMap(List::stream)
            .collect(groupingBy(d -> getTypeKey(d), groupingBy(
                d -> d.getSource(), () -> new EnumMap<>(Source.class), toList())));

    // selector filtered items
    Map<String, Map<Source, List<DownloadableItem>>> filteredTypeSourceMap = new HashMap<>();
    for (String typeKey : typeKeySourceMap.keySet()) {
      Map<Source, List<DownloadableItem>> items =
          createFilteredMap(typeKeySourceMap.get(typeKey), profile, constraint);
      if (!items.isEmpty()) {
        filteredTypeSourceMap.put(typeKey, items);
      }
    }

    // select number of items per source
    // TODO: decide strategy with help of parameter
    final ReportSelectionStrategy strategy =
        constraint.getSelectionStrategy() == null ? DEFAULT_SELECTION_STRATEGY
            : constraint.getSelectionStrategy();
    final Map<String, Map<Source, List<DownloadableItem>>> sourceSelectedTypeSourceMap =
        strategy.select(filteredTypeSourceMap);

    // select from multiple sources the newest element
    return sourceSelectedTypeSourceMap.values().stream()
        .map(sourceMap -> sourceMap.values().stream()
            .flatMap(List::stream)
            .sorted(DATE_SOURCE_COMPARATOR).collect(toList()))
        .flatMap(
            sorted -> constraint.isOneReportEachType() ? sorted.stream().limit(1) : sorted.stream())
        .sorted(ALL_COMPARATOR)
        //.sorted(TYPE_COMPARATOR)
        .collect(toList());
  }

  /**
   * @param items of the same report type
   */
  private Map<Source, List<DownloadableItem>> createFilteredMap(
      Map<Source, List<DownloadableItem>> items, Profile profile,
      ReportConstraint constraint) {
    final Map<Source, List<DownloadableItem>> result = new HashMap<>();
    items.entrySet().stream().forEach(entry -> {
      final List<DownloadableItem> list = createFilteredList(entry, profile, constraint);

      if (!list.isEmpty()) {
        result.put(entry.getKey(), list);
      }
    });
    return result;
  }

  /**
   * @see #createFilteredMap(Map, Profile, ReportConstraint)
   */
  private List<DownloadableItem> createFilteredList(
      Map.Entry<Source, List<DownloadableItem>> entry,
      Profile profile, ReportConstraint constraint) {
    switch (entry.getKey()) {
      case FUNDINFO:
        return entry.getValue().stream()
            .filter(item -> applyFundInfoSelectors(item, profile))
            .filter(item -> constraint.passLanguageCheck(item))
            .filter(item -> constraint.passCountryCheck(item))
            .collect(toList());
      case VWD: {
        Stream<DownloadableItem> items = entry.getValue().stream()
            .filter(item -> applyVwdSelectors(item, profile))
            .filter(item -> constraint.passLanguageCheck(item))
            .filter(item -> constraint.passCountryCheck(item));
        if (constraint.isNewlyFiltered()) {
          return items
              .filter(item -> constraint.passDateCheck(item))
              .filter(item -> constraint.passTypeCheck(item))
              .filter(item -> constraint.passFilterStrategyCheck(item))
              .collect(toList());
        } else {
          final Map<String, Optional<DownloadableItem>> selected = items
              .collect(groupingBy(notNullOrDefault(DownloadableItem::getLanguage),
                  minBy(
                      DATE_COMPARATOR))); /** DM-482  country constraint already added, now it is relevant to filter by date only*/
          return selected.values().stream()
              .map(Optional::get)
              .collect(toList());
        }
      }
      case SOFTWARESYSTEMSAT:
        return entry.getValue().stream()
            .filter(item -> applySsatSelectors(item, profile))
            .filter(item -> constraint.passLanguageCheck(item))
            .collect(toList());
      case STOCK_SELECTION:
        // enforce stock selection permission
        return entry.getValue().stream()
            .filter(item -> applyStockSelectionSelectors(item, profile))
//                      .filter(item -> constraint.passAllChecks(item))
            .collect(toList());
      case FIDA:
        return entry.getValue().stream()
            //.filter(item -> constraint.passAllChecks(item))
            .filter(item -> FidaProfiler.profileDownloadableItem(profile, item))
            .collect(toList());
      case FWW:
        return new ArrayList<>(entry.getValue());
      case DZBANK:
        return entry.getValue().stream()
            .filter(item -> constraint.passFilterStrategyCheck(item))
            .collect(toList());
      default:
        throw new UnsupportedOperationException("no support for: " + entry.getKey());
    }
  }

  private boolean applyFundInfoSelectors(DownloadableItem item, Profile p) {
    if (Type.FactSheet == item.getType()) {
      return p.isAllowed(Selector.FUNDINFO_FACTSHEET);
    }
    return p.isAllowed(Selector.FUNDINFO_REPORTS);
  }

  private boolean applyVwdSelectors(DownloadableItem item, Profile p) {
    if (Type.KIID == item.getType()) {
      return p.isAllowed(Selector.VWD_FUND_REPORTS_KIID);
    }
    return p.isAllowed(Selector.VWD_FUND_REPORTS);
  }

  private boolean applySsatSelectors(DownloadableItem item, Profile p) {
    if (Type.KIID == item.getType()) {
      return p.isAllowed(Selector.SOFTWARESYSTEMS_AT_REPORTS_KIID);
    } else if (Type.FactSheet == item.getType()) {
      return p.isAllowed(Selector.SOFTWARESYSTEMS_AT_FACTSHEETS);
    }
    return p.isAllowed(Selector.SOFTWARESYSTEMS_AT_REPORTS);
  }

  private boolean applyStockSelectionSelectors(DownloadableItem item, Profile p) {
    if (p instanceof VwdProfile) {
      final VwdProfile vwdProfile = (VwdProfile) p;
      if ("1".equals(vwdProfile.getKonzernId())) {
        return item.getInstrumentType() != InstrumentTypeEnum.FND
            || MarketAdmissionUtil
            .allowByMarketAdmissionStockSelection(p, item.getMarketAdmission());
      }
    }
    return true;
  }

  private Function<DownloadableItem, String> notNullOrDefault(
      Function<DownloadableItem, String> func) {
    return p -> {
      String key = func.apply(p);
      return key == null ? EMPTY_KEY : key;
    };
  }

  private String getTypeKey(DownloadableItem item) {
    if (null == item.getType() || Type.Unknown == item.getType()) {
      return item.getDescription();
    }
    return item.getType().name();
  }

  private ReportRequest createReportRequest(long instrumentId, InstrumentTypeEnum type,
      Profile profile, Locale locale) {
    final ReportRequest req = new ReportRequest(instrumentId);
    if (profile.isAllowed(Selector.VWD_FUND_REPORTS)
        || profile.isAllowed(Selector.VWD_FUND_REPORTS_KIID)) {
      req.addProvider(Source.VWD);
    }
    if (profile.isAllowed(Selector.SOFTWARESYSTEMS_AT_REPORTS)
        || profile.isAllowed(Selector.SOFTWARESYSTEMS_AT_REPORTS_KIID)) {
      req.addProvider(Source.SOFTWARESYSTEMSAT);
    }
    if (profile.isAllowed(Selector.FWW_FUND_REPORTS)) {
      req.addProvider(Source.FWW);
    }
    if ((InstrumentTypeEnum.CER == type
        && profile.isAllowed(Selector.STOCKSELECTION_CERTIFICATE_REPORTS))
        || MarketAdmissionUtil.isStockSelectionAllowed(profile)) {
      req.addProvider(Source.STOCK_SELECTION);
    }
    if (profile.isAllowed(Selector.DZ_BANK_USER)
        || profile.isAllowed(Selector.WGZ_BANK_USER)) {
      req.addProvider(Source.DZBANK);
    }
    if (profile.isAllowed(Selector.FUNDINFO_REPORTS)
        || profile.isAllowed(Selector.FUNDINFO_FACTSHEET)) {
      req.addProvider(Source.FUNDINFO);
    }
    if (profile.isAllowed(Selector.FIDA_FUND_REPORTS_KIID)
        || profile.isAllowed(Selector.FIDA_FUND_REPORTS_KIID_I)) {
      req.addProvider(Source.FIDA);
    }
    if (locale != null) {
      req.setLocale(locale);
    }
    return req;
  }
}