package de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCProfiledQuoteList;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.ListLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;

/**
 * @author Ulrich Maurer
 *         Date: 07.12.11
 */
public class KwtProductMap {
    public static final String LISTS_KEY = "kwt_productmap"; // $NON-NLS$
    public static final String INTEREST_KEY = "zinspapiere"; // $NON-NLS$

    public static final String CSV_FIELD_NAME = "name"; // $NON-NLS$
    public static final String CSV_FIELD_URL = "url"; // $NON-NLS$
    public static final String CSV_FIELD_COUPON = "kupon"; // $NON-NLS$
    public static final String CSV_FIELD_TEXT = "text"; // $NON-NLS$
    public static final String CSV_FIELD_MATURITY = "laufzeit"; // $NON-NLS$
    public static final String CSV_FIELD_ISIN = "isin"; // $NON-NLS$
    public static final String CSV_FIELD_PRICE = "nettokurs"; // $NON-NLS$
    public static final String CSV_FIELD_DURATION = "duration"; // $NON-NLS$
    public static final String CSV_FIELD_YIELD = "rendite"; // $NON-NLS$
    public static final String CSV_FIELD_PRICEDATE = "kursdatum"; // $NON-NLS$

    public static final String LIST_ENTRY_NAME = "name"; // $NON-NLS$
    public static final String LIST_ENTRY_NOTE = "note"; // $NON-NLS$
    public static final String LIST_ENTRY_URL = "url"; // $NON-NLS$

    private static final String[] CSV_FIELDS = {CSV_FIELD_NAME, CSV_FIELD_URL, CSV_FIELD_COUPON, CSV_FIELD_TEXT, CSV_FIELD_MATURITY, CSV_FIELD_ISIN, CSV_FIELD_PRICE, CSV_FIELD_DURATION, CSV_FIELD_YIELD, CSV_FIELD_PRICEDATE};

    public static class List {
        private final String id;
        private final String title;
        private final String listid;
        private final ListEntry[] listEntries;
        private boolean identified = false;

        public List(String id, String title, String listid, ListEntry[] listEntries) {
            this.id = id;
            this.title = title;
            this.listid = listid;
            this.listEntries = listEntries;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getListid() {
            return listid;
        }

        public ListEntry[] getListEntries() {
            return listEntries;
        }

        public ArrayList<ListEntry> getListEntries(String instrumentType) {
            final ArrayList<ListEntry> listEntries = new ArrayList<ListEntry>(this.listEntries.length);
            for (ListEntry listEntry : this.getListEntries()) {
                if (instrumentType.equals(listEntry.getInstrumentType())) {
                    listEntries.add(listEntry);
                }
            }
            return listEntries;
        }

        public boolean isIdentified() {
            return identified;
        }

        public void setIdentified(boolean identified) {
            this.identified = identified;
        }
    }

    public static class ListEntry {
        private final String id;
        private final String name;
        private final String note;
        private final String url;
        private final HashMap<String, String> mapData;

        private QuoteWithInstrument qwi = null;

        public ListEntry(String id, String name, String note, String url, HashMap<String, String> mapData) {
            this.id = id;
            this.name = name;
            this.note = note;
            this.url = url;
            this.mapData = mapData;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            if (this.name != null) {
                return name;
            }
            if (this.mapData == null) {
                return null;
            }
            final String name = this.mapData.get(CSV_FIELD_NAME);
            if (name != null) {
                return name;
            }
            return this.mapData.get(CSV_FIELD_COUPON) + " " + this.mapData.get(CSV_FIELD_TEXT);
        }

        public String getNote() {
            return note;
        }

        public String getUrl() {
            return url;
        }

        public String getData(String key) {
            return this.mapData == null ? null : this.mapData.get(key);
        }

        public boolean hasData() {
            return this.mapData != null;
        }

        public void setQwi(QuoteWithInstrument qwi) {
            this.qwi = qwi;
        }

        public QuoteWithInstrument getQwi() {
            return qwi;
        }

        public String getInstrumentType() {
            return hasData() ? "BND" : (this.qwi == null ? null : this.qwi.getInstrumentData().getType()); // $NON-NLS$
        }
    }
    
    public static class ListEntryComparator implements Comparator<ListEntry> {
        private final NaturalComparator<String> comparator;
        private final String csvField;

