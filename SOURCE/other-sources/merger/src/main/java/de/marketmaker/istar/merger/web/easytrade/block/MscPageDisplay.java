/*
 * NwsNachricht.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.provider.pages.MergerPageRequest;
import de.marketmaker.istar.merger.provider.pages.MergerPageResponse;
import de.marketmaker.istar.merger.provider.pages.PageProvider;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;

/**
 * Returns a formatted vwd feed page.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscPageDisplay extends EasytradeCommandController {
    private PageProvider pageProvider;

    public MscPageDisplay() {
        super(MergerPageRequest.class);
    }

    public void setPageProvider(PageProvider pageProvider) {
        this.pageProvider = pageProvider;
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder binder) throws Exception {
        // no need to call super.initBinder
        binder.registerCustomEditor(MergerPageRequest.RendererType.class,
                new EnumEditor(MergerPageRequest.RendererType.class));
        binder.registerCustomEditor(Locale.class, new LocaleEditor());
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final MergerPageRequest c = (MergerPageRequest) o;
        final MergerPageResponse page = this.pageProvider.getPage(c);
        return new ModelAndView("mscpagedisplay", "page", page);
    }
}