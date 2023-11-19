/*
 * VwdProfileFactory.java
 *
 * Created on 26.06.2008 10:56:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.profile.Profile;

import static de.marketmaker.istar.domain.data.PriceQuality.DELAYED;
import static de.marketmaker.istar.domain.data.PriceQuality.REALTIME;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdProfileFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Profile.Aspect, Set<Integer>> allowedFieldsByAspect;

    private final Map<Profile.Aspect, Set<Integer>> forbiddenFieldsByAspect;

    private static final int MAX_QLT = 4;

    public VwdProfileFactory(Map<Profile.Aspect, Set<Integer>> allowedFieldsByAspect,
            Map<Profile.Aspect, Set<Integer>> forbiddenFieldsByAspect) {
        this.allowedFieldsByAspect = allowedFieldsByAspect;
        this.forbiddenFieldsByAspect = forbiddenFieldsByAspect;
    }

    public VwdProfileFactory() {
        this(null, null);
    }

    public VwdProfile read(InputStream is) throws Exception {
        final SAXBuilder builder = new SAXBuilder();
        try {
            return doRead(builder.build(is));
        } finally {
            IoUtils.close(is);
        }
    }

    public VwdProfile read(String xml) throws Exception {
        return doRead(new SAXBuilder().build(new StringReader(xml)));
    }

    private VwdProfile doRead(final Document document) throws JDOMException, IOException {
        final Element root = document.getRootElement();
        final Element header = root.getChild("Terminal").getChild("Header");

        final VwdProfile result = new VwdProfile();
        result.setVwdId(header.getChildTextTrim("vwdId"));
        result.setState(getState(header));
        if (result.getState() != VwdProfile.State.ACTIVE) {
            return result;
        }

        result.setTerminalName(header.getChildTextTrim("TerminalName"));
        result.setProduktId(header.getChildTextTrim("ProduktId"));
        result.setKonzernId(header.getChildTextTrim("KonzernId"));
        result.setCreated(toDateTime(header.getChildTextTrim("created")));
        result.setUpdated(toDateTime(header.getChildTextTrim("updated")));
        result.setExported(toDateTime(header.getChildTextTrim("exported")));

        final Element permissionsElement = root.getChild("Terminal").getChild("vwdPermissions");
        if (permissionsElement == null) {
            return result;
        }

        final List<Element> permissions = permissionsElement.getChildren("vwdPermission");

        final Map<Profile.Aspect, Map<Integer, VwdProfile.SelectorItem>> aspects =
                new EnumMap<>(Profile.Aspect.class);

        for (Element permission : permissions) {
            final String subtype = permission.getAttributeValue("subtype");
            final VwdProfile.Aspect aspect = toAspect(subtype);
            if (aspect == null) {
                continue;
            }

            final Map<Integer, VwdProfile.SelectorItem> idToItem
                    = aspects.computeIfAbsent(aspect, k -> new HashMap<>());

            final Element selectorsItem = permission.getChild("Selectors");
            if (selectorsItem == null) {
                continue;
            }
            final List<Element> selectors = selectorsItem.getChildren("Sel");
            for (Element selector : selectors) {
                final int id = getSelectorId(selector);
                if (!isAllowed(aspect, id)) {
                    continue;
                }
                final List<Element> mqs = selector.getChildren("mq");
                int qlt = MAX_QLT;
                int mode = 2;
                for (Element mq : mqs) {
                    int mqQlt = Integer.parseInt(mq.getAttributeValue("qlt"));
                    int mqMode = Integer.parseInt(mq.getAttributeValue("mode"));
                    if (mqQlt < qlt) {
                        qlt = mqQlt;
                        mode = mqMode;
                    }
                    else if (mqQlt == qlt && mqMode < mode) {
                        mode = mqMode;
                    }
                }
                if (qlt == MAX_QLT) {
                    continue;
                }
                final PriceQuality pq = toPriceQuality(qlt);
                if (pq == null) {
                    continue;
                }

                if (idToItem.containsKey(id)) {
                    final VwdProfile.SelectorItem oldItem = idToItem.get(id);
                    if (oldItem.getPq().compareTo(pq) < 0) {
                        continue;
                    }
                    if (oldItem.getPq().compareTo(pq) == 0) {
                        if (oldItem.isPush()) {
                            continue;
                        }
                    }
                }
                idToItem.put(id, new VwdProfile.SelectorItem(id, pq,
                        mode == 1 && (pq == REALTIME || pq == DELAYED)));
            }
        }

        for (final Map.Entry<Profile.Aspect, Map<Integer, VwdProfile.SelectorItem>> entry : aspects.entrySet()) {
            final List<VwdProfile.SelectorItem> items = new ArrayList<>(entry.getValue().values());
            items.sort(null);
            result.setSelectors(items, entry.getKey());
        }

        return result;
    }

    private boolean isAllowed(Profile.Aspect aspect, int id) {
        if (this.allowedFieldsByAspect != null) {
            final Set<Integer> allowedFields = this.allowedFieldsByAspect.get(aspect);
            if (allowedFields != null && !allowedFields.contains(id)) {
                return false;
            }
        }
        if (this.forbiddenFieldsByAspect != null) {
            final Set<Integer> forbiddenFields = this.forbiddenFieldsByAspect.get(aspect);
            if (forbiddenFields != null && forbiddenFields.contains(id)) {
                return false;
            }
        }
        return true;
    }

    private int getSelectorId(Element selector) {
        final String s = selector.getAttributeValue("id");
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return Integer.parseInt(EntitlementsVwd.toNumericSelector(s));
        }
    }

    private VwdProfile.Aspect toAspect(String s) {
        if (s.startsWith("Kurs")) {
            return VwdProfile.Aspect.PRICE;
        }
        if ("Nachrichten".equals(s)) {
            return VwdProfile.Aspect.NEWS;
        }
        if ("Seite".equals(s)) {
            return VwdProfile.Aspect.PAGE;
        }
        if ("Produkt".equals(s)) {
            return VwdProfile.Aspect.PRODUCT;
        }
        if (isFunction(s)) {
            return VwdProfile.Aspect.FUNCTION;
        }
        if (isStaticData(s)) {
            return VwdProfile.Aspect.STATIC;
        }
        // other aspects are of no interest to us:
        return null;
    }

    private boolean isFunction(String s) {
        return "Funktion".equals(s) || "Sonstige".equals(s) || "Index Constituents".equals(s);
    }

    private boolean isStaticData(String s) {
        return "Wm Data".equals(s);
    }

    private PriceQuality toPriceQuality(int n) {
        switch (n) {
            case 1:
                return REALTIME;
            case 2:
                return DELAYED;
            case 3:
                return PriceQuality.END_OF_DAY;
            default:
                this.logger.warn("<toPriceQuality> unknown: " + n);
                return null;
        }
    }

    private VwdProfile.State getState(final Element e) {
        Element status = e.getChild("status");
        if (status == null) {
            status = e.getChild("Status");
        }
        if (status == null) {
            return VwdProfile.State.UNKNOWN;
        }

        String idStr = status.getAttributeValue("id");
        if (StringUtils.hasText(idStr)) {
            return getState(Integer.parseInt(idStr));
        }

        this.logger.warn("<getState> w/o status[@id]");
        return getState(e.getTextTrim());
    }

    private VwdProfile.State getState(int id) {
        switch (id) {
            case 1:
                return VwdProfile.State.ACTIVE;
            case 0:
                return VwdProfile.State.INACTIVE;
            default:
                if (id > 0) {
                    this.logger.warn("<getState> unknown non-error status " + id);
                }
                return VwdProfile.State.UNKNOWN;
        }
    }

    private VwdProfile.State getState(final String text) {
        if (text == null || text.startsWith("unknown")) {
            return VwdProfile.State.UNKNOWN;
        }
        try {
            return VwdProfile.State.valueOf(text.toUpperCase());
        }
        catch (IllegalArgumentException iae) {
            this.logger.warn("<getState> unknown state: '" + text + "'");
            return VwdProfile.State.UNKNOWN;
        }
    }

    private DateTime toDateTime(String s) {
        return ISODateTimeFormat.dateTimeParser().parseDateTime(s);
    }
}
