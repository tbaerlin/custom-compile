package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.marketmaker.istar.domain.Language;
import org.springframework.validation.BindException;

import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.data.InstrumentAllocationImpl;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.CachingUserProvider;
import de.marketmaker.istar.merger.provider.PortfolioEvaluationProvider;
import de.marketmaker.istar.merger.user.EvaluatedPortfolio;
import de.marketmaker.istar.merger.user.EvaluatedPosition;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.easytrade.block.UserCommand;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.data.PieData;

/**
 * PfVisualization.java
 * Created on Sep 21, 2009 1:41:35 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PfVisualization extends AbstractImgChart {

    private static final DecimalFormat FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        FORMAT.applyLocalizedPattern("0.00");
    }

    private CachingUserProvider cachingUserProvider;

    private PortfolioEvaluationProvider evaluationProvider;

    public PfVisualization() {
        super(PfVisualizationCommand.class);
    }

    @Override
    protected void onBind(HttpServletRequest httpServletRequest, Object o) throws Exception {
        super.onBind(httpServletRequest, o);
        if (o instanceof UserCommand) {
            final UserCommand command = (UserCommand) o;
            if (command.getUserid() == null) {
                final HttpSession session = httpServletRequest.getSession(false);
                if (session != null) {
                    command.setUserid((String) session.getAttribute(ProfileResolver.AUTHENTICATION_KEY));
                }
            }
        }
    }

    private static BigDecimal calcShare(BigDecimal categoryValue, BigDecimal portfolioValue) {
        return categoryValue.divide(portfolioValue, Constants.MC);
    }

    private static InstrumentAllocation.Type mapType(PfVisualizationCommand cmd) {
        if ("TYP".equals(cmd.getType())) {
            return InstrumentAllocation.Type.SECTOR;
        }
        if ("LAND".equals(cmd.getType())) {
            return InstrumentAllocation.Type.COUNTRY;
        }
        if ("WAEHRUNG".equals(cmd.getType())) {
            return InstrumentAllocation.Type.CURRENCY;
        }
        if ("ASSET".equals(cmd.getType())) {
            return InstrumentAllocation.Type.ASSET;
        }
        return InstrumentAllocation.Type.INSTRUMENT;
    }

    private static String getCategory(InstrumentAllocation.Type type, EvaluatedPosition pos) {
        final InstrumentTypeEnum it = pos.getQuote().getInstrument().getInstrumentType();
        final Language language = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());

        if (type == InstrumentAllocation.Type.SECTOR) {
            if (it == InstrumentTypeEnum.STK || it == InstrumentTypeEnum.BND) {
                return pos.getQuote().getInstrument().getSector().getNameOrDefault(language);
            }
        }
        if (type == InstrumentAllocation.Type.COUNTRY) {
            return pos.getQuote().getInstrument().getCountry().getNameOrDefault(language);
        }
        if (type == InstrumentAllocation.Type.CURRENCY) {
            return pos.getQuote().getCurrency().getNameOrDefault(language);
        }
        return it.getNameOrDefault(language);
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
                                                        HttpServletResponse response, Object object,
                                                        BindException bindException) throws Exception {
        final PfVisualizationCommand cmd = (PfVisualizationCommand) object;

        final PieData pd = createPieData(cmd);

        final ChartModelAndView result = createChartModelAndView(cmd);
        result.addObject("pie", pd);
        return result;
    }

    private PieData createPieData(PfVisualizationCommand cmd) {
        return createPieData(getAllocations(this.cachingUserProvider, this.evaluationProvider, cmd));
    }

    public static List<InstrumentAllocation> getAllocations(CachingUserProvider cachingUserProvider, PortfolioEvaluationProvider evaluationProvider, PfVisualizationCommand cmd) {
        final UserContext uc = cachingUserProvider.getUserContext(cmd);
        final EvaluatedPortfolio portfolio = evaluationProvider.evaluate(uc.getUser().getPortfolio(Long.valueOf(cmd.getPortfolioid())), false);
        final InstrumentAllocation.Type type = mapType(cmd);

        final List<InstrumentAllocation> result = new ArrayList<>();
        final BigDecimal currentValue = portfolio.getCurrentValue();
        for (Map.Entry<String, BigDecimal> e : getCategories(portfolio, type).entrySet()) {
            final BigDecimal share = calcShare(e.getValue(), currentValue);
            if (share.signum() > 0) { // piedata only accepts positive value
                result.add(new InstrumentAllocationImpl(type, e.getKey(), share));
            }
        }
        return result;
    }

    private static Map<String, BigDecimal> getCategories(EvaluatedPortfolio portfolio,
            InstrumentAllocation.Type type) {
        final Map<String, BigDecimal> result = new HashMap<>();

        for (EvaluatedPosition pos : portfolio.getPositions()) {
            final String category = getCategory(type, pos);
            if (result.containsKey(category)) {
                final BigDecimal share = result.get(category);
                result.put(category, share.add(pos.getCurrentValueInPortfolioCurrency()));
            }
            else {
                result.put(category, pos.getCurrentValueInPortfolioCurrency());
            }
        }
        return result;
    }

    private PieData createPieData(List<InstrumentAllocation> allocations) {
        final PieData pd = getByValue(allocations);
        pd.finish();
        return pd;
    }

    private PieData getByValue(List<InstrumentAllocation> allocations) {
        final PieData.ByValue result = new PieData.ByValue();
        if (allocations.isEmpty()) {
            result.add("100% " + getUnknownText(), BigDecimal.ONE);
        }
        else {
            for (InstrumentAllocation allocation : allocations) {
                result.add(getLabel(allocation), allocation.getShare());
            }
        }
        return result;
    }

    private String getUnknownText() {
        return RequestContextHolder.getRequestContext().isLocaleLanguageGerman()
                ? "Unbekannt" : "Unknown";
    }

    private String getLabel(InstrumentAllocation allocation) {
        return formatAsPct(allocation.getShare()) + "% " + allocation.getCategory();
    }

    private String formatAsPct(BigDecimal share) {
        final BigDecimal pct = share.multiply(Constants.ONE_HUNDRED, Constants.MC);
        synchronized (FORMAT) {
            return FORMAT.format(pct);
        }
    }

    public void setEvaluationProvider(PortfolioEvaluationProvider evaluationProvider) {
        this.evaluationProvider = evaluationProvider;
    }

    public void setCachingUserProvider(CachingUserProvider cachingUserProvider) {
        this.cachingUserProvider = cachingUserProvider;
    }
}