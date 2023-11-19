package de.marketmaker.istar.merger.provider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.DerivativeIpoData;
import de.marketmaker.istar.domainimpl.data.DerivativeIpoDataImpl;
import de.marketmaker.istar.domainimpl.data.DownloadableItemImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DerivativeIpoProviderDzbankImpl implements InitializingBean,
        DerivativeIpoProviderDzbank {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File file;

    private final AtomicReference<List<DerivativeIpoData>> dzInstruments = new AtomicReference<>();

    private final AtomicReference<List<DerivativeIpoData>> wgzInstruments = new AtomicReference<>();

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readData();
            }
        });
        this.activeMonitor.addResource(resource);

        readData();
    }

    public List<DerivativeIpoData> getDerivateIposDzbank(String type) {
        if ("dz".equalsIgnoreCase(type)) {
            return new ArrayList<>(this.dzInstruments.get());
        }

        if ("wgz".equalsIgnoreCase(type)) {
            return new ArrayList<>(this.wgzInstruments.get());
        }

        return Collections.emptyList();
    }

    private void readData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final DerivativeIpoReader r = new DerivativeIpoReader();
            r.read(this.file);

            this.dzInstruments.set(r.getDzIpos());
            this.wgzInstruments.set(r.getWgzIpos());

            this.logger.info("<readData> read " + this.file + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readData> failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
        final DerivativeIpoProviderDzbankImpl provider = new DerivativeIpoProviderDzbankImpl();
        provider.setActiveMonitor(new ActiveMonitor());
        provider.setFile(new File("/home/tkiesgen/produktion/var/data/provider/istar-dzbank-derivative-ipos.xml.gz"));
        provider.afterPropertiesSet();

        System.out.println(provider.getDerivateIposDzbank("dz"));
        System.out.println(provider.getDerivateIposDzbank("wgz"));
    }

    public static class DerivativeIpoReader extends AbstractSaxReader {
        private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

        private final List<DerivativeIpoData> dzIpos = new ArrayList<>();

        private final List<DerivativeIpoData> wgzIpos = new ArrayList<>();

        private String issuertype;

        private String wkn;

        private String name;

        private LocalDate subscriptionStart;

        private LocalDate subscriptionEnd;

        private LocalDate valutaDate;

        private LocalDate expirationDate;

        private String url;

        private DateTime fileDate;

        private String reporttype;

        private int sort;

        private boolean dzPib;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                if (tagName.equals("ISSUERTYPE")) {
                    this.issuertype = getCurrentString();
                }
                else if (tagName.equals("WKN")) {
                    this.wkn = getCurrentString();
                }
                else if (tagName.equals("NAME")) {
                    this.name = getCurrentString();
                }
                else if (tagName.equals("REPORTTYPE")) {
                    this.reporttype = getCurrentString();
                }
                else if (tagName.equals("REPORTFILENAME")) {
                    this.url = getCurrentString();
                }
                else if (tagName.equals("FILEDATE")) {
                    this.fileDate = getDate();
                }
                else if (tagName.equals("SUBSCRIPTIONPERIODSTART")) {
                    this.subscriptionStart = getLocalDate();
                }
                else if (tagName.equals("SUBSCRIPTIONPERIODEND")) {
                    this.subscriptionEnd = getLocalDate();
                }
                else if (tagName.equals("VALUTADATE")) {
                    this.valutaDate = getLocalDate();
                }
                else if (tagName.equals("EXPIRATIONDATE")) {
                    this.expirationDate = getLocalDate();
                }
                else if (tagName.equals("SORT_")) {
                    this.sort = getCurrentInt();
                }
                else if (tagName.equals("DZPIB")) {
                    this.dzPib = getCurrentBoolean();
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

        private DateTime getDate() {
            return DTF.parseDateTime(getCurrentString(false));
        }

        private LocalDate getLocalDate() {
            return getDate().toLocalDate();
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.issuertype == null || wkn == null) {
                reset();
                return;
            }

            final List<DerivativeIpoData> items = getList();

            DerivativeIpoDataImpl existing = null;
            for (final DerivativeIpoData item : items) {
                if (this.wkn.equals(item.getWkn())) {
                    existing = (DerivativeIpoDataImpl) item;
                    break;
                }
            }

            if (existing == null) {
                existing = new DerivativeIpoDataImpl(this.wkn, this.name, this.subscriptionStart, this.subscriptionEnd,
                        this.valutaDate, this.expirationDate, this.sort, this.dzPib);
                items.add(existing);
            }

            if (this.url != null) {
                existing.addReport(new DownloadableItemImpl(null, this.reporttype, this.url, this.fileDate, null));
            }

            reset();
        }

        private List<DerivativeIpoData> getList() {
            if ("dz".equals(this.issuertype)) {
                return this.dzIpos;
            }
            else if ("wgz".equals(this.issuertype)) {
                return this.wgzIpos;
            }
            return null;
        }

        protected void reset() {
            this.issuertype = null;
            this.wkn = null;
            this.name = null;
            this.subscriptionStart = null;
            this.subscriptionEnd = null;
            this.valutaDate = null;
            this.expirationDate = null;
            this.url = null;
            this.fileDate = null;
            this.reporttype = null;
            this.sort = -1000;
            this.errorOccured = false;
        }

        public List<DerivativeIpoData> getDzIpos() {
            return dzIpos;
        }

        public List<DerivativeIpoData> getWgzIpos() {
            return wgzIpos;
        }
    }
}
