package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import com.google.gwt.i18n.client.DateTimeFormat;
import de.marketmaker.itools.gwtutil.client.util.date.CsDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;

import java.util.Date;

/**
 * User: umaurer
 * Date: 19.12.13
 * Time: 17:15
 */
public class GwtClientServerProxyGwt extends GwtClientServerProxy {
    @Override
    public String formatIso8601(String sDate) {
        if (CsDateParser.isIso8601(sDate)) {
            return sDate;
        }
        return GwtDateParser.formatIso8601(sDate);
    }
}
