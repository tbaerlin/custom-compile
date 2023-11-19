package de.marketmaker.istar.merger.web.easytrade.block;


import de.marketmaker.istar.domain.instrument.QuoteNameStrategies;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.DzBankRecordProvider;
import de.marketmaker.istar.merger.query.DistinctValueCounter;

import de.marketmaker.istar.merger.query.UnderlyingCounter;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>
 * Retrieves meta data related to GIS_Finder.
 * </p>
 * <p>
 * Returns fields that are relevant for categorizing, querying and sorting DzBankRecords.
 * </p>
 * @author Michael Wohlfart
 */
public class GisFinderMetadata implements AtomController {

    private DzBankRecordProvider dzProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setDzProvider(DzBankRecordProvider dzProvider) {
        this.dzProvider = dzProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        EasytradeCommandController.checkPermission(Selector.DZ_KAPITALKMARKT);

        final Set<? extends DistinctValueCounter> valueCounts =
                rewriteUnderlyings(
                        this.dzProvider.createDzBankRecordsMetadata().getMetadataSet(),
                        this.instrumentProvider, isMarketmanagerZone(request));

        final HashMap<String, Object> model = new HashMap<>();
        for (DistinctValueCounter counter : valueCounts) {
            model.put(counter.getPropertyName(), counter);
        }
        return new ModelAndView("gisfindermetadata", model);
    }

    static Set<? extends DistinctValueCounter> rewriteUnderlyings(
            Set<? extends DistinctValueCounter> metadata,
            EasytradeInstrumentProvider instrumentProvider,
            boolean useVwdcodeAsKey) {
        final Set<DistinctValueCounter> result = new HashSet<>();
        final QuoteNameStrategy nameStrategy = getCurrentNameStrategy();

        for (final DistinctValueCounter dataset : metadata) {
            if (UnderlyingCounter.PROPERTY_NAME.equals(dataset.getPropertyName())) {
                result.add(((UnderlyingCounter)dataset).rewriteUnderlyings(
                        nameStrategy, instrumentProvider, useVwdcodeAsKey));
            }
            else {
                result.add(dataset);
            }
        }
        return result;
    }

    private static QuoteNameStrategy getCurrentNameStrategy() {
        final RequestContext ctx = RequestContextHolder.getRequestContext();
        return (ctx != null) ? ctx.getQuoteNameStrategy() : QuoteNameStrategies.DEFAULT;
    }

    private boolean isMarketmanagerZone(HttpServletRequest request) {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        return z != null && "marketmanager".equals(z.getName());
    }

}