        public ListEntryComparator(String csvField, boolean ascending) {
            this.csvField = csvField;
            this.comparator = new NaturalComparator<String>('.', ',', ascending);
        }

        public int compare(ListEntry le1, ListEntry le2) {
            if (!"name".equals(this.csvField)) { // $NON-NLS$
                final String f1 = le1.getData(this.csvField);
                final String f2 = le2.getData(this.csvField);
                if (f1 != null) {
                    if (f2 != null) {
                        int result = comparator.compare(f1, f2);
                        if (result != 0) {
                            return result;
                        }
                    }
                    else {
                        return -1;
                    }
                }
                else if (f2 != null) {
                    return 1;
                }
            }
            return comparator.compare(le1.getName(), le2.getName());
        }
    }

    public interface ListCallback {
        void onListAvailable(List list);
        void onListNotAvailable();
    }

    private final HashMap<String, List> mapLists;

    public KwtProductMap() {
        this.mapLists = readLists();
    }

    private HashMap<String, List> readLists() {
        final JSONWrapper jsonLists = SessionData.INSTANCE.getGuiDef(LISTS_KEY);
        final int listsCount = jsonLists.size();
        final HashMap<String, List> mapLists = new HashMap<String, List>(listsCount);
        for (int i = 0; i < listsCount; i++) {
            final JSONWrapper jsonList = jsonLists.get(i);
            final String id = jsonList.getString("id"); // $NON-NLS$
            final String title = jsonList.getString("title"); // $NON-NLS$
            final String listid = jsonList.getString("listid"); // $NON-NLS$
            final ListEntry[] listEntries = readListEntries(listid);
            mapLists.put(listid, new List(id, title, listid, listEntries));
        }
        return mapLists;
    }

    private HashMap<String, String> getInterestData(String id) {
        final JSONWrapper jsonEntry = SessionData.INSTANCE.getGuiDef(INTEREST_KEY).get(id);
        if (!jsonEntry.isValid()) {
            return null;
        }
        final HashMap<String, String> mapEntry = new HashMap<String, String>();
        for (String entryField : CSV_FIELDS) {
            mapEntry.put(entryField, jsonEntry.getString(entryField));
        }
        return mapEntry;
    }

    private ListEntry[] readListEntries(String listid) {
        final JSONWrapper jsonList = SessionData.INSTANCE.getGuiDef(listid).get("elements"); // $NON-NLS$
        final int listSize = jsonList.size();
        final ListEntry[] listEntries = new ListEntry[listSize];
        for (int i = 0; i < listSize; i++) {
            final JSONWrapper jsonEntry = jsonList.get(i);
            final String id = jsonEntry.getString("id"); // $NON-NLS$
            final HashMap<String, String> mapData = getInterestData(id);
            listEntries[i] = new ListEntry(id, jsonEntry.getString(LIST_ENTRY_NAME), jsonEntry.getString(LIST_ENTRY_NOTE), jsonEntry.getString(LIST_ENTRY_URL), mapData);
        }
        return listEntries;
    }

    public String getList(String id, ListCallback callback) {
        final List list = this.mapLists.get(id);
        if (list == null) {
            callback.onListNotAvailable();
            return null;
        }

        if (list.isIdentified()) {
            callback.onListAvailable(list);
        }
        else {
            identifyList(list, callback);
        }
        return list.getTitle();
    }

    private void identifyList(final List list, final ListCallback callback) {
        final DmxmlContext context = new DmxmlContext();
        final DmxmlContext.Block<MSCProfiledQuoteList> block = ListLoader.createBlock(context, list.getListid());
        context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
                Firebug.error("list request failed", caught);
                callback.onListNotAvailable();
            }

            public void onSuccess(ResponseType result) {
                final Map<String,QuoteWithInstrument> quotesByRequestSymbol = ListLoader.getQuotesByRequestSymbol(block);
                for (ListEntry listEntry : list.getListEntries()) {
                    if (!listEntry.hasData()) {
                        final QuoteWithInstrument qwi = quotesByRequestSymbol.get(listEntry.getId());
                        listEntry.setQwi(qwi);
                    }
                }
                list.setIdentified(true);
                callback.onListAvailable(list);
            }
        });
    }
}
