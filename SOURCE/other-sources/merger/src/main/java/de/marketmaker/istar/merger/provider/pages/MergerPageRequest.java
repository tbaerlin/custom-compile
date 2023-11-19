/*
 * PageRequest.java
 *
 * Created on 22.02.2008 17:18:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;

/**
 * Request a pre-formatted page, usually with links to other pages and containing feed data fields.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MergerPageRequest {

    public enum RendererType {
        PRE {
            PdlRenderer createRenderer(Properties p) {
                return new HtmlPrePdlRenderer(p);
            }
        },
        FOP {
            PdlRenderer createRenderer(Properties p) {
                return new FopPrePdlRenderer(p);
            }
        },
        TABLE {
            PdlRenderer createRenderer(Properties p) {
                return new HtmlTablePdlRenderer(p);
            }
        };

        abstract PdlRenderer createRenderer(Properties p);
    }

    private static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    private String pageId;

    private String[] renderingProperties = new String[0];

    private String currency;

    private Locale locale = DEFAULT_LOCALE;

    private boolean preferGermanText = false;

    private RendererType rendererType = RendererType.PRE;

    /**
     * @return 3-letter ISO code for a currency, all prices displayed on the
     * page will be converted to this currency
     */
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return id of requested page
     * @sample 2501
     */
    @NotNull
    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /**
     * Properties that can be used to adapt the rendered output, format for each property is
     * <tt>key=value</tt>. The different RendererTypes support different properties. Customers
     * are not expected to deal with properties themselves.
     */
    @MmInternal
    public String[] getRenderingProperties() {
        return renderingProperties;
    }

    public void setRenderingProperties(String[] renderingProperties) {
        this.renderingProperties = renderingProperties;
    }

    /**
     * How (vwd) pages should be formatted, default is <tt>PRE</tt>
     * <dl>
     *     <dt><tt>PRE</tt></dt>
     *     <dd>renders page in html <tt>pre</tt>-element</dd>
     *     <dt><tt>TABLE</tt></dt>
     *     <dd>renders page in html <tt>table</tt>-element</dd>
     *     <dd></dd>
     * </dl>
     */
    public RendererType getRendererType() {
        return rendererType;
    }

    public void setRendererType(RendererType rendererType) {
        this.rendererType = rendererType;
    }

    /**
     * @return locale that will be used to render prices, default is <tt>de</tt> (the number 3.5
     * will be rendered as 3,5 with locale <tt>de</tt> and as 3.5 with locale <tt>en</tt>
     */
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return if set to true and a german version of a (vwd) page is available,
     * return that instead of the default english version
     */
    public boolean isPreferGermanText() {
        final List<Locale> locales = RequestContextHolder.getRequestContext().getLocales();
        return this.preferGermanText || locales == null || Language.hasFirstLocaleDeLanguage(locales);
    }

    public void setPreferGermanText(boolean preferGermanText) {
        this.preferGermanText = preferGermanText;
    }
}
