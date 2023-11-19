package de.marketmaker.istar.merger.provider.funddata;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Delivers morningstars only for funds with german market admission.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MorningstarRatingProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private final AtomicReference<Map<Long, Integer>> morningstars = new AtomicReference<>();
    private File morningstarsFile;

    public void setMorningstarsFile(File morningstarsFile) {
        this.morningstarsFile = morningstarsFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        readMorningstars();

        final FileResource morningstarsResource = new FileResource(this.morningstarsFile);
        morningstarsResource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readMorningstars();
            }
        });
        this.activeMonitor.addResource(morningstarsResource);
    }

    private void readMorningstars() {
        try {
            final TimeTaker tt = new TimeTaker();

            final GZIPInputStream is = new GZIPInputStream(new FileInputStream(this.morningstarsFile));
            final SAXBuilder saxBuilder = new SAXBuilder();
            final Document document = saxBuilder.build(is);
            is.close();

            final Map<Long, Integer> tmp = new HashMap<>();
            //noinspection unchecked
            final List<Element> rows = document.getRootElement().getChildren("ROW");
            for (final Element row : rows) {
                final Long iid = Long.parseLong(row.getChildTextTrim("IID"));

                final String str = row.getChildTextTrim("MORNINGSTARS");
                if (StringUtils.hasText(str)) {
                    tmp.put(iid, Integer.parseInt(str));
                }
            }

            this.morningstars.set(tmp);

            this.logger.info("<readMorningstars> read morningstar ratings, took " + tt);
        }
        catch (Exception e) {
            this.logger.error("<readMorningstars> failed", e);
        }
    }

    public Integer getMorningstarRating(long instrumentid) {
        final Map<Long, Integer> map = this.morningstars.get();
        return map.get(instrumentid);
    }

    public static void main(String[] args) throws Exception {
        final MorningstarRatingProvider p = new MorningstarRatingProvider();
        p.setActiveMonitor(new ActiveMonitor());
        p.setMorningstarsFile(new File("/home/tkiesgen/produktion/var/data/provider/istar-morningstars.xml.gz"));
        p.afterPropertiesSet();
        System.out.println(p.getMorningstarRating(28107));
    }
}
