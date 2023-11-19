/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.PartitionResultVisitor;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkFinder extends AbstractFindersuchergebnis {

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public static class Command extends AbstractFindersuchergebnis.Command {

        private boolean partition;

        public boolean isPartition() {
            return partition;
        }

        public void setPartition(boolean partition) {
            this.partition = partition;
        }
    }

    public StkFinder() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = getFields(InstrumentTypeEnum.STK, PermissionType.FACTSET);

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.STK, cmd, fields);

        if (cmd.getIid() != null) {
            rsr.setInstrumentIds(Arrays.asList(cmd.getIid()));
        }

        if (cmd.isPartition()) {
            rsr.setVisitorClass(PartitionResultVisitor.class);
        }

        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        if (parameters.containsKey("index")) {
            handleIndexQuery(rsr, parameters);
        }
        rsr.addParameters(parameters);

        final List<String> sortfields = asSortfields(fields);

        final ListResult listResult = ListResult.create(cmd, sortfields, "name", 0);
        final String sortfield = fields.get(RatioDataRecord.Field.valueOf(listResult.getSortedBy())).name();
        rsr.addParameter("sort1", sortfield);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, isWithPrices(cmd, false), false);

        return new ModelAndView("stkfinder", model);
    }

    private void handleIndexQuery(RatioSearchRequest rsr, Map<String, String> parameters) {
        final String index = parameters.remove("index");
        final StringBuilder qids = new StringBuilder(100);

        final Set<Long> iids = new HashSet<>();

        final String[] symbols = index.split("@");
        for (final String symbol : symbols) {
            final Quote quote = this.instrumentProvider.identifyQuote(symbol, SymbolStrategyEnum.AUTO, null, null);

            IndexCompositionResponse response
                    = this.indexCompositionProvider.getIndexCompositionByQid(quote.getId());
            if (response.isValid()) {
                iids.addAll(response.getIndexComposition().getIids());
            }
            final List<Quote> quotes =
                    this.instrumentProvider.getIndexQuotes(EasytradeInstrumentProvider.qidSymbol(quote.getId()));
            for (Quote q : quotes) {
                if (qids.length() > 0) {
                    qids.append('@');
                }
                qids.append(q.getId());
            }
        }

        rsr.setInstrumentIds(new ArrayList<>(iids));
        if (qids.length() > 0) {
            rsr.addParameter("qid", qids.toString());
        }
    }
}