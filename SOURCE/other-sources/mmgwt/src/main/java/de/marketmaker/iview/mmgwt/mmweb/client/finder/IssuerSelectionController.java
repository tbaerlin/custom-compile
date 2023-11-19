package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.dmxml.FinderGroupCell;
import de.marketmaker.iview.dmxml.FinderGroupRow;
import de.marketmaker.iview.dmxml.FinderGroupTable;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MinMaxAvgElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.FlexTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author umaurer
 */
public class IssuerSelectionController extends AbstractPageController {
    private DmxmlContext.Block<FinderGroupTable> block;
    private HistoryToken historyToken;
    private IssuerSelectionView view;

    public IssuerSelectionController(ContentContainer contentContainer) {
        super(contentContainer);
        init();
    }

    private void init() {
        this.block = this.context.addBlock("MSC_FinderGroups"); // $NON-NLS-0$
        this.block.setParameter("type", "CER"); // $NON-NLS$
        this.block.setParameter("primaryField", "certificateSubtype"); // $NON-NLS$
        this.block.setParameter("secondaryField", "multiassetName"); // $NON-NLS$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS$
        this.block.setParameter("sortBy", "fieldname"); // $NON-NLS$
        this.block.setParameter("ascending", "true"); // $NON-NLS$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.historyToken = event.getHistoryToken();
        this.block.setParameter("query", buildQuery(this.historyToken)); // $NON-NLS$
        refresh();
    }

    private String buildQuery(HistoryToken historyToken) {
        final List<String> terms = new ArrayList<>(2);
        terms.add("issuername=='" + historyToken.get(2) + "'"); // $NON-NLS$
        terms.add("certificateType=='" + historyToken.get(3) + "'"); // $NON-NLS$
        return StringUtil.join("&&", terms); // $NON-NLS$
    }


    private static class UnderlyingData implements Comparable<UnderlyingData> {
        private final InstrumentData instrumentData;
        private final String multiassetName;
        private final String columnKey;

        public UnderlyingData(String columnKey, InstrumentData instrumentData) {
            this.columnKey = columnKey;
            this.instrumentData = instrumentData;
            this.multiassetName = null;
        }

        private UnderlyingData(String columnKey, String multiassetName) {
            this.columnKey = columnKey;
            this.multiassetName = multiassetName;
            this.instrumentData = null;
        }

        public boolean isMultiasset() {
            return this.multiassetName != null;
        }

        public InstrumentData getInstrumentData() {
            return this.instrumentData;
        }

        public String getMultiassetName() {
            return multiassetName;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public int compareTo(UnderlyingData o) {
            final String s1 = isMultiasset() ? this.multiassetName : this.instrumentData.getName();
            final String s2 = o.isMultiasset() ? o.multiassetName : o.instrumentData.getName();

            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    @Override
    protected void onResult() {
        if (this.view == null) {
            this.view = new IssuerSelectionView(this);
        }

        if (!this.block.isResponseOk()) {
            this.view.show(false, true, DefaultTableDataModel.NULL);
            return;
        }

        final List<IdentifierData> underlyings = this.block.getResult().getUnderlying();
        final List<FinderGroupRow> rows = this.block.getResult().getRow();

        final boolean manySubtypes = rows.size() > 1;
        final int subtypeOffset = manySubtypes ? 1 : 0;
        final int numRows = countRows(rows);
        final int numCols = 2 + subtypeOffset;
        final boolean doWrap = numRows > 5;

        FlexTableDataModel tdm;
        if (doWrap) {
            tdm = new FlexTableDataModel(numCols * 2);
        }
        else {
            tdm = new FlexTableDataModel(numCols);
        }

        final List<UnderlyingData> sortedUnderlyings = getUnderlyingData(underlyings, this.block.getResult().getColumn());
        Collections.sort(sortedUnderlyings);

        int i = 0;
        int counter = 0;
        int wrapOffset = 0;
        for (final FinderGroupRow row : rows) {
            if (manySubtypes) {
                if (doWrap) {
                    if ((rows.size() == 2 && row.equals(rows.get(1))) || //only 2 subtypes? wrap it!
                            (counter > (numRows / 2) && wrapOffset == 0) || //rather half data written? wrap it!
                            (row.equals(rows.get(rows.size() - 1)) && wrapOffset == 0)) { //last subtype and still no wrap? wrap it!
                        i = 0;
                        wrapOffset = +numCols;
                    }
                }
                tdm.setValueAt(i, wrapOffset, row.getKey());
                tdm.setValueAt(i++, subtypeOffset + wrapOffset, ""); // $NON-NLS-0$
            }

            final Map<String, FinderGroupCell> cellByColumn = new HashMap<>();

            for (final FinderGroupCell cell : row.getColumn()) {
                if (cell == null) {
                    continue;
                }
                cellByColumn.put(cell.getKey(), cell);
            }

            for (final UnderlyingData underlyingData : sortedUnderlyings) {
                final FinderGroupCell cell = cellByColumn.get(underlyingData.getColumnKey());

                if (cell == null) {
                    continue;
                }

                if (!manySubtypes && doWrap) {
                    if (counter > (numRows / 2) && wrapOffset == 0) { //no subtypes and rather half data written? wrap it!
                        i = 0;
                        wrapOffset = +numCols;
                    }
                }

                counter++;

                if (underlyingData.isMultiasset()) {
                    final LinkListener<Link> linkListener = new LinkListener<Link>() {
                        public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                            gotoFinder(null, underlyingData.getMultiassetName(), row.getKey());
                        }
                    };
                    tdm.setValueAt(i, subtypeOffset + wrapOffset, new Link(linkListener, underlyingData.getMultiassetName(), null));
                }
                else {
                    final LinkListener<Link> linkListener = new LinkListener<Link>() {
                        public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                            gotoFinder(underlyingData.getInstrumentData(), null, row.getKey());
                        }
                    };
                    tdm.setValueAt(i, subtypeOffset + wrapOffset, new Link(linkListener, underlyingData.getInstrumentData().getName(), null));
                }

                if (manySubtypes) {
                    tdm.setValueAt(i, wrapOffset, ""); // $NON-NLS-0$
                }
                final MinMaxAvgElement item = (MinMaxAvgElement) cell.getItem().get(0);
                tdm.setValueAt(i, 1 + subtypeOffset + wrapOffset, item.getCount());
                i++;
            }
        }
        this.view.show(doWrap, manySubtypes, tdm);
    }

