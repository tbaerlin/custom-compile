package de.marketmaker.istar.merger.web.easytrade.multiplex;

import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.EasytradeHandlerMapping;
import de.marketmaker.istar.merger.web.easytrade.MoleculeController;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;

/**
 * Created on 06.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class LocalTarget implements MultiplexerTarget {
    private MoleculeController mc;

    public void setMc(MoleculeController mc) {
        this.mc = mc;
    }

    @Override
    public Future<List<ModelAndView>> handleMixedRequest(final HttpServletRequest request, final HttpServletResponse response, final Zone zone, final MoleculeRequest mr, ExecutorService executorService) throws Exception {
        request.setAttribute(REQUEST_ATTRIBUTE_NAME, mr);
        return new Future<List<ModelAndView>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public List<ModelAndView> get() throws InterruptedException, ExecutionException {
                try {
                    //noinspection unchecked
                    return (List<ModelAndView>) mc.handleRequestInternal(request, response).getModel().get(MoleculeController.KEY_ATOMS);
                } catch (Exception e) {
                    throw new ExecutionException(e);
                }
            }

            @Override
            public List<ModelAndView> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }

    @Override
    public ModelAndView handleStraightRequest(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr) throws Exception {
        return this.mc.handleRequestInternal(request, response);
    }

    @Override
    public boolean supports(Zone zone, MoleculeRequest.AtomRequest atom) {
            final String name = getControllerName(zone, atom);
            return this.mc.hasAtomController(name);
    }

    private String getControllerName(Zone zone, MoleculeRequest.AtomRequest atom) {
        final Map<String, String[]> parameters
                = zone.getParameterMap(atom.getParameterMap(), atom.getName());
        final String[] strings = parameters.get(EasytradeHandlerMapping.CONTROLLER_NAME);
        return (strings != null) ? strings[0] : atom.getName();
    }

}