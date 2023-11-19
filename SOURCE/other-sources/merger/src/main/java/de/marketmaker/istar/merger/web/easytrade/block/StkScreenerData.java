/*
 * StkScreenerData.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.provider.screener.ScreenerField;
import de.marketmaker.istar.merger.provider.screener.ScreenerProvider;
import de.marketmaker.istar.merger.provider.screener.ScreenerResult;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;

/**
 * Returns <i>theScreener</i> analysis data for a given financial instrument symbol.
 * <p>
 *     The <i>theScreener</i> provides evaluations and ratings regarding the upside potential and the risk.
 *     The upside rating is presented with the <i>Four Stars</i>.
 *     They stand for the objective evaluation of a stock based on earnings revision trend, price potential, medium term technical trend, and relative performance.
 *     The risk evaluation takes the historical bear market and bad news behavior into account.
 * </p>
 * <p>Please note that the functionality of the underlying software module is shared between the following two blocks:</p>
 * <ul>
 *     <li>STK_ScreenerData {@see STK_ScreenerData}</li>
 *     <li>STK_ScreenerInterest {@see STK_ScreenerInterest}</li>
 * </ul>
 *
 * <h3>STK_ScreenerData</h1>
 * <p>The block <b>STK_ScreenerData</b> provides basic information about the instrument, the analysis results, and risk assessment related key figures.</p>
 * <p>The security is selected based on the given symbol (according to the symbol strategy) and market (without a given market strategy).</p>
 *
 * <h3>STK_ScreenerInterest</h1>
 * <p>The block <b>STK_ScreenerInterest</b> provides only a <i>Four Stars</i> related subset (aka interest) of STK_ScreenerData containing the rating and the URL of the <i>Four Stars</i> indicator image</p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkScreenerData extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    private ScreenerProvider screenerProvider;

    private String template = "stkscreenerdata";

    public StkScreenerData() {
        super(Command.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setScreenerProvider(ScreenerProvider screenerProvider) {
        this.screenerProvider = screenerProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command extends DefaultSymbolCommand {
        private String language = "de";

        /**
         * @return The language to be used for the generated texts. In addition to simple language
         * values like "en" or "de", country specific languages are supported (for example "en-GB").
         * This has influence on how the dates, prices and numbers are displayed.
         * @sample de
         * @sample en
         * @sample fr
         * @sample en-GB
         */
        @Pattern(regex = "(de|en|fr)(-[A-Z][A-Z])?")
        public String getLanguage() {
            return language;
        }

        /**
         * @param language The language to be used for the generated texts.
         */
        public void setLanguage(String language) {
            this.language = language;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        checkPermission(Selector.SCREENER);

        final Command cmd = (Command) o;
        final Quote q = this.instrumentProvider.getQuote(cmd);

        if (q == null) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final ScreenerResult screenerResult = this.screenerProvider.getScreenerResult(q.getInstrument().getId(), cmd.getLanguage());

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", q);
        model.put("baseFields", getSortedFields(screenerResult.getBaseFields()));
        model.put("analysisFields", getSortedFields(screenerResult.getAnalysisFields()));
        model.put("riskFields", getSortedFields(screenerResult.getRiskFields()));

        final List<ScreenerField> list = screenerResult.getAnalysisFields();
        for (final ScreenerField field : list) {
            if ("interest".equals(field.getName())) {
                model.put("interestfield", field);
                break;
            }
        }

        return new ModelAndView(this.template, model);
    }

    private List<ScreenerField> getSortedFields(List<ScreenerField> fields) {
        fields.sort(new Comparator<ScreenerField>() {
            public int compare(ScreenerField o1, ScreenerField o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return fields;
    }
}
