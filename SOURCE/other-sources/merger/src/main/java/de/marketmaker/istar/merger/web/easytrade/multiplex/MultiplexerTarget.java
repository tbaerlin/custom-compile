package de.marketmaker.istar.merger.web.easytrade.multiplex;

import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created on 06.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface MultiplexerTarget {
    Future<List<ModelAndView>> handleMixedRequest(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr, ExecutorService executorService) throws Exception;

    ModelAndView handleStraightRequest(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr) throws Exception;

    boolean supports(Zone zone, MoleculeRequest.AtomRequest atom);
}
