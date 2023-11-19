/*
 * XsdTypeDocuProvider.java
 *
 * Created on 16.03.2012 14:47:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.XsdType;
import de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu.dictionary.Class;
import de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu.dictionary.Dictionary;
import de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu.dictionary.Div;

import static de.marketmaker.istar.common.dmxmldocu.DictHtmlFormatUtil.*;


/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class XsdDocuProvider implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    private Unmarshaller dictionaryUnmarshaller;

    private Resource dictionaryFile;

    private Map<Pair<String, XsdType>, String> attributeDocu = new HashMap<>();

    private Map<XsdType, String> typeDocu = new HashMap<>();

    private Map<Pair<String, XsdType>, String> elementDocuByNameAndEnclosingType = new HashMap<>();

    private Map<Pair<String, XsdType>, String> elementDocuByNameAndType = new HashMap<>();

    public String getTypeDocu(XsdType type) {
        return typeDocu.get(type);
    }

    public String getAttributeDocu(String attributeName, XsdType declaringType) {
        return attributeDocu.get(Pair.of(attributeName, declaringType));
    }

    public String getElementDocu(String elementName, XsdType elementType, XsdType enclosingType) {
        // First try by name and enclosing type, as this is most specific.
        final String byEnclosingDocu =
                this.elementDocuByNameAndEnclosingType.get(Pair.of(elementName, enclosingType));
        if (byEnclosingDocu != null) {
            return byEnclosingDocu;
        }

        // If by enclosing type does not contain special entry
        final String byTypeDocu = this.elementDocuByNameAndType.get(Pair.of(elementName, elementType));
        if (byTypeDocu != null) {
            return byTypeDocu;
        }

        // otherwise, we can only give documentation for the type of the element
        return this.typeDocu.get(elementType);
    }

    public void afterPropertiesSet() throws Exception {
        try {
            this.logger.info("<afterPropertiesSet> Loading response dictionary form " + dictionaryFile);

            final Dictionary dict = unmarshallDictionary();

            final List<Dictionary.Body.Div> divs = dict.getBody().getDiv();
            Dictionary.Body.Div typeDesc = null, fieldDesc = null, attrDesc = null, fields = null;
            for (Dictionary.Body.Div div : divs) {
                switch (div.getId()) {
                    case TYPE_DESC:
                        typeDesc = div;
                        break;
                    case FIELD_DESC:
                        fieldDesc = div;
                        break;
                    case ATTR_DESC:
                        attrDesc = div;
                        break;
                    case FIELDS:
                        fields = div;
                        break;
                }
            }
            for (Div div : typeDesc.getDiv()) {
                if (div.getClazz() == Class.DEF) {
                    typeDocu.put(new XsdType(div.getId()), getUnescapeDivValue(div));
                }
            }
            for (Div div : fields.getDiv()) {
                if (div.getClazz() == Class.DEF) {
                    elementDocuByNameAndType.put(decodeFieldId(div.getId()), getUnescapeDivValue(div));
                }
            }
            for (Div div : attrDesc.getDiv()) {
                if (div.getClazz() == Class.DEF) {
                    attributeDocu.put(decodeAttributeId(div.getId()), getUnescapeDivValue(div));
                }
            }
            for (Div div : fieldDesc.getDiv()) {
                if (div.getClazz() == Class.DEF) {
                    elementDocuByNameAndEnclosingType.put(decodeFieldDescId(div.getId()), getUnescapeDivValue(div));
                }
            }
        } catch (Exception e) {
            this.logger.warn("<afterPropertiesSet> Error reading dictionary File " + dictionaryFile, e); // $NON-NLS$
        }
    }

    private String getUnescapeDivValue(Div div) {
        return StringEscapeUtils.unescapeXml(div.getValue());
    }

    private Dictionary unmarshallDictionary() throws IOException, JAXBException {
        final BufferedInputStream inputStream = new BufferedInputStream(dictionaryFile.getInputStream());
        final Dictionary dict;
        synchronized (this.dictionaryUnmarshaller) {
            @SuppressWarnings("unchecked")
            final JAXBElement<Dictionary> element =
                    (JAXBElement<Dictionary>) dictionaryUnmarshaller.unmarshal(
                            new StreamSource(inputStream));
            dict = element.getValue();
        }
        return dict;
    }

    private Pair<String, XsdType> decodeFieldDescId(String id) {
        final String declaringType = StringUtils.substringBeforeLast(id, TYPE_ELEMENT_SEP);
        final String elementName = StringUtils.substringAfterLast(id, TYPE_ELEMENT_SEP);
        return Pair.of(elementName, new XsdType(declaringType));
    }

    private Pair<String, XsdType> decodeAttributeId(String id) {
        final String declaringType = StringUtils.substringBeforeLast(id, TYPE_ATTRIBUTE_SEP);
        final String attrName = StringUtils.substringAfterLast(id, TYPE_ATTRIBUTE_SEP);
        return Pair.of(attrName, new XsdType(declaringType));
    }


    private Pair<String, XsdType> decodeFieldId(String id) {
        final String fieldName = StringUtils.substringBeforeLast(id, FIELD_FIELD_TYPE_SEP);
        final String fieldTypeName = StringUtils.substringAfterLast(id, FIELD_FIELD_TYPE_SEP);
        return Pair.of(fieldName, new XsdType(fieldTypeName));
    }

    public Unmarshaller getDictionaryUnmarshaller() {
        return dictionaryUnmarshaller;
    }

    public void setDictionaryUnmarshaller(Unmarshaller dictionaryUnmarshaller) {
        this.dictionaryUnmarshaller = dictionaryUnmarshaller;
    }

    public Resource getDictionaryFile() {
        return dictionaryFile;
    }

    public void setDictionaryFile(Resource dictionaryFile) {
        this.dictionaryFile = dictionaryFile;
    }


    /**
     * Pair utility class for maps
     *
     * @param <A>
     * @param <B>
     */
    private static final class Pair<A, B> {
        public final A a;

        public final B b;

        public static <A, B> Pair<A, B> of(A a, B b) {
            return new Pair<>(a, b);
        }

        private Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return !(a != null ? !a.equals(pair.a) : pair.a != null) && !(b != null ? !b.equals(pair.b) : pair.b != null);
        }

        @Override
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Pair{a=" + a + ", b=" + b + '}'; // $NON-NLS$
        }
    }
}
