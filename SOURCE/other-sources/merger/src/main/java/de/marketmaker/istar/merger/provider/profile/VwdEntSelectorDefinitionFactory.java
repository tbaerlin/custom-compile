/*
 * VwdEntSelectorDefinitionFactory.java
 *
 * Created on 23.07.12 11:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus Dick
 */
public class VwdEntSelectorDefinitionFactory implements ResponseExtractor<Map<Integer, SelectorDefinition>> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String ROOT_NAME = "vwdSelectorDefinitions";
    private static final String SELECTOR_DEFINITION_NAME = "Sel";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String SELECTOR_ATTRIBUTE_NAME = "selector";
    private static final String SHORTNAME_ATTRIBUTE_NAME = "shortname";
    private static final String SERVICE_ATTRIBUTE_NAME = "service";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String SUBTYPE_ATTRIBUTE_NAME = "subtype";
    private static final String DESCRIPTION_ATTRIBUTE_NAME = "description";


    @Override
    public Map<Integer, SelectorDefinition> extractData(
            ClientHttpResponse response) throws IOException {
        try {
            return read(response.getBody());
        } catch (JDOMException e) {
            throw new IOException(e);
        }
    }

    Map<Integer, SelectorDefinition> read(InputStream in) throws IOException, JDOMException {
        int skipStat = 0;

        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(in);
        final Element root = document.getRootElement();

        if(ROOT_NAME.equals(root.getName())) {
            Map<Integer, SelectorDefinition> result = new HashMap<>();

            for(Object o : root.getChildren(SELECTOR_DEFINITION_NAME)) {
                final Element element = (Element)o;
                try {
                    final Integer id = Integer.valueOf(element.getAttributeValue(ID_ATTRIBUTE_NAME));
                    final SelectorDefinition sd = new SelectorDefinition();
                    sd.setId(id);
                    sd.setSelector(element.getAttributeValue(SELECTOR_ATTRIBUTE_NAME));
                    sd.setShortname(element.getAttributeValue(SHORTNAME_ATTRIBUTE_NAME));
                    sd.setService(element.getAttributeValue(SERVICE_ATTRIBUTE_NAME));
                    sd.setType(element.getAttributeValue(TYPE_ATTRIBUTE_NAME));
                    sd.setSubtype(element.getAttributeValue(SUBTYPE_ATTRIBUTE_NAME));
                    sd.setDescription(element.getAttributeValue(DESCRIPTION_ATTRIBUTE_NAME));

                    result.put(id, sd);
                }
                catch(NumberFormatException nfe) {
                    logger.error("<read> Skipping element: " + element);
                    skipStat++;
                }
            }
            logger.info("<read> Skipped SelectorDefinitions " + skipStat);
            return result;
        }
        else {
            logger.error("<read> Unexpected root element for SelectorDefinition: " + root.getName());
        }

        return null;
    }
}
