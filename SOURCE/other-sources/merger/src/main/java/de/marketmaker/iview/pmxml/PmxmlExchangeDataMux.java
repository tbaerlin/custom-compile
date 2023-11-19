package de.marketmaker.iview.pmxml;

import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.itools.pmxml.frontend.InvalidSessionException;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;

import java.util.Locale;

/**
 * Created on 18.02.13 10:53
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class PmxmlExchangeDataMux implements PmxmlExchangeData {
    private PmxmlExchangeData dePmxml;
    private PmxmlExchangeData enPmxml;

    public void setDePmxml(PmxmlExchangeData dePmxml) {
        this.dePmxml = dePmxml;
    }

    public void setEnPmxml(PmxmlExchangeData enPmxml) {
        this.enPmxml = enPmxml;
    }


    private PmxmlExchangeData getPmxml() {
        final Locale locale = RequestContextHolder.getRequestContext().getLocale();
        if (locale.getLanguage().equals("en")) {
            return this.enPmxml;
        }
        return this.dePmxml;
    }

    @Override
    public PmxmlExchangeDataResponse exchangeData(PmxmlExchangeDataRequest pmxmlExchangeDataRequest) throws PmxmlException {
        try {
            return getPmxml().exchangeData(pmxmlExchangeDataRequest);
        }
        catch (Exception e) {
            final Throwable cause = e.getCause();
            if (cause instanceof InvalidSessionException) {
                throw (InvalidSessionException) cause;
            }
            throw e;
        }
    }

}
