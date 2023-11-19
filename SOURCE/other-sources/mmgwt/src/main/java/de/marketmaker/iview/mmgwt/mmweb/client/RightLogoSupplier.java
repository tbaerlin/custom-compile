/*
 * RightLogoSupplier.java
 *
 * Created on 16.03.2016 08:50
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.function.Supplier;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Image;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Finalizer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Provides the right logo to the ICE/AS toolbar for the mm[web], mm metals[web], and mm soft commodities [web] variant.
 * For the advisory solution variant see {@linkplain de.marketmaker.iview.mmgwt.mmweb.client.as.AsRightLogoSupplier}.
 * @author mdick
 */
@NonNLS
public class RightLogoSupplier implements Supplier<Image> {

    private static final String TEXT_VARIABLE = "{text}";

    @SuppressWarnings("Duplicates")
    @Override
    public Image get() {
        final SafeUri imageUri = getImageUri();
        final Image image = new Image(imageUri);
        final Finalizer<HandlerRegistration> finalizer = new Finalizer<>();
        finalizer.set(image.addErrorHandler(event -> {
            final HandlerRegistration handlerRegistration = finalizer.get();
            if (handlerRegistration != null) {
                handlerRegistration.removeHandler();
            }
            final SafeUri defaultImageUri = getDefaultImageUri();
            Firebug.info("<RightLogoSupplier> failed to load logo " + (imageUri != null ? imageUri.asString() : "null") + ". Applying default logo: " + defaultImageUri.asString());
            image.setUrl(defaultImageUri);
        }));

        return image;
    }

    private static SafeUri getImageUri() {
        final String metaTagRightLogoUri = JsUtil.getMetaValue("rightLogoUri"); // logo uri is defined in meta tag of html file // $NON-NLS$
        if (metaTagRightLogoUri != null) {
            Firebug.info("<RightLogoSupplier> using logo from HTML meta 'rightLogoUri': " + metaTagRightLogoUri);
            return UriUtils.fromTrustedString(metaTagRightLogoUri);
        }

        final String guidefRightLogoUri = SessionData.INSTANCE.getGuiDefValue("rightLogoUri");
        if (guidefRightLogoUri != null) {
            if (guidefRightLogoUri.contains(TEXT_VARIABLE)) {
                final String customerName = SessionData.INSTANCE.getUser().getCustomerName();
                if (customerName == null) {
                    Firebug.info("<RightLogoSupplier> should use logo from GUIDEF but customer name was null, so no logo is used at all.");
                    return null;
                }
                final String s = UrlBuilder.forDmxml(guidefRightLogoUri.replace(TEXT_VARIABLE, URL.encodeQueryString(customerName))).toURL();
                Firebug.info("<RightLogoSupplier> using logo from GUIDEF value 'rightLogoUri' and replaced '{text'} with customer name: " + s);

                return UriUtils.fromTrustedString(s);
            }
            else {
                Firebug.info("<RightLogoSupplier> using logo from GUIDEF value 'rightLogoUri': " + guidefRightLogoUri);
                return UriUtils.fromTrustedString(guidefRightLogoUri);
            }
        }

        //default logo for mmfweb
        final String zone = GuiDefsLoader.getModuleName();
        final String s = "/" + zone + "/images/zones/" + zone + "/as-right-logo.png";
        Firebug.info("<RightLogoSupplier> using ICE/AS right zone logo: " + s);
        return UriUtils.fromTrustedString(s);
    }

    private static SafeUri getDefaultImageUri() {
        return UriUtils.fromTrustedString("/" + GuiDefsLoader.getModuleName() + "/images/as/as-default-right-logo.png");
    }
}
