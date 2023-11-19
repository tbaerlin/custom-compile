package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.Map;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * @author Ulrich Maurer
 *         Date: 16.10.12
 */
public class CssUtil {
    private static Style style = null;
    private static final Map<String, Boolean> CACHE = new HashMap<>();
    private static String transformStyle;

    /**
     * A map of hyphenated style properties to their camelCase equivalents.
     *
     * The set of style property names is limited, and common ones are reused
     * frequently, so caching saves us from converting every style property name
     * from hyphenated to camelCase form.
     *
     * Use a {@link JavaScriptObject} to avoid the dynamic casts associated with
     * the emulated version of {@link java.util.Map}.
     */
    private static JavaScriptObject hyphenatedMap;

    /**
     * Regex to match a word in a hyphenated phrase. A word starts with an a
     * hyphen or a letter, followed by zero or more characters letters. For
     * example, in the phrase background-url, the pattern matches "background" and
     * "-url".
     */
    private static RegExp maybeHyphenatedWord;

    private static Style getStyle() {
        if (style == null) {
            style = DOM.createDiv().getStyle();
        }
        return style;
    }

    public static boolean supportsBorderRadius() {
        return supportsStyle("borderRadius");
    }

    public static boolean supportsBoxShadow() {
        return supportsStyle("boxShadow");
    }

    public static boolean supportsTransition() {
        return supportsStyle("transition");
    }

    public static boolean supportsStyle(String styleName) {
        Boolean supported = CACHE.get(styleName);
        if (supported == null) {
            supported = supportsStyle(getStyle(), styleName);
            CACHE.put(styleName, supported);
        }
        return supported;
    }

    private static native boolean supportsStyle(Style style, String styleName) /*-{
        return typeof style[styleName] == 'string';
    }-*/;

    private static native String _getComputedPropertyValue(Element el, String prop)  /*-{
        var computedStyle;
        if ($doc.defaultView && $doc.defaultView.getComputedStyle) { // standard (includes ie9)
            computedStyle = $doc.defaultView.getComputedStyle(el, null)[prop];
        }
        else if (el.currentStyle) { // IE older
            computedStyle = el.currentStyle[prop];
        }
        else { // inline style
            computedStyle = el.style[prop];
        }
        return computedStyle;
    }-*/;

    public static String getComputedPropertyValue(Element el, String prop) {
        return _getComputedPropertyValue(el, toCamelCaseForm(prop));
    }

    public static float getComputedPropertyPx(Element elt, String propertyName) {
        final String value = getComputedPropertyValue(elt, propertyName);
        if (value == null || !value.endsWith("px")) {
            return 0;
        }
        return Float.parseFloat(value.substring(0, value.length() - 2));
    }

    /**
     * Convert a hyphenated or camelCase string to a camelCase string.
     *
     * copied from com/google/gwt/dom/builder/client/DomStylesBuilder.java
     *
     * @param name the hyphenated or camelCase string to convert
     * @return the hyphenated string
     */
    private static String toCamelCaseForm(String name) {
        // Static initializers.
        if (hyphenatedMap == null) {
            hyphenatedMap = JavaScriptObject.createObject();
            maybeHyphenatedWord = RegExp.compile("([-]?)([a-z])([a-z0-9]*)", "g");
        }
        // Early exit if already in camelCase form.
        if (!name.contains("-")) {
            return name;
        }
        // Check for the name in the cache.
        String camelCase = getCamelCaseName(hyphenatedMap, name);
        // Convert the name to camelCase format if not in the cache.
        if (camelCase == null) {
            /*
             * Strip of any leading hyphens, which are used in browser specified style
             * properties such as "-webkit-border-radius". In this case, the first
             * word "webkit" should remain lowercase.
             */
            if (name.startsWith("-") && name.length() > 1) {
                name = name.substring(1);
            }
            camelCase = "";
            MatchResult matches;
            while ((matches = maybeHyphenatedWord.exec(name)) != null) {
                String word = matches.getGroup(0);
                if (!word.startsWith("-")) {
                    // The word is not hyphenated. Probably the first word.
                    camelCase += word;
                } else {
                    // Remove hyphen and uppercase next letter.
                    camelCase += matches.getGroup(2).toUpperCase();
                    if (matches.getGroupCount() > 2) {
                        camelCase += matches.getGroup(3);
                    }
                }
            }
            putCamelCaseName(hyphenatedMap, name, camelCase);
        }
        return camelCase;
    }

