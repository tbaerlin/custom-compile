/*
 * AbstractComboBoxForm.java
 *
 * Created on 05.05.2008 14:15:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import java.util.ArrayList;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.MSCQuotes;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.RadioButtonRenderer;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class SelectQuoteForm extends ContentPanel implements Activatable {

    private static class Controller implements AsyncCallback<ResponseType> {
        private final SelectQuoteForm view;

        private final DmxmlContext.Block<MSCQuotes> block;

        private final DmxmlContext context;
        
        private final boolean withVwdCode = SnippetConfigurationView.QUOTE_SELECTION_WITH_VWDCODE;

        private Controller(SelectQuoteForm view) {
            this.context = new DmxmlContext();
            this.block = context.addBlock("MSC_Quotes"); // $NON-NLS-0$
            this.block.setParameter("disablePaging", true); // $NON-NLS-0$
            this.block.setParameter("sortBy", "marketName"); // $NON-NLS-0$ $NON-NLS-1$
            this.view = view;
        }

        public void onSuccess(ResponseType responseType) {
            final DefaultTableDataModel dtm = DefaultTableDataModel.create(getResult().getQuotedata(),
                    new AbstractRowMapper<QuoteData>() {
                        public Object[] mapRow(QuoteData qd) {
                            return new Object[]{
                                    false,
                                    qd.getMarketName(),
                                    qd.getCurrencyIso(),
                                    withVwdCode ? qd.getVwdcode() : qd.getMarketVwd()
                            };
                        }
                    });
            this.view.show(dtm);
        }

        protected QuoteWithInstrument getNthResult(int n) {
            final MSCQuotes result = getResult();
            return new QuoteWithInstrument(result.getInstrumentdata(), result.getQuotedata().get(n));
        }

        private MSCQuotes getResult() {
            return this.block.getResult();
        }

        private void reload() {
            final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
            if (qwi != null) {
                this.block.setParameter("symbol", qwi.getIid(true)); // $NON-NLS-0$
                this.context.issueRequest(this);
            }
        }

        public void onFailure(Throwable throwable) {
            this.view.show(DefaultTableDataModel.create(new ArrayList<Object[]>()));
        }
    }

    private final Controller c = new Controller(this);

    private final Map<String, String> params;

    private final RadioButtonRenderer renderer = new RadioButtonRenderer("mktSelect"); // $NON-NLS-0$

    private SnippetTableWidget tw;

    protected SelectQuoteForm(Map<String, String> params) {
        setHeaderVisible(false);
        addStyleName("mm-contentData"); // $NON-NLS-0$

        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        this.params = params;

        final TableColumnModel columnModel = createColumnModel();

        this.tw = new SnippetTableWidget(columnModel)
                .surroundedBy("<form id=\"mktSelectForm\">", "</form>"); // $NON-NLS-0$ $NON-NLS-1$

        this.renderer.setElement(this.tw.getElement());
        this.tw.setWidth("100%"); // $NON-NLS$
        add(this.tw);
    }

    private TableColumnModel createColumnModel() {
        final TableColumn[] columns = new TableColumn[4];
        final boolean withVwdCode = SnippetConfigurationView.QUOTE_SELECTION_WITH_VWDCODE;

        columns[0] = new TableColumn(I18n.I.option(), 0.1f).withRenderer(this.renderer);
        columns[1] = new TableColumn(I18n.I.name(), withVwdCode ? 0.5f : 0.7f)
                .withRenderer(new MaxLengthStringRenderer(withVwdCode ? 32 : 50, "-"));  // $NON-NLS-0$
        columns[2] = new TableColumn(I18n.I.currency(), 0.1f).withRenderer(STRING_CENTER);
        if (withVwdCode) {
            columns[3] = new TableColumn("vwdCode", 0.3f).withRenderer(STRING); // $NON-NLS$ 
        }
        else {
            columns[3] = new TableColumn(I18n.I.code(), 0.1f).withRenderer(STRING_10);
        }

        return new DefaultTableColumnModel(columns);
    }

    public void activate() {
        this.c.reload();
    }

    public void deactivate() {
        ackSelectionChanged();
    }

    public void show(TableDataModel dtm) {
        this.tw.updateData(dtm);
    }

    private void ackNoSelection() {
        this.params.remove("title"); // $NON-NLS-0$
        this.params.remove("symbol"); // $NON-NLS-0$
        QuoteWithInstrument.setLastSelected(null);
    }

    private void ackSelectionChanged() {
        final int n = this.renderer.getSelected();
        if (n == -1) {
            ackNoSelection();
            return;
        }
        final QuoteWithInstrument qwi = this.c.getNthResult(n);
        this.params.put("title", qwi.getInstrumentData().getName()); // $NON-NLS-0$
        this.params.put("symbol", qwi.getInstrumentData().getIid()); // $NON-NLS-0$
        QuoteWithInstrument.setLastSelected(qwi);
    }
}
