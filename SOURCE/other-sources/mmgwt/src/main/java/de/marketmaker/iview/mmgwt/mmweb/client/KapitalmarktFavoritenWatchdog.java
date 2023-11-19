/*
 * UserMessageWatchdog.java
 *
 * Created on 07.08.2008 11:01:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Date;

import com.google.gwt.event.shared.HandlerManager;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KapitalmarktFavoritenWatchdog extends AbstractMessageWatchdog {
    private static final String KEY = AppConfig.PROP_KEY_KAPITALMARKTFAVORITENID;

    public KapitalmarktFavoritenWatchdog(HandlerManager eventBus) {
        super(eventBus);
    }

    @Override
    protected String getPropertyKey() {
        return KEY;
    }

    @Override
    protected String getTitle() {
        return I18n.I.newKapitalmarktFavoriten() + getDate(); 
    }

    @Override
    protected void showNewMessageDialog() {
        // empty, no dialog.
    }

    protected String getMessage() {
        return I18n.I.messageNewKapitalmarktFavoritenAvailable(); 
    }

    protected String getDirectAccessItem() {
        return "DZ_FI"; // $NON-NLS-0$
    }

    public static String getDate() {
        final String value = SessionData.INSTANCE.getUser().getAppConfig().getProperty(KEY);
        if (value == null) {
            return ""; // $NON-NLS-0$
        }
        final long millis = Long.parseLong(value, Character.MAX_RADIX);
        return Formatter.LF.formatDate(new Date(millis));
    }
}
