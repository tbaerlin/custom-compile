/*
 * AsRightLogoSupplier.java
 *
 * Created on 16.03.2016 10:17
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import java.util.function.Supplier;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Image;

import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Finalizer;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class AsRightLogoSupplier implements Supplier<Image> {
    @SuppressWarnings("Duplicates")
    @Override
    public Image get() {
        final Image image = new Image(getImageUri());
        final Finalizer<HandlerRegistration> finalizer = new Finalizer<>();
        finalizer.set(image.addErrorHandler(event -> {
            final HandlerRegistration handlerRegistration = finalizer.get();
            if (handlerRegistration != null) {
                handlerRegistration.removeHandler();
            }
            image.setUrl(getDefaultImageUri());
        }));

        return image;
    }

    private static SafeUri getImageUri() {
        final String zone = GuiDefsLoader.getModuleName();
        return UriUtils.fromTrustedString("/" + zone + "/images/zones/" + zone + "/as-right-logo.png");
    }

    private static SafeUri getDefaultImageUri() {
        return UriUtils.fromTrustedString("/" + GuiDefsLoader.getModuleName() + "/images/as/as-default-right-logo.png");
    }
}