    private int countRows(List<FinderGroupRow> rows) {
        int count = 0;
        for (final FinderGroupRow row : rows) {
            for (final FinderGroupCell cell : row.getColumn()) {
                if (cell == null) {
                    continue;
                }
                count++;
            }
        }
        return count;
    }

    private List<UnderlyingData> getUnderlyingData(List<IdentifierData> underlyings, List<String> columns) {
        final Map<String, IdentifierData> iidToIdentifier = new HashMap<>();
        for (final IdentifierData underlying : underlyings) {
            iidToIdentifier.put(underlying.getInstrumentdata().getIid(), underlying);
        }

        final List<UnderlyingData> result = new ArrayList<>();

        for (final String column : columns) {
            final String iidKey = column + ".iid"; // $NON-NLS-0$
            if (iidToIdentifier.containsKey(iidKey)) {
                final IdentifierData data = iidToIdentifier.get(iidKey);
                result.add(new UnderlyingData(column, data.getInstrumentdata()));
            }
            else {
                result.add(new UnderlyingData(column, column));
            }
        }

        return result;
    }

    public void gotoFinder(InstrumentData instrumentData, String multiassetName, String cerSubtype) {
        final String type = "CER"; // $NON-NLS-0$
        final FinderController controller = FinderControllerRegistry.get(LiveFinderCER.CER_FINDER_ID);
        if (controller == null) {
            return;
        }

        final FinderFormConfig config = new FinderFormConfig("multifind", type); // $NON-NLS-0$
        config.put(LiveFinderCER.BASE_ID, "true"); // $NON-NLS-0$
        config.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
        config.put(FinderFormKeys.ISSUER_NAME + "-item", this.historyToken.get(2)); // $NON-NLS-0$

        config.put(FinderFormKeys.TYPE, "true"); // $NON-NLS-0$
        config.put(FinderFormKeys.TYPE + "-item", this.historyToken.get(3)); // $NON-NLS-0$

        config.put(FinderFormKeys.SUBTYPE, "true"); // $NON-NLS-0$
        config.put(FinderFormKeys.SUBTYPE + "-item", cerSubtype); // $NON-NLS-0$
        // config.put(FinderFormKeys.SUBTYPE + "-text", cerSubtype); // $NON-NLS-0$

        if (instrumentData != null) {
            final String iid = instrumentData.getIid();
            final String instrumentId = iid.substring(0, iid.length() - 4);

            config.put(LiveFinderCER.UNDERLYING_ID, "true"); // $NON-NLS-0$
            config.put(FinderFormKeys.UNDERLYING, "true"); // $NON-NLS-0$
            config.put(FinderFormKeys.UNDERLYING + "-symbol", instrumentId); // $NON-NLS-0$
            config.put(FinderFormKeys.UNDERLYING + "-name", instrumentData.getName()); // $NON-NLS-0$
        }
        if (multiassetName != null) {
            config.put(FinderFormKeys.MULTI_ASSET_NAME, "true"); // $NON-NLS-0$
            config.put(FinderFormKeys.MULTI_ASSET_NAME + "-text", multiassetName); // $NON-NLS-0$
        }

        config.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
        config.put(FinderFormKeys.SORT + "-item", "wkn"); // $NON-NLS-0$ $NON-NLS-1$
        controller.prepareFind(config);
        PlaceUtil.goTo("M_LF_CER"); // $NON-NLS-0$
    }

}
