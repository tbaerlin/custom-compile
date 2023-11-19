/*
 * AbstractImgChart.java
 *
 * Created on 28.08.2006 16:28:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.HttpException;
import static de.marketmaker.istar.merger.web.ZoneDispatcherServlet.CACHE_PARAMETER_NAME;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.istar.merger.web.view.binary.BinaryView;
import de.marketmaker.istar.chart.ChartEncodingConfig;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.ChartResult;
import de.marketmaker.istar.chart.ChartServer;
import de.marketmaker.istar.chart.ChartView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractImgChart extends EasytradeCommandController implements BeanNameAware {
    private static final DateTime EARLIEST_CHART_DATE
            = Constants.EARLIEST_CHART_DAY.toDateTimeAtStartOfDay();

    private static final String DEFAULT_CHART_ENCODING_FORMAT = "png";

    private ChartServer chartServer;

    private String formatName = DEFAULT_CHART_ENCODING_FORMAT;

    private Map<String, String> styleMappings;

    private Ehcache chartCache;

    protected AbstractImgChart(Class<? extends BaseImgCommand> commandClass) {
        super(commandClass);
    }

    protected void initApplicationContext() {
        super.initApplicationContext();
    }

    public void setStyleMappings(Map<String, String> styleMappings) {
        this.styleMappings = styleMappings;
    }

    public void setChartServer(ChartServer chartServer) {
        this.chartServer = chartServer;
    }

    protected ChartEncodingConfig getEncodingConfig() {
        return new ChartEncodingConfig(this.formatName);
    }

    public void setChartCache(Ehcache chartCache) {
        this.chartCache = chartCache;
    }

    public void setBeanName(String s) {
        final int p = s.lastIndexOf('.') + 1;
        if (p > 0) {
            this.formatName = s.substring(p);
        }
    }

    @Override
    protected void onBind(HttpServletRequest request, Object command) throws Exception {
        super.onBind(request, command);
        ((BaseImgCommand) command).setRequest(request);
    }

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object object,
            BindException bindException) throws Exception {
        super.onBindAndValidate(request, object, bindException);
        if (object instanceof BaseImgCommand) {
            ((BaseImgCommand) object).validate(bindException);
        }
    }

    protected final ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object object, BindException errors) throws Exception {

        applyCacheSeconds(response, getCacheSeconds());

        final BaseImgCommand cmd = (BaseImgCommand) object;
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<doHandle> " + cmd.appendParameters(new StringBuilder(100)));
        }
        final int cacheTimeSec = ServletRequestUtils.getIntParameter(request, CACHE_PARAMETER_NAME, -1);
        String cacheKey = null;

        if (this.chartCache != null && cacheTimeSec != -1) {
            cacheKey = getCacheKey(request, cmd);
            final Element e = this.chartCache.get(cacheKey);
            if (e != null) {
                return (ModelAndView) e.getObjectValue();
            }
        }

        ChartModelAndView mv = null;
        try {
            mv = createChartModelAndView(request, response, object, errors);
            if (mv == null) {
                errors.reject("empty.chartmodel", "no model/view to render chart");
            }
        } catch (MergerException me) {
            errors.reject(me.getCode(), me.getMessage());
        }

        addProperties(mv, object);
        // TODO: add params to mv.getView().getEncodingConfig()

        if ("request".equals(request.getParameter("dump"))) {
            return createDumpModel(request, response, errors, cmd, mv);
        }

        if (errors.hasErrors()) {
            return null;
        }

        final ModelAndView result = toModelAndView(mv);

        if (result.getModel().get(BinaryView.CONTENT_KEY) == null) {
            this.logger.warn("<handle> no chart for " + object);
        }

        if (cacheKey != null) {
            final Element e = new Element(cacheKey, result);
            e.setTimeToLive(cacheTimeSec);
            this.chartCache.put(e);
        }

        return result;
    }

    private ModelAndView createDumpModel(HttpServletRequest request, HttpServletResponse response,
            BindException errors, BaseImgCommand cmd, ChartModelAndView mv) throws Exception {
        if (errors.hasErrors()) {
            final ModelAndView mav = createErrorModel(request, response, cmd, errors);
            if (mav == null) {
                return null;
            }
            mv = new ChartModelAndView(new ChartView(cmd.getErrorLayout(), cmd.getStyle(),
                    getEncodingConfig()), mav.getModel());
        }
        final Map<String, Object> model = new HashMap<>();
        model.put(BinaryView.CONTENT_TYPE_KEY, "application/x-java-serialized-object");
        model.put(BinaryView.CONTENT_KEY, serialize(mv));
        return new ModelAndView(BinaryView.VIEW_NAME, model);
    }

    private String getCacheKey(HttpServletRequest request, BaseImgCommand cmd) {
        return appendRequest(request, cmd, appendProfile(new StringBuilder(100))).toString();
    }

    private StringBuilder appendRequest(HttpServletRequest request, BaseImgCommand cmd,
            StringBuilder sb) {
        sb.append(request.getRequestURI()).append("?");
        cmd.appendParameters(sb);
        return sb;
    }

    private StringBuilder appendProfile(StringBuilder sb) {
        return sb.append(System.identityHashCode(RequestContextHolder.getRequestContext().getProfile())).append("|");
    }

    private void addProperties(ChartModelAndView mv, Object object) {
        if (mv == null || !(object instanceof BaseImgCommand)) {
            return;
        }
        final BaseImgCommand bic = (BaseImgCommand) object;
        if (!StringUtils.hasText(bic.getProperties())) {
            return;
        }
        final String[] props = bic.getProperties().split(";");
        for (String prop : props) {
            String[] keyValue = prop.split("=");
            if (keyValue.length != 2) {
                continue;
            }
            mv.addObject(keyValue[0], keyValue[1]);
        }
    }

    protected ModelAndView toModelAndView(ChartModelAndView mv) {
        final Map<String, Object> model = new HashMap<>();
        final ChartResult chartResult = this.chartServer.render(mv);
        if (chartResult != null) {
            model.put(BinaryView.CONTENT_TYPE_KEY, chartResult.getMimeType());
            model.put(BinaryView.CONTENT_KEY, chartResult.getChart());
        }
        return new ModelAndView(BinaryView.VIEW_NAME, model);
    }

    protected ModelAndView createErrorModel(HttpServletRequest request,
            HttpServletResponse response, Object o, BindException errors) throws Exception {

        final DefaultMessageSourceResolvable resolvable = errors.getAllErrors().get(0);
        this.logger.warn("<createErrorModel> for " + toString(request)
                + ": " + resolvable.getCode() + "; " + resolvable.getDefaultMessage());

        final BaseImgCommand cmd = (BaseImgCommand) o;
        if (cmd.getErrorLayout() != null) {
            final ChartModelAndView mv = errorChartModelAndView(cmd);
            return toModelAndView(mv);
        }

        throw new HttpException(cmd.getErrorCode(),
                "Error " + resolvable.getCode() + ": " + resolvable.getDefaultMessage());
    }

    private String toString(HttpServletRequest request) {
        final String qs = request.getQueryString();
        return request.getRequestURI() + (qs != null ? ("?" + qs) : "");
    }

    private ChartModelAndView errorChartModelAndView(BaseImgCommand cmd) {
        return new ChartModelAndView(cmd.getErrorLayout(), cmd.getStyle(),
                Math.max(10, Math.min(1000, cmd.getWidth())),
                Math.max(10, Math.min(1000, cmd.getHeight())),
                getEncodingConfig());
    }

    private static byte[] serialize(ChartModelAndView mv) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mv);
        oos.close();
        return baos.toByteArray();
    }

    protected ChartModelAndView createChartModelAndView(BaseImgCommand cmd) {
        final ChartModelAndView result = new ChartModelAndView(createView(cmd));
        addLocales(cmd, result);
        addContextModel(cmd, result);
        return result;
    }

    private void addContextModel(BaseImgCommand cmd, ChartModelAndView result) {
        final Object model = cmd.getContextMap().get("model");
        if (model instanceof Map) {
            //noinspection unchecked
            result.getModel().putAll((Map<String, Object>) model);
        }
    }

    private void addLocales(BaseImgCommand cmd, ChartModelAndView result) {
        final String dl = getDefinedOrDefault(cmd.getDateLocale(), cmd.getLocale());
        if (StringUtils.hasText(dl)) {
            result.withDateFormatLocale(StringUtils.parseLocaleString(dl));
        }
        final String nl = getDefinedOrDefault(cmd.getNumberLocale(), cmd.getLocale());
        if (StringUtils.hasText(nl)) {
            result.withNumberFormatLocale(StringUtils.parseLocaleString(nl));
        }
    }

    private String getDefinedOrDefault(String s1, String s2) {
        return (StringUtils.hasText(s1)) ? s1 : s2;
    }

    private ChartView createView(BaseImgCommand cmd) {
        return new ChartView(cmd.getLayout(), resolveStyleName(cmd),
                cmd.getWidth(), cmd.getHeight(), getEncodingConfig());
    }

    protected String resolveStyleName(BaseImgCommand cmd) {
        if (this.styleMappings == null) {
            return cmd.getStyle();
        }
        final String mappedName = this.styleMappings.get(cmd.getStyle());
        return (mappedName != null) ? mappedName : cmd.getStyle();
    }

    protected abstract ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object o, BindException bindException) throws Exception;

    public static Interval getInterval(Period p) {
        return new Interval(getStart(p), new LocalDate().plusDays(1).toDateTimeAtStartOfDay());
    }

    private static DateTime getStart(Period p) {
        final boolean isIntraday = Period.days(1).equals(p);
        final DateTime start = new DateTime().plusDays(isIntraday ? 1 : 0).minus(p);
        return start.isBefore(EARLIEST_CHART_DATE)
                ? EARLIEST_CHART_DATE
                : start.withTimeAtStartOfDay();
    }
}
