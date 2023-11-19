/*
 * AbstractSpsAsyncLinkWidget.java
 *
 * Created on 16.03.2018 12:49
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.DefaultFocusKeyHandler;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public abstract class AbstractSpsAsyncLinkWidget extends SpsBoundWidget<HTML, SpsGroupProperty> implements SpsAfterPropertiesSetHandler, HasFocusHandlers, HasBlurHandlers {
    public static final String HANDLE = "Handle";  // $NON-NLS$
    public static final String DISPLAY_NAME = "DisplayName";  // $NON-NLS$
    public static final String DOCUMENT_TYPE = "DocumentType";  // $NON-NLS$
    public static final String HAS_SUFFICIENT_RIGHTS = "HasSufficientRights";  // $NON-NLS$

    public static final String MM_LINK_STYLE = "mm-link";  // $NON-NLS$

    private final HTML html = new HTML();
    private HandlerRegistration clickHandlerRegistration;

    private boolean linkEnabled = true;
    private final String styleName;

    public AbstractSpsAsyncLinkWidget(String styleName) {
        this.styleName = styleName;
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);
    }

    public AbstractSpsAsyncLinkWidget withDisabledLink() {
        this.linkEnabled = false;
        return this;
    }

    @Override
    public void onPropertyChange() {
        update();
    }

    @Override
    public void afterPropertiesSet() {
        update();
    }

    private void update() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();

        final String handle = getString(group, HANDLE);
        final String displayName = getString(group, DISPLAY_NAME);

        final boolean visible = StringUtil.hasText(handle) && StringUtil.hasText(displayName);
        this.html.setVisible(visible);
        if(!visible) {
            return;
        }

        this.html.setHTML(new SafeHtmlBuilder()
                .append(getIcon(getString(group, DOCUMENT_TYPE)))
                .appendHtmlConstant("<span>")  // $NON-NLS$
                .appendEscaped(displayName)
                .appendHtmlConstant("</span>")  // $NON-NLS$
                .toSafeHtml());

        this.html.setStyleName(this.styleName);

        final boolean sufficientRights = hasSufficientRights();
        if(sufficientRights) {
            this.html.addStyleName(MM_LINK_STYLE);
        }
        else {
            this.html.removeStyleName(MM_LINK_STYLE);
        }

        updateClickHandler(sufficientRights);
    }

    private static String getString(SpsGroupProperty groupProperty, String bindKey) {
        final SpsLeafProperty property = (SpsLeafProperty) groupProperty.get(bindKey);
        if (property == null) {
            return null;
        }

        return property.getStringValue();
    }

    private static SafeHtml getIcon(String documentType) {
        return IconImage.getImagePrototypeForFileType(documentType).getSafeHtml();
    }

    private void updateClickHandler(final boolean sufficientRights) {
        if(this.clickHandlerRegistration != null) {
            this.clickHandlerRegistration.removeHandler();
        }

        if(sufficientRights) {
            this.clickHandlerRegistration = this.html.addClickHandler(event -> openDocument(true));
        }
    }

    private void openDocument(boolean sufficientRights) {
        if(sufficientRights) {
            final String handle = getString(getBindFeature().getSpsProperty(), HANDLE);
            if (handle != null) {
                Window.open(getUrl(handle), "_blank", "");  // $NON-NLS$
            }
        }
    }

    abstract protected  String getUrl(String handle);

    private boolean hasSufficientRights() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        if(group == null || !this.linkEnabled) {
            return false;
        }

        final String hasSufficientRightsText = getString(group, HAS_SUFFICIENT_RIGHTS);
        return !(Boolean.FALSE.toString().equals(hasSufficientRightsText));
    }

    @Override
    protected HTML createWidget() {
        if(this.linkEnabled) {
            WidgetUtil.makeFocusable(this.html, new DefaultFocusKeyHandler() {
                @Override
                public boolean onFocusKeyClick() {
                    openDocument(hasSufficientRights());
                    return true;
                }
            });
        }
        return this.html;
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.html.addDomHandler(handler, FocusEvent.getType());
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.html.addDomHandler(handler, BlurEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.html.fireEvent(event);
    }
}
