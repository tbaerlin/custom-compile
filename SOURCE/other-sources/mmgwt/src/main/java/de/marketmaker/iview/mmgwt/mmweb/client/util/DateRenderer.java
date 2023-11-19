/*
 * DateRenderer.java
 *
 * Created on 05.06.2008 18:28:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

import java.util.Date;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class DateRenderer implements Renderer<String> {
    public static DateRenderer isoDateAndTime(String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String s) {
                return s;
            }
        };
    }

    // renders '2015-06-02T06:43:28.000Z', '2015-06-02T06:43:28Z', '2014-11-19T13:25:48.000+01:00' in
    // client specific timezone, GWT expects ms in the ISO String
    // this regex stuff is all about adding ms if needed... (and while we are on it, we add a timezone 'Z' if missing)
    public static DateRenderer localTimezoneDateTime(String defaultValue) {
        // group 1: any datetime look-a-like String  e.g. '2015-06-02T06:43:28'
        // group 2: any millisecs with leading '.' (optional group)
        // group 3: Z or any timezone offset e.g. +01:00 (optional group)
        final RegExp regex = RegExp.compile("^([^\\.]+?)(.[0-9]{3})?(Z|[\\+|\\-][0-9]{2}:[0-9]{2})?$");  // $NON-NLS$
        final DateTimeFormat inputFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);
        return new DateRenderer(defaultValue) {
            protected String formatValue(String isoString) {
                if (!StringUtil.hasText(isoString)) {
                    return defaultValue;
                }
                final MatchResult m = regex.exec(isoString);
                if (!StringUtil.hasText(m.getGroup(1))) {
                    return defaultValue;
                }
                final Date date = inputFormat.parse(
                        m.getGroup(1)
                        + (StringUtil.hasText(m.getGroup(2))?m.getGroup(2):".000") // $NON-NLS$
                        + (StringUtil.hasText(m.getGroup(3))?m.getGroup(3):"Z")); // $NON-NLS$
                return Formatter.LF.formatDate(date) + "&nbsp;" + Formatter.formatTime(date); // $NON-NLS$
            }
        };
    }

    public static DateRenderer date(String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String s) {
                return Formatter.LF.formatDate(s);
            }
        };
    }

    public static DateRenderer fullTime(String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String s) {
                if (s == null) {
                    return defaultValue;
                }
                final String time = Formatter.formatTime(s);
                return "00:00:00".equals(time) ? this.defaultValue : time; // $NON-NLS$
            }
        };
    }

    public static DateRenderer compactDateTime(String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String s) {
                if (s == null) {
                    return defaultValue;
                }
                final MmJsDate jsDate = GwtDateParser.getMmJsDate(s);

                final String time = JsDateFormatter.formatHhmm(jsDate);
                final String date = JsDateFormatter.formatDateShort(jsDate);
                return jsDate.isMidnight() ? date : (date + " " + time); // $NON-NLS$
            }
        };
    }

    public static DateRenderer dateAndTime(String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String date) {
                if (date == null) {
                    return defaultValue;
                }
                return Formatter.LF.formatDate(date) + "&nbsp;" + Formatter.formatTime(date); // $NON-NLS$
            }
        };
    }

    public static DateRenderer dateOrTime(final boolean compact, String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String s) {
                if (s == null) {
                    return defaultValue;
                }
                final MmJsDate jsDate = GwtDateParser.getMmJsDate(s);

                if (jsDate.isToday() && !jsDate.isMidnight()) {
                    return compact
                            ? JsDateFormatter.formatHhmm(jsDate)
                            : JsDateFormatter.formatHhmmss(jsDate);
                }
                else {
                    return JsDateFormatter.formatDateShort(jsDate);
                }
            }
        };
    }

    public static DateRenderer compactTime(String defaultValue) {
        return new DateRenderer(defaultValue) {
            protected String formatValue(String s) {
                if (s == null) {
                    return defaultValue;
                }
                final String time = Formatter.formatTimeHhmm(s);
                return "00:00".equals(time) ? this.defaultValue : time; // $NON-NLS-0$
            }
        };
    }

    protected String defaultValue;

    private DateRenderer(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String render(String s) {
        return s == null ? this.defaultValue : formatValue(s);
    }

    protected abstract String formatValue(String s);
}
