/*
 * AbstractImgCommand.java
 *
 * Created on 28.08.2006 16:36:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BaseImgCommand {
    private static final String VALUE_SEPARATOR = ";";

    private String chartName;

    private String locale = null;

    private String dateLocale = null;

    /**
     * If the chart cannot be drawn and errorLayout is null, a response with this code will be sent
     */
    private int errorCode = HttpServletResponse.SC_NOT_FOUND;

    /**
     * If the chart cannot be drawn and this is not null, an attempt will be made to
     * render this layout for a given error model
     */
    private String errorLayout;

    private int height;

    private String layout;

    private String numberLocale = null;

    /**
     * key=value pairs separated by semicolons that will be copied into the chart's model.
     */
    private String properties;

    private String style;

    private int width;

    private HttpServletRequest request;

    private boolean withCredentials = false;

    /**
     * Absolute url prefix, only used for blocks that return a chart url
     */
    private String baseUrl;

    public BaseImgCommand() {
    }

    protected BaseImgCommand(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public StringBuilder appendParameters(StringBuilder sb) {
        if (this.width > 0) {
            sb.append("width=").append(this.width);
        }
        if (this.height > 0) {
            sb.append("&height=").append(this.height);
        }
        appendParameter(sb, this.style, "style");
        appendParameter(sb, this.numberLocale, "numberLocale");
        appendParameter(sb, this.dateLocale, "dateLocale");
        appendParameter(sb, resolveLocale(), "locale");
        if (this.withCredentials) {
            final MoleculeRequest mr = (MoleculeRequest) this.request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);
            final String encodedCredentials = ProfileUtil.encodeCredential(mr.getAuthentication(), mr.getAuthenticationType());
            try {
                appendParameter(sb, URLEncoder.encode(encodedCredentials, "UTF-8"), ProfileResolver.CREDENTIAL_KEY);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb;
    }

    private String resolveLocale() {
        if (StringUtils.hasText(this.locale)) {
            return this.locale;
        }
        return RequestContextHolder.getRequestContext().getLocale().toString();
    }

    @MmInternal
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @MmInternal
    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    /**
     * Locale used to render times/dates (if the chart has not been configured with
     * that locale, the chart's default locale will be used). Overrides <tt>locale</tt>
     * @sample en
     */
    public String getDateLocale() {
        return dateLocale;
    }

    public void setDateLocale(String dateLocale) {
        this.dateLocale = dateLocale;
    }

    @MmInternal
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @MmInternal
    public String getErrorLayout() {
        return errorLayout;
    }

    public void setErrorLayout(String errorLayout) {
        this.errorLayout = errorLayout;
    }

    /**
     * @return height in px
     */
    @Range(min = 0, max = 1200)
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Request a specific layout (e.g., size, w/o volume, etc);
     * whether this option is available and which values are allowed
     * depends on the specific chart, contact us for details.
     */
    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    /**
     * @return locale used to render times/dates/numbers/prices
     * (if the chart has no configuration for that locale, its default locale will be used).
     * @sample de
     */
    public String getLocale() {
        return this.locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Locale used to render numbers/prices
     * (if the chart has no configuration for that locale, its default locale will be used).
     * Overrides <tt>locale</tt>.
     * @sample de
     */
    public String getNumberLocale() {
        return numberLocale;
    }

    public void setNumberLocale(String numberLocale) {
        this.numberLocale = numberLocale;
    }

    @MmInternal
    public String getProperties() {
        return this.properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * Request a specific style (e.g., colors, fonts);
     * whether this option is available and which values are allowed
     * depends on the specific chart, contact us for details.
     */
    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @MmInternal
    public boolean isWithCredentials() {
        return withCredentials;
    }

    public void setWithCredentials(boolean withCredentials) {
        this.withCredentials = withCredentials;
    }

    /**
     * @return width in px
     */
    @Range(min = 0, max = 1600)
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setChartlayout(String chartlayout) {
        setLayout(chartlayout);
    }

    @MmInternal
    public HttpServletRequest getRequest() {
        return request;
    }

    public Map getContextMap() {
        if (this.request == null) {
            return Collections.emptyMap();
        }
        final Map result = (Map) this.request.getAttribute(Zone.ATOM_CONTEXT_ATTRIBUTE);
        return (result != null) ? result : Collections.emptyMap();
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    protected StringBuilder appendParameter(StringBuilder sb, boolean value, boolean defaultValue,
                                            String name) {
        if (value != defaultValue) {
            return appendParameter(sb, Boolean.valueOf(value).toString(), name);
        }
        return sb;
    }

    protected StringBuilder appendParameter(StringBuilder sb, String value, String name) {
        if (StringUtils.hasText(value)) {
            sb.append("&").append(name).append("=").append(value);
        }
        return sb;
    }

    protected StringBuilder appendParameter(StringBuilder sb, Enum value, String name) {
        if (value != null) {
            sb.append("&").append(name).append("=").append(value.name());
        }
        return sb;
    }

    protected StringBuilder appendParameter(StringBuilder sb, Period value, String name) {
        if (value != null) {
            sb.append("&").append(name).append("=").append(ISOPeriodFormat.standard().print(value));
        }
        return sb;
    }

    protected StringBuilder appendParameters(StringBuilder sb, String[] values, String name) {
        if (values != null && values.length > 0) {
            sb.append("&").append(name).append("=").append(values[0]);
            for (int i = 1; i < values.length; i++) {
                sb.append(VALUE_SEPARATOR).append(values[i]);
            }
        }
        return sb;
    }

    protected String[] separate(String[] values) {
        if (isUndefined(values)) { // ignore null or an array in which every value has length 0
            return null;
        }
        return (values.length == 1) ? values[0].split(VALUE_SEPARATOR) : values;
    }

    private boolean isUndefined(String[] values) {
        if (values != null) {
            for (String value : values) {
                if (StringUtils.hasLength(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void validate(BindException bindException) {
        if (this.style == null) {
            bindException.reject("validator.notNull", "style is null");
        }
        if (this.layout == null) {
            bindException.reject("validator.notNull", "layout is null");
        }
    }
}
