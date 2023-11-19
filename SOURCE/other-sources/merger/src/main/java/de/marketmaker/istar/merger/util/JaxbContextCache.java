/*
 * JaxbContextCache.java
 *
 * Created on 14.10.2008 14:45:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central repository for JAXBContext objects.
 * Since each JAXBContext tends to load all required jaxb classes, this class helps to avoid
 * excessive (un)loading of classes.<p>
 * The JAXBContexts returned are thread-safe, (un)marshallers obtained from the context are not.
 * @see <a href="https://jaxb.dev.java.net/faq/index.html#threadSafety">JAXB Thread-Safety</a>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class JaxbContextCache {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, JAXBContext> contexts = new ConcurrentHashMap<>();

    public static final JaxbContextCache INSTANCE = new JaxbContextCache();

    private JaxbContextCache() {
    }

    public JAXBContext getContext(String packageName) throws JAXBException {
        JAXBContext context = this.contexts.get(packageName);
        if (context == null) {
            try {
                context = JAXBContext.newInstance(packageName);
                this.contexts.put(packageName, context);
                this.logger.info("<getContext> created for package " + packageName);
            }
            catch (JAXBException e) {
                this.logger.error("<getContext> failed for package " + packageName, e);
                throw e;
            }
        }
        return context;
    }
}
