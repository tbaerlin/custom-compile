/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.PreferSwissQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * Queries certificates that match given criteria.
 * <p>
 * Criteria are specified by the <code>query</code> parameter, which is composed of field predicates.
 * Note that some fields have only limited values. Those values can be queried using
 * {@see CER_FinderMetadata}.
 * </p>
 * <p>
 * Allowed search fields can be found in the sort field lists in the response.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample query isin==DE0007
 */
public class CerFinder extends AbstractFindersuchergebnis {
    private static final LocalDate ENDLESS_EXPIRATION = new LocalDate(2099, 12, 30);

    private static final Set<String> EXPIRES_PARAMETERS = new HashSet<>(Arrays.asList("expires", "expires:L", "expires:U"));

    public CerFinder() {
        super(ProviderCommand.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final ProviderCommand cmd = (ProviderCommand) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields = getFields(InstrumentTypeEnum.CER, cmd.getProviderPreference());

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.CER, cmd, fields);
        if (cmd.getIid() != null) {
            rsr.setInstrumentIds(Arrays.asList(cmd.getIid()));
        }

        // TODO: remove after Update in mmf3
        if ("SMF".equals(cmd.getProviderPreference())) {
            rsr.setDataRecordStrategyClass(PreferSwissQuoteStrategy.class);
        }

        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        replaceUnderlyingSymbol(parameters, cmd.isWithSiblingsForUnderlying());

        // TODO: hack for mmf3 for handling special feed values of endless certificates (2099-12-30)
        if (!Collections.disjoint(parameters.keySet(), EXPIRES_PARAMETERS)) {
            for (final String ep : EXPIRES_PARAMETERS) {
                final String v = parameters.get(ep);

                if (!StringUtils.hasText(v)) {
                    continue;
                }
                final LocalDate d = DateUtil.parseDate(v).toLocalDate();
                if (d.equals(ENDLESS_EXPIRATION)) {
                    parameters.remove(ep);
                    parameters.put(RatioFieldDescription.gatrixxIsEndless.name(), "true");
                }
            }
        }

        if (cmd.getFieldForResultCount() != null) {
            rsr.setFieldidForResultCount(cmd.getFieldForResultCount().id());

            final String fieldname = cmd.getFieldForResultCount().name().toLowerCase();
            if (parameters.containsKey(fieldname)) {
                rsr.setFilterForResultCount(parameters.remove(fieldname));
            }
        }

        rsr.addParameters(parameters);

        final List<String> sortfields = asSortfields(fields);

        if (cmd.getSortBy() != null && cmd.getSortBy().indexOf(",") > 0) {
            final String[] strings = cmd.getSortBy().split(",");
            cmd.setSortBy(strings[0]);

            final String sortfield = fields.get(RatioDataRecord.Field.valueOf(strings[1])).name();
            rsr.addParameter("sort2", sortfield);
        }

        final ListResult listResult = ListResult.create(cmd, sortfields, "name", 0);
        final String sortfield = fields.get(RatioDataRecord.Field.valueOf(listResult.getSortedBy())).name();
        rsr.addParameter("sort1", sortfield);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));
        if (rsr.getParameters().containsKey("sort2")) {
            rsr.addParameter("sort2:D", Boolean.toString(!listResult.isAscending()));
        }

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, isWithPrices(cmd, false), false);

        addCounts(sr, model);

        return new ModelAndView("cerfinder", model);
    }

    private void addCounts(RatioSearchResponse sr, Map<String, Object> model) {
        final Map<Object, Integer> counts = ((DefaultRatioSearchResponse) sr).getResultGroupCount();
        final int aktienanleihenCount = getCount(counts, "AKTIENANLEIHE");
        final int sprinterCount = getCount(counts, "SPRINTER");
        final int bonusCount = getCount(counts, "BONUS");
        final int discountCount = getCount(counts, "DISCOUNT");
        final int knockoutCount = getCount(counts, "KNOCKOUT");
        final int indexCount = getCount(counts, "INDEX");
        final int garantieCount = getCount(counts, "GARANTIE");
//        final int sonstigCount = drsr.getNumTotal() - aktienanleihenCount - sprinterCount - bonusCount - discountCount - knockoutCount - indexCount;
        final int sonstigCount = getCount(counts, "SONSTIG");

        model.put("aktienanleihenCount", aktienanleihenCount);
        model.put("sprinterCount", sprinterCount);
        model.put("bonusCount", bonusCount);
        model.put("discountCount", discountCount);
        model.put("knockoutCount", knockoutCount);
        model.put("indexCount", indexCount);
        model.put("garantieCount", garantieCount);
        model.put("sonstigCount", sonstigCount);
    }

    private int getCount(Map<Object, Integer> counts, String key) {
        if (counts == null) {
            return 0;
        }
        final Integer count = counts.get(key);
        if (count == null) {
            return 0;
        }
        return count;
    }
}