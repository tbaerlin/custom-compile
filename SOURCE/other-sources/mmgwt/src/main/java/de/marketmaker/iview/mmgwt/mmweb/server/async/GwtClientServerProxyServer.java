package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.itools.gwtutil.client.util.date.CsDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.ServerDateParser;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.GwtClientServerProxy;

/**
 * User: umaurer
 * Date: 19.12.13
 * Time: 17:17
 */
public class GwtClientServerProxyServer extends GwtClientServerProxy {
    @Override
    public String formatIso8601(String sDate) {
        if (CsDateParser.isIso8601(sDate)) {
            return sDate;
        }
        return ServerDateParser.formatIso8601(sDate);
    }

    @Override
    public <C> C getClassInstance(Class<C> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("cannot create instance of class " + clazz.getName(), e);
        }
    }
}
