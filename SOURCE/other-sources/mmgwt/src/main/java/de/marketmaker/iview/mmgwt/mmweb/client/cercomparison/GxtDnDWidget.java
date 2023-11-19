package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison;

import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GxtDragNDrop;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GxtDragNDropUtil;

import java.util.HashMap;

/**
 * Created on 07.09.2010 12:34:28
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
class GxtDnDWidget extends AbstractSelectInstrumentWidget {
    private static HashMap<String, String> mapCategoryCache = new HashMap<>();

    public GxtDnDWidget(final CerComparisonController controller) {
        super(controller);
        final Grid grid = new Grid(1, 2);
        grid.setStyleName("mm-certcomparison-dnd");
        final ContentPanel dndPanel = new ContentPanel();
        dndPanel.setHeaderVisible(false);
        dndPanel.addStyleName("mm-certcomparison-dnd-panel"); // $NON-NLS$

        grid.setWidget(0, 0, dndPanel);

        final Button buttonAdd = Button.icon("mm-icon-comparison-add")  // $NON-NLS$
                .tooltip(I18n.I.certificatesSearch())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        searchForQuote();
                    }
                })
                .build();
        grid.setWidget(0, 1, buttonAdd);

        new GxtDragNDropUtil<>(dndPanel, "ins", // $NON-NLS-0$
                new GxtDragNDrop<QuoteWithInstrument>() {
                    public void onDrop(QuoteWithInstrument qwi) {
                        setQuote(qwi);
                    }

                    public void onDragEnter(QuoteWithInstrument qwi, DNDEvent e) {
                        if (InstrumentTypeEnum.valueOf(qwi.getInstrumentData().getType()) != InstrumentTypeEnum.CER) {
                            setStatus(e, false);
                        }
                        else {
                            checkForCategory(qwi, e);
                        }
                    }
                });

        initWidget(grid);
    }

    private void checkForCategory(final QuoteWithInstrument qwi, final DNDEvent e) {
        final String qid = qwi.getId();
        if (evaluateCategory(qid, e)) {
            // categoryKey is in cache
            return;
        }
        mapCategoryCache.put(qid, "ERROR"); // $NON-NLS$  -  cache is overwritten after onSuccess()

        final DmxmlContext.Block<CERDetailedStaticData> block = getBlockCerStatic();
        block.setParameter("symbol", qid); // $NON-NLS$

        getContext().issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ResponseType result) {
                mapCategoryCache.put(qid, block.isResponseOk() ?
                        block.getResult().getCategoryKey() :
                        "ERROR" // $NON-NLS$
                );
                evaluateCategory(qid, e);
            }
        });
    }

    private boolean evaluateCategory(final String symbol, final DNDEvent e) {
        final String categoryKey = mapCategoryCache.get(symbol);
        if (categoryKey == null) {
            setStatus(e, false);
            return false;
        }

        final CerComparisonController controller = getController();
        setStatus(e, controller.getTypeKey() == null || controller.getTypeKey().equals(categoryKey));
        return true;
    }

    private void setStatus(DNDEvent e, boolean status) {
        e.getStatus().setStatus(status);
        e.setCancelled(!status);
    }
}
