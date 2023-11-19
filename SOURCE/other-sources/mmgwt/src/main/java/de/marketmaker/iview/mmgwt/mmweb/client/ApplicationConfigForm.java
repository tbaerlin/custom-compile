/*
* ApplicationConfigForm.java
*
* Created on 11.09.2008 15:54:46
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Arrays;

import javax.inject.Inject;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.NumberFormat;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.AbstractFinder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.Charge;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceConstituentsConfig;

import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature.DZ_RELEASE_2016;
import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature.VWD_RELEASE_2014;

/**
 * @author Michael Lösch
 */
public class ApplicationConfigForm extends AbstractPageController {
    private final CheckBox cbVwdSymbol;

    private final CheckBox cbValorennummer;

    private final CheckBox cbNewsReload;

    private final CheckBox cbHideTeaser;

    private final CheckBox cbHideLimits;

    private final CheckBox cbOpenWatchlistOnAdd;

    private final TextField<String> emailAddress;

    private final TextField<String> minCharge;

    private final TextField<String> percentageCharge;

    private final CheckBox cbShowMainWindowPush;

    private final CheckBox cbShowCashInPortfolio;

    private final CheckBox cbHideAmazon;

    private final CheckBox cbDefaultCustomIssuer;

    private final CheckBox cbTriggerBrowserPrintDialog;

    private final SimpleComboBox<Integer> finderPageSize;

    private WorkspaceConstituentsConfig workspaceConstituentsConfig;

    private final Button btnSave;

    private FeatureFlags featureFlags = Ginjector.INSTANCE.getFeatureFlags();

    class MyLayoutContainer extends LayoutContainer implements NeedsScrollLayout {
        MyLayoutContainer(Layout layout) {
            super(layout);
        }
    }

    private final LayoutContainer view = new MyLayoutContainer(new FlowLayout());

    public static final ApplicationConfigForm INSTANCE = new ApplicationConfigForm();

    private final Radio rPushColorAll = createRadio(I18n.I.update(), false);

    private final Radio rPushColorChange = createRadio(I18n.I.change(), true);

    private final Radio rPushColorNone = createRadio(I18n.I.never(), false);

    private ApplicationConfigForm() {
        this.cbVwdSymbol = GuiUtil.createCheckBox(I18n.I.displayVwdSymbolInPortrait());
        this.cbShowCashInPortfolio = GuiUtil.createCheckBox(I18n.I.showCashInPortfolio());
        this.cbValorennummer = GuiUtil.createCheckBox(I18n.I.searchByValor());
        this.cbNewsReload = GuiUtil.createCheckBox(I18n.I.automaticNewsReload());
        this.cbHideTeaser = GuiUtil.createCheckBox(I18n.I.hideProdTeaser());
        this.cbHideLimits = GuiUtil.createCheckBox(I18n.I.hideLimits());
        this.cbShowMainWindowPush = GuiUtil.createCheckBox(I18n.I.displayPushInWindowTitle(Settings.INSTANCE.mainWindowPushTitle()));
        this.cbHideAmazon = GuiUtil.createCheckBox(I18n.I.hideAmazon());
        this.cbTriggerBrowserPrintDialog = GuiUtil.createCheckBox(I18n.I.triggerBrowserPrintDialog());
        this.cbOpenWatchlistOnAdd = GuiUtil.createCheckBox(I18n.I.openWatchlistOnAdd());
        final String issuerDisplayName = GuiDefsLoader.getIssuerDisplayName();
        this.cbDefaultCustomIssuer = GuiUtil.createCheckBox(I18n.I.alwaysSelected(issuerDisplayName));
        this.emailAddress = GuiUtil.createTextField(I18n.I.emailAddressForLimit(), 110, 240);
        this.minCharge = GuiUtil.createTextField(I18n.I.minimumOrFlatFee(), 110, 240);
        this.percentageCharge = GuiUtil.createTextField(I18n.I.feePercent(), 110, 240);
        this.finderPageSize = GuiUtil.createSimpleComboBox(I18n.I.entriesPerPageInFinderSearchResults(),
                Arrays.asList(AbstractFinder.POSSIBLE_PAGE_SIZES), false);

        final RadioGroup pushColorGroup = new RadioGroup();
        pushColorGroup.setFieldLabel(I18n.I.dyeBackgroundOnPush());
        pushColorGroup.setSelectionRequired(true);
        pushColorGroup.add(this.rPushColorAll);
        pushColorGroup.add(this.rPushColorChange);
        pushColorGroup.add(this.rPushColorNone);

        this.btnSave = new Button(I18n.I.saveSettings(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                save();
            }
        });

        final FormPanel fp = new FormPanel();
        fp.setHeaderVisible(false);

        if (!SessionData.isAsDesign()) {
            final FieldSet workspaceFieldSet = createFieldSet(I18n.I.favorites());
            this.workspaceConstituentsConfig = new WorkspaceConstituentsConfig(this::enableSaveButton);
            workspaceFieldSet.add(this.workspaceConstituentsConfig);
            fp.add(workspaceFieldSet);
        }


