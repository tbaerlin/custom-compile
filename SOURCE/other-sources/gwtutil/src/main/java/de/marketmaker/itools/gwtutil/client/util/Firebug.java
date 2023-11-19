package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.event.shared.UmbrellaException;

import java.util.Map;

/**
 * User: umaurer
 * Date: 11.06.13
 * Time: 11:49
 */
public class Firebug {
    /**
     * If the firefox firebug plugin is enabled, it adds a global "console" variable. Applications // $NON-NLS-0$
     * can send log messages to this object and the output will appear in firebug's console window.
     *
     * @param s String to write to the console
     * @see <a href="http://getfirebug.com/wiki/index.php/Console_API">Console</a>
     */
    public static native void log(String s)
        /*-{
            if (window != window.parent) {
                // for firebug lite where console is defined in parent frame:
                if(window.parent["console"]){ // $NON-NLS-0$
                    window.parent.console.log(s);
                    return;
                }
            }
            if (typeof console != 'undefined') {
                console.log(s);
            }
        }-*/;

    /**
     * IE does not provide the debug method. Therefore, one is created on demand.
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd565625(v=vs.85).aspx">Debugging Script with the Developer Tools</a>
     */
    public static native void debug(String s)
        /*-{
            if (window != window.parent) {
                // for firebug lite where console is defined in parent frame:
                if(window.parent["console"]){ // $NON-NLS-0$
                    //for IE
                    if(typeof window.parent.console.debug == 'undefined') {
                        window.parent.console.debug = window.parent.console.log;
                    }
                    window.parent.console.debug(s);
                    return;
                }
            }
            if (typeof console != 'undefined') {
                //for IE
                if(typeof console.debug == 'undefined') {
                    console.debug = console.log;
                }
                console.debug(s);
            }
        }-*/;

    public static native void info(String s)
        /*-{
            if (window != window.parent) {
                // for firebug lite where console is defined in parent frame:
                if(window.parent["console"]){ // $NON-NLS$
                    window.parent.console.info(s);
                    return;
                }
            }
            if (typeof console != 'undefined') {
                console.info(s);
            }
        }-*/;

    public static native void warn(String s)
        /*-{
            if (window != window.parent) {
                // for firebug lite where console is defined in parent frame:
                if(window.parent["console"]){ // $NON-NLS$
                    window.parent.console.warn(s);
                    return;
                }
            }
            if (typeof console != 'undefined') {
                console.warn(s);
            }
        }-*/;

    public static void warn(String s, Throwable t) {
        warn(toString(s, t));
    }

    public static native void error(String s)
        /*-{
            if (window != window.parent) {
                // for firebug lite where console is defined in parent frame:
                if(window.parent["console"]){ // $NON-NLS$
                    window.parent.console.error(s);
                    return;
                }
            }
            if (typeof console != 'undefined') {
                console.error(s);
            }
        }-*/;

    public static void error(String s, Throwable t) {
        error(toString(s, t));
    }

    public static native void groupStart(String groupName)
        /*-{
            if (typeof console != 'undefined' && console.groupCollapsed) {
                console.groupCollapsed(groupName);
            }
        }-*/;

    public static native void groupEnd()
        /*-{
            if (typeof console != 'undefined' && console.groupEnd) {
                console.groupEnd();
            }
        }-*/;

    public static void group(String groupName, String... sArray) {
        groupStart(groupName);
        for (String s : sArray) {
            log(s);
        }
        groupEnd();
    }

    public static void groupCollapsed(String groupName, String... sArray) {
        groupStart(groupName);
        for (String s : sArray) {
            log(s);
        }
        groupEnd();
    }

    /**
     * Starts a new JavaScript profiling session in your Browser.
     *
     * Works in Chrome and Firefox, but Firefox does not understand source maps in its profiler,
     * so as the only really usable Browser remains Chrome.
     * If your JavaScript is obfuscated, try to add the following line to your Dev*.gwt.xml file:
     * <code>&lt;set-configuration-property name="compiler.emulatedStack.recordFileNames" value="true"/&gt;</code>.
     * In Chrome you will not see the clear function or method names, but on mouse over, you will
     * see the class names and the line in the Java file that corresponds to the function/method.
     *
     * @param identifier identifies the profile in the browser's profile tab
     */
    public static native void profileStart(String identifier)
        /*-{
            if (typeof console != 'undefined' && console.profile) {
                console.profile(identifier);
            }
        }-*/;

    /**
     * Stops the JavaScript profiling session.
     */
    public static native void profileEnd()
        /*-{
            if (typeof console != 'undefined' && console.profileEnd) {
                console.profileEnd();
            }
        }-*/;

    public static String toString(Throwable throwable) {
        return toString(null, throwable);
    }

    public static String toString(String message, Throwable throwable) {
        if (throwable instanceof UmbrellaException && ((UmbrellaException)throwable).getCauses().size() == 1) {
            return toString(message, ((UmbrellaException)throwable).getCauses().iterator().next());
        }
        int k = 0;
        StringBuilder sb = new StringBuilder(200);
        if (message != null) {
            sb.append(message).append('\n');
        }
        while (throwable != null && k++ < 3) {
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            sb.append(throwable.toString()).append("\n"); // $NON-NLS$
            int i = 0;
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (++i < 40) {
                    sb.append("    at ").append(stackTraceElement).append("\n"); // $NON-NLS$
                }
                else {
                    sb.append("    ..."); // $NON-NLS$
                    break;
                }
            }
            throwable = throwable.getCause();
            if (throwable != null) {
                sb.append("Caused by: "); // $NON-NLS$
            }
        }
        return sb.toString();
    }

    public static void logAsGroup(String description, Map<?, ?> map) {
        final String[] s = new String[map.size()];
        int i = 0;
        for (Map.Entry entry : map.entrySet()) {
            s[i++] = entry.getKey().toString() + ": " + (entry.getValue() == null ? "null" : entry.getValue().toString());
        }
        Firebug.group(description, s);
    }
}
