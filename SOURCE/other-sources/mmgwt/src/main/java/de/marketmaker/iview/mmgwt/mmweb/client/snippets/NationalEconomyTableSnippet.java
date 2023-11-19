package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.MERItems;
import de.marketmaker.iview.dmxml.MerCountry;
import de.marketmaker.iview.dmxml.MerItem;
import de.marketmaker.iview.dmxml.MerType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.economic.TypeListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.FlexTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CurrencyRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * NationalEconomyTableSnippet.java
 * <p/>
 * Created on Oct 2, 2008 5:13:47 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class NationalEconomyTableSnippet
        extends AbstractSnippet<NationalEconomyTableSnippet, SnippetTableView<NationalEconomyTableSnippet>> {

    public static class Class extends SnippetClass {
        public Class() {
            super("NationalEconomyTable"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NationalEconomyTableSnippet(context, config);
        }
    }

    class Field {
        final private String name;
        final private String display;

        Field(String name, String display) {
            this.name = name;
            this.display = display;
        }

        public String getName() {
            return name;
        }

        public String getDisplay() {
            return display;
        }
    }

    final private DmxmlContext.Block<MERItems> block;
    final private Map<String, Map<String, String>> fields;

    public NationalEconomyTableSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);

        String country = configuration.getString("country"); // $NON-NLS$
        ArrayList<String> types = configuration.getList("types"); // $NON-NLS$
        this.fields = getExplicitFieldList(configuration.getList("fields")); // $NON-NLS$

        this.block = this.context.addBlock("MER_Items"); // $NON-NLS$
        this.block.setParameter("country", country); // $NON-NLS$
        this.block.setParameters("type", types.toArray(new String[types.size()])); // $NON-NLS$

        this.setView(SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.2f, new TableCellRenderers.StringRenderer("--")),
                new TableColumn(I18n.I.value(), 0.2f, TableCellRenderers.DEFAULT_RIGHT) 
        })));
    }

    public void destroy() {
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            return;
        }

        final MerCountry merCountry = this.block.getResult().getCountry().get(0);
        final List<MerType> types = merCountry.getType();
        final FlexTableDataModel dtm = new FlexTableDataModel(2);

        int row = 0;
        for (MerType type : types) {
            dtm.setValueAt(row, 0, "<b>" + type.getName() + "<b>"); // $NON-NLS$
            row++;
            for (MerItem item : type.getItem()) {
                final String itemName = TypeListSnippet.getName(item, merCountry.getName());
                if (this.fields.containsKey(type.getName())) {
                    final String qid = item.getQuotedata().getQid();
                    if (this.fields.get(type.getName()).containsKey(qid)) {
                        final String display = this.fields.get(type.getName()).get(qid);
                        addRow(dtm, row, display, item);
                        row++;
                    }
                }
                else {
                    addRow(dtm, row, itemName, item);
                    row++;
                }
            }
        }
        getView().update(dtm);
    }

    private void addRow(FlexTableDataModel dtm, int row, String name, MerItem item) {
        dtm.setValueAt(row, 0, name);
        if (item.getPrice() == null) {
            dtm.setValueAt(row, 1, "<span class=\"mm-right\">--</span>"); // $NON-NLS$
        }
        else {
            final Link link = createLink(item);
            dtm.setValueAt(row, 1, link);
        }
    }

    private Link createLink(final MerItem item) {
        final String price = Renderer.LARGE_NUMBER.render(item.getPrice());
        final String currency = CurrencyRenderer.DEFAULT.render(item.getQuotedata().getCurrencyIso());
        final String text = currency + "<span class=\"economy-price-date\"> (" // $NON-NLS$
                + Formatter.LF.formatDateShort(item.getDate()) + ")</span>"; // $NON-NLS$
        return new Link(new LinkListener<Link>() {
            public void onClick(LinkContext linkContext, Element e) {
                PlaceUtil.goToChartcenter(item.getInstrumentdata(), item.getQuotedata());
            }
        }, "<span class=\"mm-link\">" + price + " " + text + "</span>", null); // $NON-NLS$
    }

    private Map<String, Map<String, String>> getExplicitFieldList(final List<String> fieldDefs) {
        Map<String, Map<String, String>> result = new HashMap<>();
        if (fieldDefs != null) {
            for (int i = 0, fieldDefsSize = fieldDefs.size(); i < fieldDefsSize; i++) {
                String fieldDef = fieldDefs.get(i);
                final String type = fieldDef.substring(0, fieldDef.indexOf(";", 0));
                final Map<String, String> fieldMap;

                if (result.containsKey(type)) {
                    fieldMap = result.get(type);
                }
                else {
                    fieldMap = new HashMap<>();
                    result.put(type, fieldMap);
                }
                Field field = parseFieldDef(fieldDef);
                fieldMap.put(field.getName(), field.getDisplay());
            }
        }
        return result;
    }

    //fieldDef-Format: type;quoteid;displayName
    private Field parseFieldDef(String fieldDef) {
        final String field;
        final String display;
        final int firstPoint = fieldDef.indexOf(";");
        final int secondPoint = fieldDef.indexOf(";", firstPoint + 1);

        field = fieldDef.substring(firstPoint + 1, secondPoint);
        display = fieldDef.substring(secondPoint + 1);

        return new Field(field, display);
    }
}