        final FieldSet miscFieldSet = createFieldSet(I18n.I.general());
        miscFieldSet.add(this.cbVwdSymbol);
        if (featureFlags.isEnabled0(VWD_RELEASE_2014) || featureFlags.isEnabled0(DZ_RELEASE_2016)) {
            miscFieldSet.add(this.cbShowCashInPortfolio);
        }
        miscFieldSet.add(this.cbValorennummer);
        miscFieldSet.add(this.cbNewsReload);
        if (issuerDisplayName != null) {
            miscFieldSet.add(this.cbHideAmazon);
        }

        if (Selector.DZ_TEASER.isAllowed()) {
            miscFieldSet.add(this.cbHideTeaser);
        }

        if (this.featureFlags.isEnabled0(VWD_RELEASE_2014)) {
            miscFieldSet.add(this.cbHideLimits);
        }

        miscFieldSet.add(cbTriggerBrowserPrintDialog);
        miscFieldSet.add(this.cbOpenWatchlistOnAdd);
        miscFieldSet.add(this.emailAddress);

        if (this.featureFlags.isEnabled0(FeatureFlags.Feature.VWD_RELEASE_2015)) {
            miscFieldSet.add(this.finderPageSize);
        }

        fp.add(miscFieldSet);

        if (SessionData.INSTANCE.isWithPush()) {
            final FieldSet pushFieldSet = createFieldSet(I18n.I.push());
            pushFieldSet.add(pushColorGroup);
            pushFieldSet.add(this.cbShowMainWindowPush);
            fp.add(pushFieldSet);
        }

        final FieldSet portfolioFieldSet = createFieldSet(I18n.I.portfolioFeePerTrade());
        portfolioFieldSet.add(this.minCharge);
        portfolioFieldSet.add(this.percentageCharge);
        fp.add(portfolioFieldSet);

        if (issuerDisplayName != null) {
            final FieldSet finder = createFieldSet(I18n.I.finderAppConf());
            finder.add(this.cbDefaultCustomIssuer);
            fp.add(finder);
        }

        fp.addButton(this.btnSave);
        fp.setButtonAlign(Style.HorizontalAlignment.LEFT);

        fp.setStyleName("mm-appconfig-form"); // $NON-NLS-0$

