package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.analyzer.AnalyzerProvider;
import de.marketmaker.istar.merger.provider.analyzer.AnalyzerRequest;
import de.marketmaker.istar.merger.provider.analyzer.AnalyzerResponse;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * <p>
 * Evaluate a set of analyses and provide statistical data about performance and risk estimates.<br/>
 * </p>
 *  <p>
 * Examples:
 * <ul>
 *     <li>analyses ordered by performance (Top/Flop-List)<br/>
 *        <b>collect securityVwdCode, analysisId, performance, prevClose, target for analysis sort performance desc</b>
 *     </li>
 *     <li>agencies ordered by success rates<br/>
 *        <b>collect agencyId, agencySymbol, successCount for agency sort successCount desc skip 0 limit 100 date 2016-02-01</b>
 *     </li>
 *     <li>securities ordered by buy/sell ratio<br/>
 *        <b>collect securitySymbol, buy, sell for security sort buySellRatio asc skip 200 limit 100</b>
 *     </li>
 *     <li>industries ordered by buy/sell ratio<br/>
 *        <b>collect industryName, buy, sell for industry sort buySellRatio desc</b>
 *     </li>
 *     <li>securities ordered by buy count with previous rating<br/>
 *        <b>collect securitySymbol, buy, prevHold, prevSell, totalRatings for security sort buy asc</b>
 *     </li>
 *     <li>buy/hold/sell for security, ordered by buy ratings<br/>
 *        <b>collect securitySymbol, buy, hold, sell, totalRatings for security sort totalRatings desc</b>
 *     </li>
 *     <li>buy/hold/sell for agency, ordered by buy ratings<br/>
 *        <b>collect agencyId, agencySymbol, buy, hold, sell, totalRatings for agency sort totalRatings desc</b>
 *     </li>
 *     <li>buy/hold/sell for industry, ordered by buy ratings<br/>
 *        <b>collect industryName, buy, hold, sell, totalRatings for industry sort totalRatings desc</b>
 *     </li>
 *     <li>buy/hold/sell for index, ordered by buy ratings<br/>
 *        <b>collect indexName, buy, hold, sell for index sort buy desc</b>
 *     </li>
 *     <li>industries ordered by buy count with previous rating<br/>
 *        <b>collect industryName, buy, prevHold, prevSell, totalRatings for industry sort buy desc</b>
 *     </li>
 *     <li>indices ordered by count of buy ratings<br/>
 *         <b>collect indexName, indexQid, buy, hold, sell, totalRatings for index sort buy desc</b>
 *     </li>
 *     <li>successful analysis<br/>
 *        <b>collect securityVwdCode, analysisId, performance, prevClose, target for analysis sort performance desc</b>
 *     </li>
 * </ul>
 * </p>
 */
public class RscAnalyzer extends EasytradeCommandController {

    public static class Command extends RscCommand {

        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

    }

    private AnalyzerProvider analyzerProvider;

    private EntitlementProviderVwd entitlementProvider;

    public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
        this.analyzerProvider = analyzerProvider;
    }

    public void setEntitlementProvider(EntitlementProviderVwd entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public RscAnalyzer() {
        super(Command.class);
    }

    @Override
    public ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Map<String, Object> model = new HashMap<>();
        AnalyzerResponse result = analyzerProvider.getAnalytics(convert((Command) o));
        model.put("query", result.getQuery());
        model.put("view", result.getReportView());
        return new ModelAndView("rscanalyzer", model);
    }

    private AnalyzerRequest convert(Command cmd) {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        return new AnalyzerRequest(
                requestContext.getProfile(),
                cmd.getProviderId(),
                cmd.getQuery());
    }

}
