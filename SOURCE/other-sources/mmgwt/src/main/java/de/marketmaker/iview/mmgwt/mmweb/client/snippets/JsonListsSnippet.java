package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class JsonListsSnippet extends AbstractSnippet<JsonListsSnippet, SnippetTableView<JsonListsSnippet>> {
    public static class Class extends SnippetClass {
        public Class() {
            super("JsonLists"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new JsonListsSnippet(context, config);
        }
    }

    private final String jsonKey;
    private final String listPrefix;
    private final String listToken;
    private String selectedListId = null;
    private JsonListDetailsSnippet detailsSnippet = null;


    protected JsonListsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.jsonKey = config.getString("jsonKey"); // $NON-NLS-0$
        this.listPrefix = config.getString("listPrefix"); // $NON-NLS-0$
        this.listToken = config.getString("listToken", null); // $NON-NLS-0$

        this.setView(new SnippetTableView<>(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.category(), -1f, TableCellRenderers.DEFAULT), 
                new TableColumn(I18n.I.nrOfElements(), -1f, TableCellRenderers.DEFAULT), 
                new TableColumn("", 10f, TableCellRenderers.DEFAULT) // $NON-NLS-0$
        })));
    }

    @Override
    public void onControllerInitialized() {
        final String detailId = getConfiguration().getString("detailId", null); // $NON-NLS-0$
        if (detailId != null) {
            this.detailsSnippet = (JsonListDetailsSnippet) this.contextController.getSnippet(detailId);
        }
        updateView();
    }

    public void destroy() {
        // nothing to do
    }

    public void updateView() {
        final JSONArray pricelists = SessionData.INSTANCE.getGuiDef(this.jsonKey).getValue().isArray();
        final List<Object[]> list = new ArrayList<>();

        for (int i = 0; i < pricelists.size(); i++) {
            final JSONObject pricelist = pricelists.get(i).isObject();
            final String id = pricelist.get("id").isString().stringValue(); // $NON-NLS-0$
            final String name = pricelist.get("title").isString().stringValue(); // $NON-NLS-0$
            final String jsonListKey = this.listPrefix + id;
            final List<QuoteWithInstrument> listQwi = SessionData.INSTANCE.getList(jsonListKey);
            final int count = listQwi.size();

            final String styleName = isSelected(id, name, jsonListKey) ? "mm-bestTool-link selected" : "mm-bestTool-link"; // $NON-NLS-0$ $NON-NLS-1$
            final String linkContent = "<div class=\"" + styleName + "-content\"></div>"; // $NON-NLS-0$ $NON-NLS-1$
            final Object oName;
            if (this.listToken == null) {
                oName = name;
            }
            else {
                oName = new Link(new LinkListener<Link>() {
                    public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                        PlaceUtil.goTo(listToken + "/" + id); // $NON-NLS-0$
                    }
                }, name, null);
            }
            final Link linkPerformance = new Link(new LinkListener<Link>() {
                public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                    selectList(id, name, jsonListKey, true);
                }
            }, linkContent, null);
            list.add(new Object[]{
                    oName, String.valueOf(count), linkPerformance
            });
        }

        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }

    private boolean isSelected(String id, String name, String jsonListKey) {
        if (this.selectedListId == null) {
            selectList(id, name, jsonListKey, false);
            return true;
        }
        return this.selectedListId.equals(id);
    }

    private void selectList(String id, String name, String jsonListKey, boolean doUpdate) {
        this.selectedListId = id;

        if (this.detailsSnippet != null) {
            this.detailsSnippet.setList(name, jsonListKey);
        }

        if (doUpdate) {
            updateView();
        }
    }

}
