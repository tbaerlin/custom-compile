package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.WGZCertificateData;
import de.marketmaker.istar.domainimpl.data.WGZCertificateDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WGZCertificateProviderImpl implements InitializingBean, WGZCertificateProvider {

    public static class WGZCertificateReader extends IstarMdpExportReader<List<WGZCertificateData>> {
        private final List<WGZCertificateData> result = new ArrayList<>();

        protected List<WGZCertificateData> getResult() {
            return this.result;
        }

        protected void handleRow() {
            final Integer cell = getInt("CELL");
            final String listid = get("LISTID");
            final String name = get("NAME");
            final String sortColumn = get("SORTCOLUMN");
            final String columnList = get("COLUMNLIST");
            final Long instrumentid = getLong("INSTRUMENTID", "IID");

            if (cell == null || listid == null || name == null || instrumentid == null) {
                return;
            }

            WGZCertificateDataImpl element = findElement(name, cell);
            if (element == null) {
                element = new WGZCertificateDataImpl(cell, listid, name,
                        (columnList == null) ? Collections.<String>emptyList() : Arrays.<String>asList(columnList.split(" ")),
                        sortColumn);
                this.result.add(element);
            }
            element.add(instrumentid);
        }

        private WGZCertificateDataImpl findElement(String name, Integer cell) {
            for (final WGZCertificateData cd : this.result) {
                if (cd.getName().equals(name) && cd.getCell().equals(cell)) {
                    return (WGZCertificateDataImpl) cd;
                }
            }
            return null;
        }

        protected void endDocument() {
            this.result.sort(new Comparator<WGZCertificateData>() {
                public int compare(WGZCertificateData o1, WGZCertificateData o2) {
                    final int diff = o1.getCell() - o2.getCell();
                    if (diff != 0) {
                        return diff;
                    }
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File file;

    private final AtomicReference<List<WGZCertificateData>> data
            = new AtomicReference<>(Collections.<WGZCertificateData>emptyList());

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

    public List<WGZCertificateData> getWGZCertificateData() {
        return this.data.get();
    }

    private void readData() {
        try {
            final TimeTaker tt = new TimeTaker();
            final List<WGZCertificateData> list = new WGZCertificateReader().read(this.file);
            this.data.set(list);

            this.logger.info("<readData> read wgz certificates, took " + tt);
        }
        catch (Exception e) {
            this.logger.error("<readData> failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
        final WGZCertificateProviderImpl provider = new WGZCertificateProviderImpl();
        provider.setActiveMonitor(new ActiveMonitor());
        provider.setFile(new File("d:/produktion/var/data/provider/istar-dzbank-wgz-certificates.xml.gz"));
        provider.afterPropertiesSet();

        for (final WGZCertificateData wgzCertificateData : provider.getWGZCertificateData()) {
            System.out.println(wgzCertificateData);
        }
    }
}
