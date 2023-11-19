package de.marketmaker.istar.merger.web.easytrade.block;

/**
 * PfVisualization.java
 * Created on Sep 21, 2009 1:41:35 PM
 * Copyright (c) vwd GmbH. All Rights Reserved.
 *
 * @author Michael Lösch
 */

import de.marketmaker.istar.merger.provider.CachingUserProvider;
import de.marketmaker.istar.merger.provider.PortfolioEvaluationProvider;
import de.marketmaker.istar.merger.web.easytrade.chart.PfVisualizationCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Generates an URL to a pie chart, showing different aspects of the diversification of a given portfolio.
 * E.g. the different position currencies existing in the portfolio and their percentage
 */

public class PfVisualization extends EasytradeChartController {

    private CachingUserProvider cachingUserProvider;
    private PortfolioEvaluationProvider evaluationProvider;

    public PfVisualization() {
        super(PfVisualizationCommand.class, "pfVisualization.png");
    }

    @Override
    protected void onBind(HttpServletRequest request, Object command) throws Exception {
        super.onBind(request, command);
        if(command instanceof UserCommand) {
            new UserHandlerMethod(request, (UserCommand)command).invoke();
        }
    }

    public void setEvaluationProvider(PortfolioEvaluationProvider evaluationProvider) {
        this.evaluationProvider = evaluationProvider;
    }

    public void setCachingUserProvider(CachingUserProvider cachingUserProvider) {
        this.cachingUserProvider = cachingUserProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final PfVisualizationCommand cmd = (PfVisualizationCommand) o;
        final Map<String, Object> model = getDefaultModel(cmd);
        if (this.cachingUserProvider != null && this.evaluationProvider != null) {
            model.put("allocations", de.marketmaker.istar.merger.web.easytrade.chart.PfVisualization.getAllocations(this.cachingUserProvider, this.evaluationProvider, cmd));
        }
        return new ModelAndView("pfvisualization", model);
    }
}