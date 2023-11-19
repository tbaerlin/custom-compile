package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import com.google.gwt.user.client.Command;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.OHLCVSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TimesAndSalesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Collections;
import java.util.List;


/**
 *
 */
public class LmePortraitTimesAndSalesController extends PortraitTimesAndSalesController {
    private static final String DEF_LME_TAS = "ts_lme_tas"; // $NON-NLS-0$
    private static final String DEF_LME = "ts_lme"; // $NON-NLS-0$

    public LmePortraitTimesAndSalesController(ContentContainer contentContainer, DmxmlContext dmxmlContext) {
        super(contentContainer, dmxmlContext);
    }

    @Override
    public void activate() {
        if (delegate != null) {
            this.delegate.activate();
        }
    }

    @Override
    public void deactivate() {
        if (delegate != null) {
            this.delegate.deactivate();
        }
    }

    @Override
    public void onPlaceChange(final PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String symbol = historyToken.get(1, null);
        if (symbol == null) {
            return;
        }

        new PriceDataTypeQuery(symbol, context) {
            @Override
            public void priceDataCallback(String priceDataType) {
                final boolean useLmePricedata = "lme".equals(priceDataType);   // $NON-NLS$
                final InstrumentTypeEnum type = InstrumentTypeEnum.fromToken(historyToken.getControllerId());
                delegate = SnippetsFactory.createFlexController(getContentContainer(), getGuidefConfig(useLmePricedata));
                pt = (PriceTeaserSnippet) delegate.getSnippet("pt"); // $NON-NLS$
                ohlcv = (OHLCVSnippet) delegate.getSnippet("ohlcv"); // $NON-NLS$
                tas = (TimesAndSalesSnippet) delegate.getSnippet("tas"); // $NON-NLS$
                delegate.activate();
                updateSnippets(type, symbol);
                delegate.onPlaceChange(event);
            }
        }.execute();
    }

    @Override
    public List<Snippet> getSnippets() {
        if (delegate == null) {
            return Collections.emptyList();
        }
        return super.getSnippets();
    }

    private void updateSnippets(InstrumentTypeEnum type, String symbol) {
        this.pt.setSymbol(type, symbol, null);
        this.ohlcv.setSymbol(symbol);
        if (this.tas != null) {
            this.tas.setSymbol(symbol);
            ohlcv.addValueChangeHandler(tas);
        }
    }

    private String getGuidefConfig(boolean useLmePricedata) {
        final String result;
        final boolean useTimesAndSales = Selector.TIMES_AND_SALES.isAllowed();
        if (useLmePricedata) {
            result = useTimesAndSales?DEF_LME_TAS:DEF_LME;
        }
        else if (asFund) {
            result =  useTimesAndSales?DEF_FUND_TAS:DEF_FUND;
        }
        else {
            result =  useTimesAndSales?DEF_OHLCV_TAS:DEF_OHLCV;
        }
        return result;
    }

    private abstract class PriceDataTypeQuery implements Command {
        private final String symbol;
        private final DmxmlContext context;
        public PriceDataTypeQuery(String symbol, DmxmlContext context) {
            this.symbol = symbol;
            this.context = context;
        }
        @Override
        public void execute() {
            final DmxmlContext.Block<MSCPriceData> blockPriceData = context.addBlock("MSC_PriceData"); // $NON-NLS$
            blockPriceData.setParameter("symbol", symbol); // $NON-NLS$
            context.issueRequest(new ResponseTypeCallback() {
                protected void onResult() {
                    final String priceDataType = blockPriceData.getResult().getPricedatatype();
                    priceDataCallback(priceDataType);
                    context.removeBlock(blockPriceData);
                }
            });
        }
        public abstract void priceDataCallback(String priceDataType);
    }

}
