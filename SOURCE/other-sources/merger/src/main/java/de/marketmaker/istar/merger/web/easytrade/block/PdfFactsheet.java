/*
 * ImgChartAnalyse.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 *         This block returns a request to generate pdf portraits.
 *         Must have parameters are zone and symbol.
 *         Optionally, it is possible to filter the TopProducts for certificates and stocks.
 *         To do so, specifiy the TopProductType(s) {@link de.marketmaker.istar.merger.web.easytrade.block.MscTopProductsCommandBuilder.ProductType}
 *         you want to include.
 *         If no TopProductType is given, all types will be included.
 *
 *         <block key="PDF_Factsheet">
 *         <parameter key="symbol" value="710000"/>
 *         <parameter key="zone" value="web"/>
 *         <parameter key="parameter" value="topProductTypes=BONUS"/>
 *         <parameter key="parameter" value="topProductTypes=DISCOUNT"/>
 *         </block>
 */
public class PdfFactsheet extends EasytradeCommandController {
    public static class Command extends DefaultSymbolCommand {
        private String zone;

        private String layout;

        private String[] parameter;

        @NotNull
        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        /**
         * Optional parameter to specify a pdf layout to be used in zones in need of different layouts.
         * @return name of requested layout
         */
        public String getLayout() {
            return layout;
        }

        public void setLayout(String layout) {
            this.layout = layout;
        }

        public String[] getParameter() {
            return parameter;
        }

        public void setParameter(String[] parameter) {
            this.parameter = parameter;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private String template = "pdffactsheet";


    public PdfFactsheet() {
        super(Command.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("request", getRequest(request, cmd.getZone(), cmd.getLayout(), quote, cmd.getParameter()));
        return new ModelAndView(this.template, model);
    }

    private String getRequest(HttpServletRequest request, String zone, String layout, Quote quote,
            String[] parameter) {
        final MoleculeRequest mr =
                (MoleculeRequest) request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME);

        final String encodedCredentials = ProfileUtil.encodeCredential(mr.getAuthentication(), mr.getAuthenticationType());

        try {
            final StringBuilder sb = new StringBuilder("/" + zone + "/pdf/" + getControllerName(request, layout, quote.getInstrument().getInstrumentType())
                    + "?symbol=" + EasytradeInstrumentProvider.qidSymbol(quote.getId())
                    + (StringUtils.hasText(layout) ? "&layout=" + URLEncoder.encode(layout, "UTF-8") : "")
                    + "&credential=" + URLEncoder.encode(encodedCredentials, "UTF-8"));
            final String showAlternatives = getShowAlternatives(quote.getInstrument().getInstrumentType());
            if (StringUtils.hasText(showAlternatives)) {
                sb.append("&").append(showAlternatives);
            }
            if (parameter != null) {
                for (final String p : parameter) {
                    final String[] keyvalue = p.split("=");
                    if (keyvalue.length != 2) {
                        continue;
                    }
                    sb.append("&").append(URLEncoder.encode(keyvalue[0], "UTF-8"))
                            .append("=").append(URLEncoder.encode(keyvalue[1], "UTF-8"));
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getControllerName(HttpServletRequest request, String layout,
            InstrumentTypeEnum type) {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        final Map<String, Object> map = z.getContextMap("PDF_Factsheet");
        if (!map.isEmpty()) {
            final String mapping = (String) map.get("layoutmapping." + layout);
            if (StringUtils.hasText(mapping)) {
                return mapping;
            }

            final String defaultMapping = (String) map.get("layoutmapping.DEFAULT");
            if (StringUtils.hasText(defaultMapping)) {
                return defaultMapping;
            }
        }

        // proceed with default processing
        switch (type) {
            case BND:
            case GNS:
                return "bondportrait.pdf";
            case CER:
                return "certificateportrait.pdf";
            case CUR:
                return "currencyportrait.pdf";
            case FND:
                return "fundportrait.pdf";
            case IND:
                return "indexportrait.pdf";
            case STK:
                return "stockportrait.pdf";
            case WNT:
                return "warrantportrait.pdf";
            default:
                return "simplesecurityportrait.pdf";
        }
    }

    private String getShowAlternatives(InstrumentTypeEnum type) {
        final String showAlt = "showAlternatives=true";
        switch (type) {
            case CER:
                return showAlt;
            case STK:
                return showAlt + "&topIssuername=" + getTopProductsIssuerName();
            default:
                return "";
        }
    }

    private String getTopProductsIssuerName() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final String dz = "DZ+BANK";
        return profile.isAllowed(Selector.DZ_BANK_USER) ? dz
                : profile.isAllowed(Selector.WGZ_BANK_USER) ? "WGZ+BANK" : dz;
    }
}
