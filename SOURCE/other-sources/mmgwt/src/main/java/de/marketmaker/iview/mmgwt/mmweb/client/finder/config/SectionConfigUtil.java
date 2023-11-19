package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElement;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserWidget;

/**
 * Created on 20.10.11 10:06
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class SectionConfigUtil {

    public static final String SEPARATOR = ","; // $NON-NLS$
    public static final String EMPTY_CONF ="EMPTY"; // $NON-NLS$


    public static String getSectionConfigAsString(String sectionId) {
        final String tmp = SessionData.INSTANCE.getUser().getAppConfig().getProperty(getPropertyName(sectionId));
        if (!StringUtil.hasText(tmp)) {
            return null;
        }
        else if (EMPTY_CONF.equals(tmp)) {
            return "";
        }
        return tmp;
    }

    public static String[] getSectionConf(String sectionId) {
        final String confStr = getSectionConfigAsString(sectionId);
        if (confStr == null) {
            return null;
        } else if ("".equals(confStr)) {
            return new String[]{};
        }
        //handle duplicates that can occur because of a former bug
        final LinkedHashSet<String> sectionStrings = new LinkedHashSet<String>();
        sectionStrings.addAll(Arrays.asList(confStr.split(SectionConfigUtil.SEPARATOR)));

        return sectionStrings.toArray(new String[sectionStrings.size()]);
    }


    public static List<String> getElementIds(List<FinderFormElement> elements) {
        return getElementIds(elements, false);
    }

    public static List<String> getElementIds(List<FinderFormElement> elements, boolean withInactiveElements) {
        final List<String> elementIds = new ArrayList<String>();
        for (FinderFormElement element : elements) {
            if (element.isActive() || withInactiveElements) {
                elementIds.add(element.getId());
            }
        }
        return elementIds;
    }

    public static List<String> getElementIds(ItemChooserWidget icw) {
        final List<String> elementIds = new ArrayList<String>();
        for (int i = 0; i < icw.getSelectedRowsCount(); i++) {
            elementIds.add(icw.getColumnValue(i));
        }
        return elementIds;
    }

    public static String saveSectionConfig(String sectionId, List<String> elementIds) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, elementsSize = elementIds.size(); i < elementsSize; i++) {
            if (i != 0) {
                sb.append(SEPARATOR);
            }
            final String id = elementIds.get(i);
            if (id.contains(SEPARATOR)) {
                throw new IllegalArgumentException("Cannot save configuration. Identifier contains separator char: " + id); // $NON-NLS$
            }
            sb.append(id);
        }
        final String conf = sb.toString();
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(getPropertyName(sectionId),
                conf.isEmpty() ? EMPTY_CONF : conf);
        return getSectionConfigAsString(sectionId);
    }

    public static String getPropertyName(String sectionId) {
        return AppConfig.LIVE_FINDER_SECTION_PREFIX + sectionId;
    }
}