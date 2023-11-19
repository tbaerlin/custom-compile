package de.marketmaker.iview.mmgwt.mmweb.client.desktop;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author umaurer
 */
public class ChartIcon extends DesktopIcon<QuoteWithInstrument> {

    public static final String PERIOD_KEY_ALL = "all"; // $NON-NLS-0$

    public ChartIcon(QuoteWithInstrument qwi, String period, String currency, String additionalParams, String width, String height) {
        this(qwi.getInstrumentData(), qwi.getQuoteData(), period, currency, qwi.getName(), additionalParams, width, height);
    }

    public ChartIcon(InstrumentData instrumentdata, QuoteData quotedata, String period) {
        this(instrumentdata, quotedata, period, null, instrumentdata.getName(), null, "200", "150"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public ChartIcon(InstrumentData instrumentdata, QuoteData quotedata, final String period, final String currency, String name, String additionalParams, String width, String height) {
        super("mm-desktopIcon-chart", // $NON-NLS-0$
                ChartUrlFactory.getUrl("chartKursverlauf.png?width=" + width + "&height=" + height + "&gd1=0&symbol=" + quotedata.getQid() + // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                        (period == null ? "" : period.equals(PERIOD_KEY_ALL) ? "&from=start&to=today" : ("&period=" + period))) + // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                        (additionalParams == null ? "" : ("&" + additionalParams)), // $NON-NLS-0$ $NON-NLS-1$
                new String[]{name, quotedata.getMarketName()},
                new QuoteWithInstrument(instrumentdata, quotedata),
                new LinkListener<QuoteWithInstrument>() {
                    public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
                        PlaceChangeEvent event = new PlaceChangeEvent(context.data.getToken("C")); // $NON-NLS-0$
                        if (currency != null && !currency.isEmpty()) {
                            event.withProperty("currency", currency); // $NON-NLS-0$
                        }
                        event.withProperty("period", period); // $NON-NLS-0$
                        PlaceUtil.fire(event);
                    }
                });
    }

    public ChartIcon(InstrumentData instrumentdata, QuoteData quotedata, String period, String name, String additionalParams) {
        this(instrumentdata, quotedata, period, null, name, additionalParams, "200", "150"); // $NON-NLS-0$ $NON-NLS-1$
    }
}
