/*
 * AsApplicationConfigDisplay.java
 *
 * Created on 30.01.2015 13:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;

/**
 * @author mdick
 */
public interface AsApplicationConfigDisplay {
    void setPresenter(Presenter presenter);

    /**
     * Visibility subsumed by {@linkplain #setMarketDataPropertiesVisible }
     */
    void setDisplayVwdCode(boolean value);

    boolean isDisplayVwdCode();

    /**
     * Visibility subsumed by {@linkplain #setMarketDataPropertiesVisible }
     */
    void setSearchByValor(boolean value);

    boolean isSearchByValor();

    /**
     * Visibility subsumed by {@linkplain #setMarketDataPropertiesVisible }
     */
    void setNewsHeadlinesAutoReload(boolean value);

    boolean isNewsHeadlineAutoReload();

    /**
     * Visibility additionally depends on {@linkplain #setMarketDataPropertiesVisible }
     */
    void setShowCashInPortfolio(boolean value);

    boolean isShowCashInPortfolio();

    void setShowCashInPortfolioVisible(boolean visible);

    /**
     * Visibility subsumed by {@linkplain #setMarketDataPropertiesVisible }
     */
    void setOpenWatchlistOnAdd(boolean value);

    boolean isOpenWatchlistOnAdd();

    /**
     * Visibility subsumed by {@linkplain #setMarketDataPropertiesVisible }
     */
    void setMinCharge(String minCharge);

    String getMinCharge();

    /**
     * Visibility subsumed by {@linkplain #setMarketDataPropertiesVisible }
     */
    void setPercentageCharge(String percentageCharge);

    String getPercentageCharge();

    void setMarketDataPropertiesVisible(boolean visible);

    void setHideAmazon(boolean value);

    boolean isHideAmazon();

    void setHideAmazonVisible(boolean visible);

    void setHideProdTeaser(boolean value);

    boolean isHideProdTeaser();

    void setHideProdTeaserVisible(boolean visible);

    /**
     * Visible in every context
     */
    void setTriggerBrowserPrintDialog(boolean value);

    boolean isTriggerBrowserPrintDialog();

    void setHideLimits(boolean value);

    boolean isHideLimits();

    void setHideLimitsVisible(boolean visible);

    void setEmailAddress(String emailAddress);

    String getEmailAddress();

    void setEmailAddressVisible(boolean visible);

    /**
     * Visibility subsumed by {@linkplain #setPushDependingPropertiesVisible }
     */
    void setDyeBackgroundOnPush(TableCellRenderers.PushColorType value);

    TableCellRenderers.PushColorType getDyeBackgroundOnPush();

    /**
     * Visibility subsumed by {@linkplain #setPushDependingPropertiesVisible }
     */
    void setShowMainWindowPush(boolean value);

    boolean isShowMainWindowPush();

    void setPushDependingPropertiesVisible(boolean visible);

    void setCustomIssuerOnly(boolean value);

    boolean isCustomIssuerOnly();

    void setCustomIssuerSectionVisible(boolean visible);

    void setFinderPageSizeVisible(boolean visible);

    Integer getFinderPageSize();

    void setFinderPageSize(Integer finderPageSize);

    void setResetButtonEnabled(boolean enabled);

    void setSaveButtonEnabled(boolean enabled);

    void updateSouthPanelPinnedMode();

    interface Presenter {
        void onChange();

        void onSave();

        void onReset();
    }
}
