/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioSearchMetaResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OpraFinderMetadata implements AtomController {
    private RatiosProvider ratiosProvider;

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final RatioSearchMetaResponse md = this.ratiosProvider.getOpraMetaData();

        final Map<String, Object> model = new HashMap<>();
        model.put("underlyingSymbol", md.getEnumFields().get(RatioFieldDescription.underlyingWkn.id()));
        model.put("symbol", md.getEnumFields().get(RatioFieldDescription.wkn.id()));
        model.put("optionType", md.getEnumFields().get(RatioFieldDescription.osType.id()));
        model.put("market", md.getEnumFields().get(RatioFieldDescription.vwdMarket.id()));

        return new ModelAndView("optfinderoprametadata", model);
    }
}