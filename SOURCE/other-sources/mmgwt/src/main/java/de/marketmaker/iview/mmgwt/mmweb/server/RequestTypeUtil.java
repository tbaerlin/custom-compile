/*
 * RequestTypeUtil.java
 *
 * Created on 06.09.12 15:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.dmxml.Parameter;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.dmxml.RequestedBlockType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import org.apache.commons.lang.LocaleUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author oflege
 */
class RequestTypeUtil {
    private final JaxbHandler jaxbHandler;

    RequestTypeUtil(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    MoleculeRequest toMoleculeRequest(RequestType type) {
        final MoleculeRequest result = new MoleculeRequest();
        result.setAuthentication(type.getAuthentication());
        result.setAuthenticationType(type.getAuthenticationType());
        final String locale = type.getLocale();
        if (locale != null) {
            final String[] locales = locale.split(",");
            final List<Locale> listLocales = new ArrayList<>(locales.length);
            for (String l : locales) {
                listLocales.add(LocaleUtils.toLocale(l));
            }
            result.setLocales(listLocales);
        }
        for (RequestedBlockType t : type.getBlock()) {
            result.addAtom(t.getId(), t.getKey(), createParameters(t), t.getDependsOnId());
        }
        result.afterPropertiesSet();
        return result;
    }

    private Map<String, String[]> createParameters(RequestedBlockType blockType) {
        final Map<String, String[]> result = new HashMap<>();
        for (Parameter p : blockType.getParameter()) {
            result.put(p.getKey(), asValue(p, result));
        }
        return result;
    }

    private String[] asValue(Parameter p, Map<String, String[]> parameters) {
        if (p.getClass() == Parameter.class) {
            return join(parameters.get(p.getKey()), p.getValue());
        }
        // any other subclass of parameter because not == Parameter.class
        // and not instanceof ExchangeDataRequest
        return marshal(p, "parameter");
    }

    private String[] join(String[] values, String value) {
        return StringUtil.concat(values, value);
    }

    private String[] marshal(Parameter p, final String elementName) {
        return new String[]{this.jaxbHandler.marshal(Parameter.class, p, elementName)};
    }

    public String asString(RequestType type) {
        final StringBuilder sb = new StringBuilder(200);
        sb.append(type.getAuthentication()).append("/").append(type.getAuthenticationType());
        for (RequestedBlockType blockType : type.getBlock()) {
            sb.append(", Block[").append(blockType.getKey()).append(", id=").append(blockType.getId());
            if (blockType.getDependsOnId() != null) {
                sb.append(", dependsOn=").append(blockType.getDependsOnId());
            }
            sb.append(", [");
            String sep = "";
            for (Parameter p : blockType.getParameter()) {
                sb.append(sep);
                sep = ", ";
                sb.append(p.getKey()).append(" => ");
                if (p.getClass() == Parameter.class) {
                    sb.append(p.getValue());
                }
                else {
                    sb.append("_complex parameter:").append(p.getClass().getName()).append("_");
                }
            }
            sb.append("]]");
        }
        return sb.toString();
    }
}
