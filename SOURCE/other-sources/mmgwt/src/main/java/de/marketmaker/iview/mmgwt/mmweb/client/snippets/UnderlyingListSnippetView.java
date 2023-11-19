/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderControllerRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderSection;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnderlyingListSnippetView extends SnippetView<UnderlyingListSnippet> {
    private final SnippetTableWidget tw;
    private final String instrumentType;

    public UnderlyingListSnippetView(UnderlyingListSnippet snippet, SnippetConfiguration config) {
        super(snippet);
        setTitle(config.getString("title", I18n.I.underlyings()));  // $NON-NLS-0$

        this.instrumentType = config.getString("instrumentType"); // $NON-NLS-0$

        final TableColumnModel columnModel = createTableColumnModel();
        this.tw = SnippetTableWidget.create(columnModel);

        final List<QuoteWithInstrument> qwis = SessionData.INSTANCE.getList(config.getString("list")); // $NON-NLS-0$
        assert (qwis != null);

        final TableDataModelBuilder builder = new TableDataModelBuilder(qwis.size(), 1);
        for (final QuoteWithInstrument qwi : qwis) {
            final LinkListener<Link> listener = new LinkListener<Link>() {
                public void onClick(LinkContext<Link> linkContext, Element e) {
                    gotoFinder(qwi);
                }
            };
            builder.addRow(new Link(listener, qwi.getName(), null));
        }
        builder.addRow("TecDAX"); // $NON-NLS-0$
        this.tw.updateData(builder.getResult());
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.tw);
    }

    public static DefaultTableColumnModel createTableColumnModel() {
        final TableColumn[] result = new TableColumn[]{
            new TableColumn(null, 1f, TableCellRenderers.DEFAULT),
        };

        return new DefaultTableColumnModel(result, false);
    }

    private void gotoFinder(QuoteWithInstrument qwi) {
        final FinderController controller = FinderControllerRegistry.get("L" + this.instrumentType); // $NON-NLS-0$
        if (controller == null) {
            Firebug.log("FinderController not found for instrumentType " + this.instrumentType); // $NON-NLS-0$
            return;
        }
        final FinderFormConfig ffc = new FinderFormConfig();

        ffc.put(FinderFormKeys.VIEW, FinderFormKeys.DISPLAY_SETTINGS);

        FinderSection.enableUnderlyingSection(ffc, this.instrumentType);
        ffc.put(FinderFormKeys.UNDERLYING, "true"); // $NON-NLS-0$
        final InstrumentData instrumentData = qwi.getInstrumentData();
        final String iid = instrumentData.getIid();
        final String symbol = iid.substring(0, iid.length() - 4);
        ffc.put(FinderFormKeys.UNDERLYING + "-symbol", symbol); // $NON-NLS-0$
        ffc.put(FinderFormKeys.UNDERLYING + "-name", instrumentData.getName()); // $NON-NLS-0$

        ffc.put(FinderFormKeys.SORT, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS-0$ $NON-NLS-1$

        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_" + this.instrumentType); // $NON-NLS-0$
    }

}
