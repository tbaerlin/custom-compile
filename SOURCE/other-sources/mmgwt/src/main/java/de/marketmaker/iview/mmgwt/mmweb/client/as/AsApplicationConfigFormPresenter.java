/*
 * AsApplicationConfigFormPresenter.java
 *
 * Created on 30.01.2015 13:14
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AlertController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.IsPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigSavedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.AbstractFinder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PushColorType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.function.SingleConsumable;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.Charge;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.AsApplicationConfigDisplay.*;

/**
 * @author mdick
 */
public class AsApplicationConfigFormPresenter implements Presenter,
        IsPageController {
    private final AsApplicationConfigDisplay display;

    private final AppConfig appConfig;

    private SimpleResetPageController pageController;

    private final ContentContainer contentContainer;

    private final boolean hideAmazonAndCustomIssuerOnlyVisible;

    private final boolean hideProdTeaserVisible;

    private final boolean hideLimitsVisible;

    private final boolean showCashInPortfolioVisible;

    private final boolean finderPageSizeVisible;

    private final boolean emailAddressVisible;

    private final boolean marketDataPropertiesVisible;

    private final boolean pushDependingPropertiesVisible;

    private boolean changesSaved = true;

    private SingleConsumable<HandlerRegistration> saveHandlerRegistration = new SingleConsumable<>();

    public AsApplicationConfigFormPresenter(ContentContainer cc,
            AsApplicationConfigDisplay display) {
        this.contentContainer = cc;
        this.display = display;
        this.appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        this.display.setPresenter(this);

        this.hideAmazonAndCustomIssuerOnlyVisible = GuiDefsLoader.getIssuerDisplayName() != null;
        this.display.setHideAmazonVisible(this.hideAmazonAndCustomIssuerOnlyVisible);
        this.display.setCustomIssuerSectionVisible(this.hideAmazonAndCustomIssuerOnlyVisible);

        this.hideProdTeaserVisible = Selector.DZ_TEASER.isAllowed();
        this.display.setHideProdTeaserVisible(this.hideProdTeaserVisible);

        this.marketDataPropertiesVisible = SessionData.isWithMarketData();
        this.display.setMarketDataPropertiesVisible(this.marketDataPropertiesVisible);

        this.hideLimitsVisible = FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() && SessionData.isWithLimits();
        this.display.setHideLimitsVisible(this.hideLimitsVisible);

        this.showCashInPortfolioVisible = SessionData.isWithMarketData() && (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled());
        this.display.setShowCashInPortfolioVisible(this.showCashInPortfolioVisible);

        this.finderPageSizeVisible = SessionData.isWithMarketData() && FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled();
        this.display.setFinderPageSizeVisible(this.finderPageSizeVisible);

        this.emailAddressVisible = SessionData.isWithLimits();
        this.display.setEmailAddressVisible(this.emailAddressVisible);

        this.pushDependingPropertiesVisible = SessionData.INSTANCE.isWithPush();
        this.display.setPushDependingPropertiesVisible(this.pushDependingPropertiesVisible);
    }

    @Override
    public void onChange() {
        this.display.setResetButtonEnabled(true);
        this.display.setSaveButtonEnabled(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onSave() {
        boolean appConfigChanged = false;

        if (this.marketDataPropertiesVisible) {
            appConfigChanged |= setTrueIfTrue(this.display.isDisplayVwdCode(), AppConfig.DISPLAY_VWDCODE);
            appConfigChanged |= setTrueIfTrue(this.display.isSearchByValor(), AppConfig.SEARCH_BY_VALOR);
            appConfigChanged |= setTrueIfTrue(this.display.isNewsHeadlineAutoReload(), AppConfig.NEWS_HEADLINES_AUTO_RELOAD);
            appConfigChanged |= setFalseIfFalse(this.display.isOpenWatchlistOnAdd(), AppConfig.OPEN_WATCHLIST_ON_ADD);
            appConfigChanged |= setString(this.display.getMinCharge(), AppConfig.PROP_KEY_MIN_CHARGE);
            appConfigChanged |= setString(this.display.getPercentageCharge(), AppConfig.PROP_KEY_PERCENTAGE_CHARGE);
        }
        if (this.hideAmazonAndCustomIssuerOnlyVisible) {
            appConfigChanged |= setTrueIfTrue(this.display.isHideAmazon(), AppConfig.HIDE_AMAZON);
            appConfigChanged |= setFalseIfFalse(this.display.isCustomIssuerOnly(), AppConfig.CUSTOM_ISSUER_ONLY);
        }
        if (this.hideProdTeaserVisible) {
            appConfigChanged |= setTrueIfTrue(this.display.isHideProdTeaser(), AppConfig.HIDE_PROD_TEASER);
        }
        appConfigChanged |= setFalseIfFalse(this.display.isTriggerBrowserPrintDialog(), AppConfig.TRIGGER_BROWSER_PRINT_DIALOG);

        if (this.hideLimitsVisible) {
            appConfigChanged |= setTrueIfTrue(this.display.isHideLimits(), AppConfig.HIDE_LIMITS);
        }

        if (this.showCashInPortfolioVisible) {
            appConfigChanged |= setFalseIfFalse(this.display.isShowCashInPortfolio(), AppConfig.SHOW_CASH_IN_PORTFOLIO);
        }

        if(this.finderPageSizeVisible) {
            final Integer finderPageSize = this.display.getFinderPageSize();
            if(finderPageSize != null) {
                appConfigChanged |= setInt(finderPageSize, AppConfig.PROP_KEY_FINDER_PAGE_SIZE);
            }
        }

        if (this.emailAddressVisible) {
            // does not change the app config
            final String emailAddress = this.display.getEmailAddress();
            AlertController.INSTANCE.saveEmailAddress(emailAddress != null ? emailAddress.trim() : null);
        }

        if (this.pushDependingPropertiesVisible) {
            appConfigChanged |= setTrueIfTrue(this.display.isShowMainWindowPush(), AppConfig.SHOW_PUSH_IN_TITLE);
            appConfigChanged |= setString(toPushColorTypeName(this.display.getDyeBackgroundOnPush()), AppConfig.COLOR_PUSH);
        }

        // we will not get a config saved event if there is nothing to save
        if(appConfigChanged) {
            // remove immediately so that we do not receive any other config save events that
            // have probably not been triggered by this method.
            this.saveHandlerRegistration.push(EventBusRegistry.get().addHandler(ConfigSavedEvent.getType(), event -> {
                // Do not replace with method reference. This will cause errors in compiled GWT code
                // in other areas that are not signalled in the JavaScript console. That affected code
                // may have nothing to do with this handler registration or its event or handler.
                // noinspection Convert2MethodRef
                this.saveHandlerRegistration.pull().ifPresent((handlerRegistration) -> handlerRegistration.removeHandler());
                onConfigSaved(event.isSuccessfullySaved());
            }));

            this.appConfig.firePropertyChange();
        }
        else {
            onConfigSaved(true);
        }

        Charge.getInstance().updateFromConfig();
        TableCellRenderers.PushCompareRenderer.updatePushStyles();
    }

    private void onConfigSaved(boolean successfullyStored) {
        if (successfullyStored) {
            this.changesSaved = true;
            this.display.setSaveButtonEnabled(false);
            this.display.setResetButtonEnabled(false);
            AbstractMainController.INSTANCE.showMessage(I18n.I.settingsSaved());
        }
        else {
            this.changesSaved = false;
            this.display.setResetButtonEnabled(false);
            AbstractMainController.INSTANCE.showError(I18n.I.errorSavingSettings());
        }
    }

    @Override
    public void onReset() {
        if (this.marketDataPropertiesVisible) {
            this.display.setDisplayVwdCode(isTrueIfTrue(AppConfig.DISPLAY_VWDCODE));
            this.display.setSearchByValor(isTrueIfTrue(AppConfig.SEARCH_BY_VALOR));
            this.display.setNewsHeadlinesAutoReload(isTrueIfTrue(AppConfig.NEWS_HEADLINES_AUTO_RELOAD));
            this.display.setOpenWatchlistOnAdd(isFalseIfFalse(AppConfig.OPEN_WATCHLIST_ON_ADD));
            this.display.setMinCharge(getString(AppConfig.PROP_KEY_MIN_CHARGE));
            this.display.setPercentageCharge(getString(AppConfig.PROP_KEY_PERCENTAGE_CHARGE));
        }
        if (this.hideAmazonAndCustomIssuerOnlyVisible) {
            this.display.setHideAmazon(isTrueIfTrue(AppConfig.HIDE_AMAZON));
            this.display.setCustomIssuerOnly(isFalseIfFalse(AppConfig.CUSTOM_ISSUER_ONLY));
        }
        if (this.hideProdTeaserVisible) {
            this.display.setHideProdTeaser(isTrueIfTrue(AppConfig.HIDE_PROD_TEASER));
        }
        this.display.setTriggerBrowserPrintDialog(isFalseIfFalse(AppConfig.TRIGGER_BROWSER_PRINT_DIALOG));

        if (this.hideLimitsVisible) {
            this.display.setHideLimits(isTrueIfTrue(AppConfig.HIDE_LIMITS));
        }
        if (this.showCashInPortfolioVisible) {
            this.display.setShowCashInPortfolio(isFalseIfFalse(AppConfig.SHOW_CASH_IN_PORTFOLIO));
        }
        if (this.finderPageSizeVisible) {
            this.display.setFinderPageSize(getInt(AppConfig.PROP_KEY_FINDER_PAGE_SIZE, AbstractFinder.DEFAULT_PAGE_SIZE));
        }
        if (this.emailAddressVisible) {
            final String emailAddress = AlertController.INSTANCE.getEmailAddress();
            this.display.setEmailAddress(StringUtil.hasText(emailAddress) ? emailAddress : null);
        }
        if (this.pushDependingPropertiesVisible) {
            this.display.setShowMainWindowPush(isTrueIfTrue(AppConfig.SHOW_PUSH_IN_TITLE));
            this.display.setDyeBackgroundOnPush(toPushColorType(getString(AppConfig.COLOR_PUSH)));
        }

        this.display.setResetButtonEnabled(false);
        this.display.setSaveButtonEnabled(!this.changesSaved);
        this.display.updateSouthPanelPinnedMode();
    }

    private String toPushColorTypeName(PushColorType pushColorType) {
        if (pushColorType == null || pushColorType == PushColorType.changes) {
            return null;
        }
        return pushColorType.name();
    }

    private PushColorType toPushColorType(String pushColorTypeName) {
        if (!StringUtil.hasText(pushColorTypeName)) {
            return PushColorType.changes;
        }
        try {
            return PushColorType.valueOf(pushColorTypeName);
        } catch (IllegalArgumentException iae) {
            return PushColorType.changes;
        }
    }

    private boolean setTrueIfTrue(boolean value, final String propertyName) {
        return this.appConfig.addProperty(propertyName, value ? "true" : null, false); // $NON-NLS$
    }

    private boolean isTrueIfTrue(final String propertyName) {
        return "true".equals(this.appConfig.getProperty(propertyName));  // $NON-NLS$
    }

    private boolean setFalseIfFalse(boolean value, final String propertyName) {
        return this.appConfig.addProperty(propertyName, value ? null : "false", false);  // $NON-NLS$
    }

    private boolean isFalseIfFalse(final String propertyName) {
        return !"false".equals(this.appConfig.getProperty(propertyName));  // $NON-NLS$
    }

    private boolean setString(String value, final String propertyName) {
        return this.appConfig.addProperty(propertyName, value, false);
    }

    private String getString(String propertyName) {
        return this.appConfig.getProperty(propertyName);
    }

    private boolean setInt(int value, final String propertyName) {
        return this.appConfig.addProperty(propertyName, value, false);
    }

    private int getInt(String propertyName, int defaultValue) {
        return this.appConfig.getIntProperty(propertyName, defaultValue);
    }

    private Widget getViewAsWidget() {
        if (this.display instanceof IsWidget) {
            return ((IsWidget) AsApplicationConfigFormPresenter.this.display).asWidget();
        }
        throw new IllegalStateException("application config form view is not a widget");  // $NON-NLS$
    }

    @Override
    public PageController asPageController() {
        if (this.pageController == null) {
            this.pageController = new SimpleResetPageController(this.contentContainer) {
                @Override
                public void onPlaceChange(PlaceChangeEvent event) {
                    onReset();
                    getContentContainer().setContent(getViewAsWidget());
                }

                @Override
                protected void reset() {
                    onReset();
                }
            };
        }
        return this.pageController;
    }
}
