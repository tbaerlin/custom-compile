/*
 * LexiconProviderImpl.java
 *
 * Created on 02.08.2006 21:55:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.provider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.lexicon.LexiconElement;
import de.marketmaker.istar.domainimpl.lexicon.LexiconDownloader;
import de.marketmaker.istar.domainimpl.lexicon.LexiconElementImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LexiconProviderImpl implements InitializingBean, LexiconProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    private final static DateTimeFormatter DTF = ISODateTimeFormat.basicDateTimeNoMillis();

    private ActiveMonitor activeMonitor;

    private final AtomicReference<Map<String, LexiconData>> data = new AtomicReference<>();

    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        readElements();

        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    readElements();
                } catch (Exception e) {
                    logger.error("<propertyChange> failed", e);
                }
            }
        });

        this.activeMonitor.addResource(resource);
    }

    private void readElements() throws JDOMException, IOException {

        final TimeTaker tt = new TimeTaker();
        final SAXBuilder saxBuilder = new SAXBuilder();
        final InputStream is = file.getName().endsWith(".gz")
                ? new GZIPInputStream(new FileInputStream(file))
                : new FileInputStream(file);
        final Document document = saxBuilder.build(is);

        final Map<String, LexiconData> types = new HashMap<>();

        //noinspection unchecked
        final List<Element> typedElements = document.getRootElement().getChildren("elements");
        if (typedElements.isEmpty()) {
            //noinspection unchecked
            final List<Element> elements = document.getRootElement().getChildren("element");
            types.put(LexiconDownloader.DEFAULT_TYPE, readElements(elements));
        }
        else {
            for (final Element typedElement : typedElements) {
                final String type = typedElement.getAttributeValue("type");
                //noinspection unchecked
                final List<Element> elements = typedElement.getChildren("element");
                types.put(type, readElements(elements));
            }
        }

        this.data.set(types);
        is.close();
        this.logger.info("<readElements> took " + tt);
    }

    private LexiconData readElements(List<Element> elements) {
        final Map<String, List<LexiconElement>> elementsByInitials = new HashMap<>();
        final Map<String, LexiconElement> elementById = new HashMap<>();
        for (final Element element : elements) {
            final String id = element.getChildTextTrim("id");
            final String source = element.getChildTextTrim("source");
            final String initial = element.getChildTextTrim("initial");
            final String item = element.getChildTextTrim("item");
            final DateTime date = DTF.parseDateTime(element.getChildTextTrim("date"));
            final String text = element.getChildTextTrim("text");

            final LexiconElementImpl le = new LexiconElementImpl(id, source, initial, item, date, text);

            elementById.put(le.getId(), le);
            List<LexiconElement> les = elementsByInitials.get(le.getInitial());
            if (les == null) {
                les = new ArrayList<>();
                elementsByInitials.put(le.getInitial(), les);
            }
            les.add(le);
        }
        return new LexiconData(elementsByInitials, elementById);
    }

    public List<String> getInitials(String lexiconId) {
        final LexiconData lexiconData = getLexiconData(lexiconId);
        final List<String> initials = new ArrayList<>(lexiconData.getElementsByInitials().keySet());
        initials.sort(GERMAN_COLLATOR);
        return initials;
    }

    public List<LexiconElement> getElements(String lexiconId, String initial) {
        final LexiconData lexiconData = getLexiconData(lexiconId);
        final List<LexiconElement> els = lexiconData.getElementsByInitials().get(initial);
        if (els == null) {
            return Collections.emptyList();
        }
        final ArrayList<LexiconElement> result = new ArrayList<>(els);
        result.sort(new Comparator<LexiconElement>() {
            public int compare(LexiconElement o1, LexiconElement o2) {
                return GERMAN_COLLATOR.compare(o1.getItem(), o2.getItem());
            }
        });
        return result;
    }

    public LexiconElement getElement(String lexiconId, String id) {
        final LexiconData lexiconData = getLexiconData(lexiconId);
        return lexiconData.getElementById().get(id);
    }

    private LexiconData getLexiconData(String type) {
        final Map<String, LexiconData> map = this.data.get();
        if (type == null) {
            return map.get(LexiconDownloader.DEFAULT_TYPE);
        }
        return map.get(type);
    }

    public static void main(String[] args) throws Exception {
        final LexiconProviderImpl lp = new LexiconProviderImpl();
        lp.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/lexicon.xml.gz"));
        lp.readElements();

        provision(lp, LexiconDownloader.DEFAULT_TYPE, "X");
        provision(lp, "postbank", "X");
    }

    private static void provision(LexiconProviderImpl lp, String type, String initial) {
        System.out.println("initials:" + lp.getInitials(type));
        final List<LexiconElement> elements = lp.getElements(type, initial);
        if (CollectionUtils.isEmpty(elements)) {
            System.out.println("no elements found for initial: '" + initial + "' in lexicon type '" + type + "'");
        }
        else {
            for (LexiconElement element : elements) {
                System.out.println(element);
            }
            final String id = elements.get(0).getId();
            System.out.println("element for " + id + ": " + lp.getElement(type, id));
        }
    }

    private static class LexiconData {
        private Map<String, List<LexiconElement>> elementsByInitials;

        private Map<String, LexiconElement> elementById;

        private LexiconData(Map<String, List<LexiconElement>> elementsByInitials,
                Map<String, LexiconElement> elementById) {
            this.elementsByInitials = elementsByInitials;
            this.elementById = elementById;
        }

        public Map<String, List<LexiconElement>> getElementsByInitials() {
            return elementsByInitials;
        }

        public Map<String, LexiconElement> getElementById() {
            return elementById;
        }
    }
}
