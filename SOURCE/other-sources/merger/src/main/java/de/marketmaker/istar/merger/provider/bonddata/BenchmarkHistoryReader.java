package de.marketmaker.istar.merger.provider.bonddata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.xml.AbstractSaxReader;

class BenchmarkHistoryReader extends AbstractSaxReader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final Map<String, List<BenchmarkHistoryResponse.BenchmarkHistoryItem>> values = new HashMap<>();

    private String vwdsymbol;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String benchmarkIsin;

    private Long benchmarkIid;

    public void endElement(String uri, String localName, String tagName) throws SAXException {
        try {
            if (tagName.equals("ROW")) {
                // i have finished a new row => create
                storeFields();
            }
            else if (tagName.equals("SYMBOL")) {
                this.vwdsymbol = getCurrentString();
            }
            else if (tagName.equals("IID_BENCHMARK")) {
                this.benchmarkIid = getCurrentLong();
            }
            else if (tagName.equals("ISIN_BENCHMARK")) {
                this.benchmarkIsin = getCurrentString();
            }
            else if (tagName.equals("FROMDATE")) {
                this.fromDate = getDate();
            }
            else if (tagName.equals("TODATE")) {
                this.toDate = getDate();
            }
            else if (tagName.equals("ROWS")) {
                //ignored
            }
            else {
                notParsed(tagName);
            }
        } catch (Exception e) {
            this.logger.error("<endElement> error in " + tagName, e);
            this.errorOccured = true;
        }
    }

    private LocalDate getDate() {
        final DateTime dt = getCurrentDateTime(DTF, false);
        return dt == null ? null : dt.toLocalDate();
    }


    private void storeFields() {
        this.limiter.ackAction();

        if (this.errorOccured) {
            reset();
            return;
        }

        if (this.vwdsymbol == null || this.benchmarkIsin == null) {
            reset();
            return;
        }

        final BenchmarkHistoryResponse.BenchmarkHistoryItem item
                = new BenchmarkHistoryResponse.BenchmarkHistoryItem(this.vwdsymbol, this.benchmarkIsin, this.benchmarkIid, this.fromDate, this.toDate);

        List<BenchmarkHistoryResponse.BenchmarkHistoryItem> items = this.values.get(this.vwdsymbol);
        if (items == null) {
            items = new ArrayList<>();
            this.values.put(this.vwdsymbol, items);
        }

        items.add(item);

        reset();
    }

    protected void reset() {
        this.vwdsymbol = null;
        this.benchmarkIsin = null;
        this.fromDate = null;
        this.toDate = null;
        this.errorOccured = false;
    }

    public Map<String, List<BenchmarkHistoryResponse.BenchmarkHistoryItem>> getValues() {
        for (final List<BenchmarkHistoryResponse.BenchmarkHistoryItem> items : values.values()) {
            items.sort(new Comparator<BenchmarkHistoryResponse.BenchmarkHistoryItem>() {
                @Override
                public int compare(BenchmarkHistoryResponse.BenchmarkHistoryItem i1,
                        BenchmarkHistoryResponse.BenchmarkHistoryItem i2) {
                    final LocalDate s1 = i1.getStart();
                    final LocalDate s2 = i2.getStart();
                    if (s1 == null && s2 == null) {
                        return 0;
                    }
                    if (s1 == null) {
                        return -1;
                    }
                    if (s2 == null) {
                        return 1;
                    }
                    return s1.compareTo(s2);
                }
            });
        }
        return values;
    }

    public static void main(String[] args) throws IOException {
        final BenchmarkHistoryResponse.BenchmarkHistoryItem item = new BenchmarkHistoryResponse.BenchmarkHistoryItem("A", "B", 1L, new LocalDate(), new LocalDate());
        final FileOutputStream fos = new FileOutputStream("/Users/tkiesgen/tmp/tmp.obj");
        final ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(item);
        oos.close();
    }
}
