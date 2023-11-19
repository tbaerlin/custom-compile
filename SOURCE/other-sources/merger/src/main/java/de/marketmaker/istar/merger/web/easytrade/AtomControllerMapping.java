/*
 * AtomControllerMapping.java
 *
 * Created on 28.02.2008 14:10:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.Controller;
import de.marketmaker.istar.merger.web.easytrade.block.AtomController;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface AtomControllerMapping {
    AtomController getController(HttpServletRequest request);

    AtomController getAtomController(String controllerName);
}
