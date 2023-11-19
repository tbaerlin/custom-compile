/*
 * IconImage.java
 *
 * Created on 28.10.2009 14:51:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;

import java.util.HashMap;

import static com.google.gwt.dom.client.Style.VerticalAlign.MIDDLE;

/**
 * @author umaurer
 */
public class IconImage {
    private static final HashMap<String, String> FILE_TYPE_TO_ICON_NAME_MAP = new HashMap<>();

    static {
        initFileTypeMap();
    }

    public static void setIconStyle(HasIcon hasIcon, String iconClass) {
        if (iconClass == null) {
            return;
        }
        final ImageSpec imageSpec = IconImageMapping.I.getImageSpec(iconClass);
        if (imageSpec != null) {
            hasIcon.setIcon(imageSpec.asPrototype());
        }
        else {
            hasIcon.setIconStyle(iconClass);
            Firebug.warn("IconImage - unknown iconClass: " + iconClass);
        }
    }

    public static AbstractImagePrototype get(String iconClass) {
        return IconImageMapping.I.getImageSpec(iconClass).asPrototype();
    }

    public static String getHtml(String iconClass, String tooltip) {
        final Image image = get(iconClass).createImage();
        image.getElement().getStyle().setVerticalAlign(MIDDLE);
        if (tooltip != null) {
            Tooltip.addQtip(image, tooltip);
        }
        return new SimplePanel(image).getElement().getInnerHTML();
    }
    
    public static ImageSpec getImageResource(String iconClass) {
        return IconImageMapping.I.getImageSpec(iconClass);
    }

    public static boolean isDefined(String iconClass) {
        return StringUtility.hasText(iconClass) && IconImageMapping.I.hasImageSpec(iconClass);
    }

    public static String getUrl(String iconClass) {
        return getImageResource(iconClass).getUrl();
    }

    public static IconImageIcon getIcon(String iconClass) {
        final IconImageIcon icon = new IconImageIcon(iconClass);
        if (IconImageMapping.I.hasImageSpec(iconClass + " disabled")) {
            icon.withDisabledIcon(iconClass + " disabled");
        }
        if (IconImageMapping.I.hasImageSpec(iconClass + " hover")) {
            icon.withHoverIcon(iconClass + " hover");
        }
        if (IconImageMapping.I.hasImageSpec(iconClass + " click")) {
            icon.withMouseDownIcon(iconClass + " click");
        }
        return icon;
    }

    public static void setIcon(IconImageIcon icon, String iconClass) {
        icon.withIcon(iconClass);
        if (IconImageMapping.I.hasImageSpec(iconClass + " disabled")) {
            icon.withDisabledIcon(iconClass + " disabled");
        }
        if (IconImageMapping.I.hasImageSpec(iconClass + " hover")) {
            icon.withHoverIcon(iconClass + " hover");
        }
        if (IconImageMapping.I.hasImageSpec(iconClass + " click")) {
            icon.withMouseDownIcon(iconClass + " click");
        }
    }

    public static AbstractImagePrototype getImagePrototypeForFileType(String rawFileType) {
        if(StringUtility.hasText(rawFileType)) {
            try {
                return get("mm-fileType-" + getMappedFileType(rawFileType));
            } catch (IllegalArgumentException iae) {
                Firebug.warn("<IconImage.getImagePrototypeForFileType> Icon for file type " + rawFileType + " not supported");
            }
        }
        return get("mm-fileType-unknown");
    }

    /**
     * A mapping is only required for this file types that do not exactly match the name of the icon.
     * To add a new icon for a new file type, it is simply sufficient to add an icon with a name according to
     * the name pattern "mm-fileType-$FILETYPE".
     *
     * @see #getImagePrototypeForFileType(String)
     */
    private static void initFileTypeMap() {
        /* all spread sheet formats supported by MS Excel */
        addFileTypeMapping("xls", "xlsx", "xlsm", "xlsb", "xlam", "xltx", "xltm", "xla", "xlm", "xlw", "ods");

        /* all document formats supported by MS Word */
        addFileTypeMapping("doc", "docx", "docm", "dotx", "dotm", "dot", "rtf", "odt");

        /* all presentation formats supported by MS PowerPoint */
        addFileTypeMapping("ppt", "pptx", "pptm", "ppsx", "pps", "ppsm", "potx", "potm", "odp");

        /* image formats supported by MS Paint not: jpe and jfif are synonyms for jpeg; dib is a synonym for bmp */
        addFileTypeMapping("chart-image", "png", "jpg", "jpeg", "jpe", "jfif", "bmp", "dib", "tif", "tiff", "gif");
    }

    private static void addFileTypeMapping(String fileTypesMapToIconName, String... fileTypes) {
        for (String fileType : fileTypes) {
            FILE_TYPE_TO_ICON_NAME_MAP.put(fileType, fileTypesMapToIconName);
        }
    }

    private static String getMappedFileType(String rawFileType) {
        final String lowerCaseFileType = rawFileType.toLowerCase();
        final String mappedFileType = FILE_TYPE_TO_ICON_NAME_MAP.get(lowerCaseFileType);
        if(mappedFileType != null) {
            return mappedFileType;
        }
        return lowerCaseFileType;
    }
}