        this.view.add(fp);
    }

    private void enableSaveButton(boolean oneWorkspaceVisible) {
        this.btnSave.setEnabled(oneWorkspaceVisible);
    }

    private Radio createRadio(String label, boolean value) {
        final Radio result = new Radio();
        result.setBoxLabel(label);
        result.setValue(value);
        return result;
    }

    private FieldSet createFieldSet(String name) {
        final FormLayout layout = new FormLayout();
        layout.setLabelWidth(200);

        final FieldSet result = new FieldSet();
        result.setHeading(name);
        result.setWidth(480);
        result.setLayout(layout);
        return result;
    }

    private void save() {
        ackCheckBoxValue(this.cbNewsReload, AppConfig.NEWS_HEADLINES_AUTO_RELOAD);
        if (featureFlags.isEnabled0(VWD_RELEASE_2014) || featureFlags.isEnabled0(DZ_RELEASE_2016)) {
            ackCheckBoxValueGenerously(this.cbShowCashInPortfolio, AppConfig.SHOW_CASH_IN_PORTFOLIO);
        }
        ackCheckBoxValue(this.cbVwdSymbol, AppConfig.DISPLAY_VWDCODE);
        ackCheckBoxValue(this.cbValorennummer, AppConfig.SEARCH_BY_VALOR);
        ackCheckBoxValue(this.cbHideAmazon, AppConfig.HIDE_AMAZON);
        ackCheckBoxValue(this.cbHideTeaser, AppConfig.HIDE_PROD_TEASER);
        ackCheckBoxValue(this.cbHideLimits, AppConfig.HIDE_LIMITS);
        ackCheckBoxValue(this.cbTriggerBrowserPrintDialog, AppConfig.TRIGGER_BROWSER_PRINT_DIALOG);
        ackCheckBoxValueGenerously(this.cbOpenWatchlistOnAdd, AppConfig.OPEN_WATCHLIST_ON_ADD);
        ackCheckBoxValueGenerously(this.cbDefaultCustomIssuer, AppConfig.CUSTOM_ISSUER_ONLY);
        ackPriceValue(this.minCharge, AppConfig.PROP_KEY_MIN_CHARGE);
        ackPriceValue(this.percentageCharge, AppConfig.PROP_KEY_PERCENTAGE_CHARGE);

        ackPushColor();
        ackCheckBoxValue(this.cbShowMainWindowPush, AppConfig.SHOW_PUSH_IN_TITLE);
        ackSimpleComboValue(this.finderPageSize, AppConfig.PROP_KEY_FINDER_PAGE_SIZE);

        this.workspaceConstituentsConfig.saveSettings();

        // save must not be called explicitly, due to automatic storage handling!

        AlertController.INSTANCE.saveEmailAddress(this.emailAddress.getValue() != null ? this.emailAddress.getValue().trim() : null);
        Charge.getInstance().updateFromConfig();
        TableCellRenderers.PushCompareRenderer.updatePushStyles();
    }

    private void setConfigControls() {
        setCheckBoxValue(this.cbNewsReload, AppConfig.NEWS_HEADLINES_AUTO_RELOAD);
        setCheckBoxValueGenerously(this.cbShowCashInPortfolio, AppConfig.SHOW_CASH_IN_PORTFOLIO);
        setCheckBoxValue(this.cbVwdSymbol, AppConfig.DISPLAY_VWDCODE);
        setCheckBoxValue(this.cbValorennummer, AppConfig.SEARCH_BY_VALOR);
        setCheckBoxValue(this.cbShowMainWindowPush, AppConfig.SHOW_PUSH_IN_TITLE);
        setCheckBoxValue(this.cbHideAmazon, AppConfig.HIDE_AMAZON);
        setCheckBoxValue(this.cbHideTeaser, AppConfig.HIDE_PROD_TEASER);
        setCheckBoxValue(this.cbHideLimits, AppConfig.HIDE_LIMITS);
        setCheckBoxValue(this.cbTriggerBrowserPrintDialog, AppConfig.TRIGGER_BROWSER_PRINT_DIALOG);
        setCheckBoxValueGenerously(this.cbOpenWatchlistOnAdd, AppConfig.OPEN_WATCHLIST_ON_ADD);
        setCheckBoxValueGenerously(this.cbDefaultCustomIssuer, AppConfig.CUSTOM_ISSUER_ONLY);
        setPageSize(this.finderPageSize);

        if (SessionData.INSTANCE.getUserProperty(AppConfig.COLOR_PUSH) != null) {
            this.rPushColorAll.setValue(TableCellRenderers.PushCompareRenderer.isColorUpdates());
            this.rPushColorChange.setValue(TableCellRenderers.PushCompareRenderer.isColorChanges());
            this.rPushColorNone.setValue(!(this.rPushColorAll.getValue() || this.rPushColorChange.getValue()));
        }

        setPriceValue(this.minCharge, AppConfig.PROP_KEY_MIN_CHARGE);
        setPriceValue(this.percentageCharge, AppConfig.PROP_KEY_PERCENTAGE_CHARGE);
    }

    private void setPageSize(SimpleComboBox<Integer> combo) {
        int pageSize = SessionData.INSTANCE.getUser().getAppConfig().getIntProperty(AppConfig.PROP_KEY_FINDER_PAGE_SIZE, AbstractFinder.DEFAULT_PAGE_SIZE);
        combo.setSimpleValue(pageSize);
    }

    private void setCheckBoxValue(final CheckBox cb, final String propertyname) {
        cb.setValue("true".equals(SessionData.INSTANCE.getUserProperty(propertyname))); // $NON-NLS-0$
    }

    private void setCheckBoxValueGenerously(final CheckBox cb, final String propertyname) {
        cb.setValue(!"false".equals(SessionData.INSTANCE.getUserProperty(propertyname))); // $NON-NLS$

    }

    private void setPriceValue(final TextField<String> tf, final String propertyname) {
        tf.setValue(getPriceValue(propertyname));
    }

    private String getPriceValue(String propertyname) {
        final String stored = SessionData.INSTANCE.getUser().getAppConfig().getProperty(propertyname);
        if (stored != null && stored.length() > 0) {
            return NumberFormat.getDecimalFormat().format(Double.valueOf(stored));
        }
        return ""; // $NON-NLS-0$
    }

    private void ackPushColor() {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(AppConfig.COLOR_PUSH, getPushColor());
    }

    private String getPushColor() {
        if (this.rPushColorAll.getValue()) {
            return TableCellRenderers.PushColorType.updates.toString();
        }
        if (this.rPushColorChange.getValue()) {
            return null;
        }
        return TableCellRenderers.PushColorType.none.toString();
    }

    private void ackCheckBoxValue(final CheckBox cb, final String propertyname) {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyname,
                cb.getValue() ? "true" : null); // $NON-NLS-0$
    }

    private void ackSimpleComboValue(final SimpleComboBox<Integer> sb, final String propertyname) {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyname, sb.getSimpleValue());
    }

    private void ackCheckBoxValueGenerously(final CheckBox cb, final String propertyname) {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyname,
                cb.getValue() ? null : "false"); // $NON-NLS-0$
    }

    private void ackPriceValue(final TextField<String> tf, final String propertyname) {
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(propertyname, getPriceValue(tf));
    }

    private String getPriceValue(TextField<String> tf) {
        if (tf.getValue() != null && tf.getValue().length() > 0) {
            return String.valueOf(NumberFormat.getDecimalFormat().parse(tf.getValue()));
        }
        return "";
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        fillInEmailAddress();
        getContentContainer().setContent(this.view);
        setConfigControls();
    }

    private void fillInEmailAddress() {
        final String tmp = AlertController.INSTANCE.getEmailAddress();
        if (StringUtil.hasText(tmp)) {
            this.emailAddress.setValue(tmp);
        }
    }

    @Inject
    public void setFeatureFlags(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }
}