    /**
     * Get the camelCase for a style name from a map.
     *
     * copied from com/google/gwt/dom/builder/client/DomStylesBuilder.java
     *
     * @param name the user specified style name
     * @return the camelCase name, or null if not set
     */
    private static native String getCamelCaseName(JavaScriptObject map, String name) /*-{
        return map[name] || null;
    }-*/;

    /**
     * Save the camelCase form of style name to a map.
     *
     * copied from com/google/gwt/dom/builder/client/DomStylesBuilder.java
     *
     * @param name the user specified style name
     * @param camelCase the camelCase name
     */
    private static native void putCamelCaseName(JavaScriptObject map, String name, String camelCase) /*-{
        map[name] = camelCase;
    }-*/;

    public static String getTransformStyle() {
        if (transformStyle == null) {
            transformStyle = _getTransformStyle(getStyle());
        }
        return transformStyle;
    }

    private static native String _getTransformStyle(Style style) /*-{
        var prefixes = ['ms', 'webkit'];
        var propName = "transform"; // $NON-NLS$
        var prefixed = null;

        if (typeof style[propName] == 'string') return propName;
        propName = "Transform"; // $NON-NLS$
        for (var i=0, l=prefixes.length; i<l; i++) {
            prefixed = prefixes[i] + propName;
            if (typeof style[prefixed] == 'string') return prefixed;
        }
        return null;
    }-*/;


    public static void setTransformOrigin(Style style, String origin) {
        style.setProperty(getTransformStyle() + "Origin", origin);
    }

    public static void setRotate(Style style, float rotate) {
        style.setProperty(getTransformStyle(), "rotate(" + rotate + "deg)");
    }

    public static void setScale(Style style, float scale) {
        style.setProperty(getTransformStyle(), "scale(" + scale + ")");
        setTransformOrigin(style, "top left");
        final float sizePercent = 100 / scale;
        style.setWidth(sizePercent, PCT);
        style.setHeight(sizePercent, PCT);
    }

    public static void resetScale(Style style) {
        style.clearProperty(getTransformStyle());
        style.clearProperty(getTransformStyle() + "Origin");
        style.clearWidth();
        style.clearHeight();
    }

    public static boolean hasStyle(Widget widget, String... searchedStyles) {
        return hasStyle(widget.getStyleName(), searchedStyles);
    }

    public static boolean hasStyle(Element element, String... searchedStyles) {
        return hasStyle(element.getClassName(), searchedStyles);
    }

    public static boolean hasStyle(String widgetStyles, String... searchedStyles) {
        if (!StringUtility.hasText(widgetStyles)) {
            return false;
        }
        final String[] splittedWs = widgetStyles.split(" ");
        for (String sS : searchedStyles) {
            for (String wS : splittedWs) {
                if (wS.equals(sS)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getStyleWithPrefix(String widgetStyles, String prefix) {
        if (!StringUtility.hasText(widgetStyles)) {
            return null;
        }
        final String[] splittedWs = widgetStyles.split(" ");
        for (String wS : splittedWs) {
            if (wS.startsWith(prefix)) {
                return wS;
            }
        }
        return null;
    }

    public static String getStyleValue(String widgetStyles, String prefix) {
        final String style = getStyleWithPrefix(widgetStyles, prefix + "-");
        return style == null ? null : style.substring(prefix.length() + 1);
    }

    public static Float getStyleValueFloat(String widgetStyles, String prefix) {
        final String value = getStyleValue(widgetStyles, prefix);
        try {
            return value == null ? null : Float.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer getStyleValueInt(String widgetStyles, String prefix) {
        final String value = getStyleValue(widgetStyles, prefix);
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void setPrefixedStyle(Widget widget, String styles, String prefix) {
        final String value = getStyleValue(styles, prefix);
        if (value != null) {
            widget.getElement().getStyle().setProperty(prefix, value);
        }
    }

    public static void setPrefixedStylePx(Widget widget, String styles, String prefix) {
        final String value = getStyleValue(styles, prefix);
        if (value != null) {
            widget.getElement().getStyle().setProperty(prefix, value.matches("[0-9]+") ? (value + "px") : value);
        }
    }

    public static void setPrefixedStyleColor(Widget widget, String styles, String prefix) {
        final String value = getStyleValue(styles, prefix);
        if (value != null) {
            final String color = value.matches("([0-9a-f]{3}){1,2}") ? ("#" + value) : value; // $NON-NLS$
            widget.getElement().getStyle().setProperty(prefix, color);
        }
    }
}
