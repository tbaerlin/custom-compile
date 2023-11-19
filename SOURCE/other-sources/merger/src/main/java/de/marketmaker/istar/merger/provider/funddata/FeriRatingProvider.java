package de.marketmaker.istar.merger.provider.funddata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.xml.AbstractSaxReader;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeriRatingProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private final AtomicReference<Map<Long, String>> ratings = new AtomicReference<>();
    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        readRatings();

        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readRatings();
            }
        });
        this.activeMonitor.addResource(resource);
    }

    private void readRatings() {
        try {
            final RatingReader ur = new RatingReader();
            ur.read(this.file);

            synchronized (this) {
                this.ratings.set(ur.getRatings());
            }

            this.logger.info("<readRatings> read fund master data");
        }
        catch (Exception e) {
            this.logger.error("<readRatings> failed", e);
        }
    }

    public static class RatingReader extends AbstractSaxReader {
        final Map<Long, String> ratings = new HashMap<>();

        private long instrumentid = -1;
        private String rating;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("IID")) {
                    this.instrumentid = getCurrentLong();
                }
                else if (tagName.equals("INSTRUMENTID")) {
                    this.instrumentid = getCurrentLong();
                }
                else if (tagName.equals("FERIRATING")) {
                    this.rating = getCurrentString();
                }
                else if (tagName.equals("ROWS")) {
                    // ignored
                }
                else {
                    notParsed(tagName);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.instrumentid < 0) {
                reset();
                return;
            }

            if (StringUtils.hasText(this.rating)) {
                this.ratings.put(this.instrumentid, this.rating);
            }

            reset();
        }

        protected void reset() {
            this.instrumentid = -1;
            this.rating = null;
            this.errorOccured = false;
        }

        public Map<Long, String> getRatings() {
            return ratings;
        }
    }

    public String getFeriRating(long instrumentid) {
        final Map<Long, String> map = this.ratings.get();
        return map.get(instrumentid);
    }
}
