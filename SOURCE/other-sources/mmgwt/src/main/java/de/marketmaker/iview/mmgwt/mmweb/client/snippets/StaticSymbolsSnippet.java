package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.FlexTableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author Ulrich Maurer
 *         Date: 24.05.12
 */
public class StaticSymbolsSnippet extends AbstractSnippet<StaticSymbolsSnippet, SnippetTableView<StaticSymbolsSnippet>> {

    public static class Class extends SnippetClass {
        public Class() {
            super("StaticSymbols"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StaticSymbolsSnippet(context, config);
        }
    }

    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();
    private String selectedSymbol;


    protected StaticSymbolsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final DefaultTableColumnModel columnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.list(), -1, TableCellRenderers.DEFAULT),
                new TableColumn("", 10f, TableCellRenderers.DEFAULT)
        });
        setView(new SnippetTableView<>(this, columnModel));
    }

    @Override
    public void onControllerInitialized() {
        final String[] detailIds = getConfiguration().getArray("detailIds"); // $NON-NLS-0$
        if (detailIds != null) {
            for (String detailId : detailIds) {
                this.symbolSnippets.add((SymbolSnippet) this.contextController.getSnippet(detailId));
            }
        }
        updateView();
    }

    void setSelectedSymbol(String symbol, final String name) {
        this.selectedSymbol = symbol;
        for (SymbolSnippet symbolSnippet : symbolSnippets) {
            symbolSnippet.setSymbol(null, symbol, name);
        }
        if (symbol != null) {
            ackParametersChanged();
        }
    }

    private boolean isSelected(String symbol, String name) {
        if (this.selectedSymbol == null) {
            setSelectedSymbol(symbol, name);
            return true;
        }
        return this.selectedSymbol.equals(symbol);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void updateView() {
        final String listkey = getConfiguration().getString("listkey"); // $NON-NLS$
        final JSONValue jvList = SessionData.INSTANCE.getGuiDef(listkey).getValue();
        final JSONArray jsonArray = jvList.isArray();
        final FlexTableDataModelBuilder dmBuilder = new FlexTableDataModelBuilder();
        for (int i = 0, size = jsonArray.size(); i < size; i++) {
            final JSONObject jsonObject = jsonArray.get(i).isObject();
            final String symbol = jsonObject.get("symbol").isString().stringValue(); // $NON-NLS$
            final String display = jsonObject.get("display").isString().stringValue(); // $NON-NLS$
            final String symbolContent = "<div class=\"" + (isSelected(symbol, display) ? "mm-bestTool-link selected" : "mm-bestTool-link") + "-content\"></div>"; // $NON-NLS$
            final LinkListener<Link> listener = new LinkListener<Link>() {
                @Override
                public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                    setSelectedSymbol(symbol, display);
                }
            };
            final Link displayLink = new Link(listener, display, null);
            final Link symbolLink = new Link(listener, symbolContent, null);
            dmBuilder.addRow(displayLink, symbolLink);
        }
        getView().update(dmBuilder.getResult());
    }
}
