/*
 * AsApplicationConfigFormView.java
 *
 * Created on 30.01.2015 13:12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.ChildrenFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsColumnSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCombo;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDecimalEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsRadios;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.AbstractFinder;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PushColorType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.MMIndexedString;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SimpleMM;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.TiType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asBoolean;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asCode;

/**
 * @author mdick
 */
public class AsApplicationConfigFormView implements AsApplicationConfigDisplay, IsWidget {

    public static final String EMAIL_ADDRESS = "AlertController.EmailAddress"; // $NON-NLS$

    private final static String ISSUER_DISPLAY_NAME = GuiDefsLoader.getIssuerDisplayName();

    private final SpsGroupProperty rootProperty = new SpsGroupProperty("", null);

    private final TaskViewPanel tvp;

    private final SpsColumnSection generalSection;

    private final SpsSection samplePortfolioSection;

    private final SpsColumnSection pushSection;

    private Presenter presenter;

    private final SpsColumnSection issuerSection;

    private final Button resetButton;

    private final Button submitButton;

    public static final HashMap<String, String> PUSH_COLOR_CODE_TO_LABEL = new HashMap<>();

    public static final HashMap<String, String> FINDER_PAGE_SIZE_TO_LABEL = new HashMap<>();

    static {
        PUSH_COLOR_CODE_TO_LABEL.put(PushColorType.updates.name(), I18n.I.update());
        PUSH_COLOR_CODE_TO_LABEL.put(PushColorType.changes.name(), I18n.I.change());
        PUSH_COLOR_CODE_TO_LABEL.put(PushColorType.none.name(), I18n.I.never());

        for (Integer pageSize : AbstractFinder.POSSIBLE_PAGE_SIZES) {
            FINDER_PAGE_SIZE_TO_LABEL.put(pageSize.toString(), pageSize.toString());
        }
    }

    public AsApplicationConfigFormView() {
        final SpsSection rootSection = new SpsSection();
        rootSection.withFormContainer(true);
        rootSection.setBaseStyle("sps-section");  // $NON-NLS$
        rootSection.setStyle("sps-form");  // $NON-NLS$
        rootSection.setLevel(0);

        final ChangeHandler changeHandler = changeEvent -> AsApplicationConfigFormView.this.presenter.onChange();

        final ChildrenFeature children = rootSection.getChildrenFeature();

        this.generalSection = createGeneralSection(this.rootProperty, changeHandler);
        children.addChild(this.generalSection);
        this.pushSection = createPushSection(this.rootProperty, changeHandler);
        children.addChild(this.pushSection);
        this.samplePortfolioSection = createSamplePortfolioSection(this.rootProperty, changeHandler);
        children.addChild(this.samplePortfolioSection);
        this.issuerSection = createIssuerSection(this.rootProperty, changeHandler);
        children.addChild(this.issuerSection);

        SimpleStandaloneEngine.configureSpsWidget(rootSection);

        this.resetButton = SimpleStandaloneEngine.createTaskViewPanelButtonFactory(I18n.I.reset(),
                event -> getPresenter().onReset()).build();
        this.submitButton = SimpleStandaloneEngine.createTaskViewPanelSubmitButtonFactory(I18n.I.save(),
                event -> getPresenter().onSave()).build();
        this.tvp = SimpleStandaloneEngine.createTaskViewPanel(rootSection.getWidget(), this.resetButton, this.submitButton);
    }

    private static SpsColumnSection createGeneralSection(SpsGroupProperty parent,
            ChangeHandler changeHandler) {
        final SpsColumnSection section = SimpleStandaloneEngine.createColumnSection(1, I18n.I.general());
        final ChildrenFeature children = section.getChildrenFeature();

        children.addChild(createCheckBox(AppConfig.DISPLAY_VWDCODE, I18n.I.displayVwdSymbolInPortrait(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.SEARCH_BY_VALOR, I18n.I.searchByValor(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.NEWS_HEADLINES_AUTO_RELOAD, I18n.I.automaticNewsReload(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.HIDE_AMAZON, I18n.I.hideAmazon(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.HIDE_PROD_TEASER, I18n.I.hideProdTeaser(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.HIDE_LIMITS, I18n.I.hideLimits(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.TRIGGER_BROWSER_PRINT_DIALOG, I18n.I.triggerBrowserPrintDialog(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.SHOW_CASH_IN_PORTFOLIO, I18n.I.showCashInPortfolio(), parent, changeHandler));
        children.addChild(createCheckBox(AppConfig.OPEN_WATCHLIST_ON_ADD, I18n.I.openWatchlistOnAdd(), parent, changeHandler));
        children.addChild(createComboBox(AppConfig.PROP_KEY_FINDER_PAGE_SIZE, I18n.I.entriesPerPageInFinderSearchResults(), FINDER_PAGE_SIZE_TO_LABEL, parent, changeHandler));
        children.addChild(createTextBox(EMAIL_ADDRESS, I18n.I.emailAddressForLimitAs(), I18n.I.emailAddressForLimitTooltipAs(), parent, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(section);

        return section;
    }

    private static SpsColumnSection createIssuerSection(SpsGroupProperty parent,
            ChangeHandler changeHandler) {
        final SpsColumnSection section = SimpleStandaloneEngine.createColumnSection(1, I18n.I.finderAppConf());
        final ChildrenFeature children = section.getChildrenFeature();

        children.addChild(createCheckBox(AppConfig.CUSTOM_ISSUER_ONLY, I18n.I.alwaysSelected(ISSUER_DISPLAY_NAME), parent, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(section);

        return section;
    }

    private static SpsSection createSamplePortfolioSection(SpsGroupProperty parent,
            ChangeHandler changeHandler) {
        final SpsSection section = SimpleStandaloneEngine.createColumnSection(1, I18n.I.portfolioFeePerTrade());
        final ChildrenFeature children = section.getChildrenFeature();

        children.addChild(createDecimalEdit(AppConfig.PROP_KEY_MIN_CHARGE, I18n.I.minimumOrFlatFeeAS(), parent, false, changeHandler));
        children.addChild(createDecimalEdit(AppConfig.PROP_KEY_PERCENTAGE_CHARGE, I18n.I.feePercentAS(), parent, true, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(section);

        return section;
    }

    private static SpsColumnSection createPushSection(SpsGroupProperty parent,
            ChangeHandler changeHandler) {
        final SpsColumnSection section = SimpleStandaloneEngine.createColumnSection(1, I18n.I.push());
        final ChildrenFeature children = section.getChildrenFeature();

        children.addChild(createRadioGroup(AppConfig.COLOR_PUSH, I18n.I.dyeBackgroundOnPushAs(),
                PUSH_COLOR_CODE_TO_LABEL, parent, changeHandler));

        children.addChild(createCheckBox(AppConfig.SHOW_PUSH_IN_TITLE,
                I18n.I.displayPushInWindowTitle(Settings.INSTANCE.mainWindowPushTitle()),
                parent, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(section);

        return section;
    }

    private static SpsCheckBox createCheckBox(String bindKey, String label, SpsGroupProperty parent,
            ChangeHandler changeHandler) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_BOOLEAN);
        pti.setBooleanIsKindOption(true);

        final SpsCheckBox spsCheckBox = new SpsCheckBox().withCaption(label)
                .withThreeValueBoolean(!pti.isBooleanIsKindOption());
        spsCheckBox.setBaseStyle("sps-edit");  // $NON-NLS$

        spsCheckBox.getBindFeature().setProperty(SimpleStandaloneEngine.createLeafProperty(bindKey, parent, pti, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(spsCheckBox);

        return spsCheckBox;
    }

    private static SpsDecimalEdit createDecimalEdit(String bindKey, String label,
            SpsGroupProperty parent, boolean percent, ChangeHandler changeHandler) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_NUMBER);
        pti.setNumberProcent(percent);

        final SpsDecimalEdit spsDecimalEdit = new SpsDecimalEdit()
                .withCaption(label)
                .withPercent(percent)
                .withPropertyUpdateOnKeyUp();
        spsDecimalEdit.setBaseStyle("sps-edit");  // $NON-NLS$

        spsDecimalEdit.getBindFeature().setProperty(SimpleStandaloneEngine.createLeafProperty(bindKey, parent, pti, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(spsDecimalEdit);

        return spsDecimalEdit;
    }

    private static SpsEdit createTextBox(String bindKey, String label, String toolTip,
            SpsGroupProperty parent, ChangeHandler changeHandler) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_STRING);

        final SpsEdit spsEdit = new SpsEdit().withCaption(label);
        spsEdit.setBaseStyle("sps-edit");  // $NON-NLS$
        spsEdit.setTooltip(toolTip);

        spsEdit.getBindFeature().setProperty(SimpleStandaloneEngine.createLeafProperty(bindKey, parent, pti, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(spsEdit);

        return spsEdit;
    }

    private static SpsRadios createRadioGroup(String bindKey, String label,
            Map<String, String> codeToLabel, SpsGroupProperty parent, ChangeHandler changeHandler) {

        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_ENUMERATION);
        addEnumElements(codeToLabel, pti);

        final SpsRadios spsRadios = new SpsRadios(codeToLabel, pti.getEnumerationNullValue())
                .withCaption(label);
        spsRadios.setBaseStyle("sps-edit");  // $NON-NLS$

        spsRadios.getBindFeature().setProperty(SimpleStandaloneEngine.createLeafProperty(bindKey, parent, pti, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(spsRadios, true);

        return spsRadios;
    }

    private static SpsCombo createComboBox(String bindKey, String label,
            Map<String, String> codeToLabel, SpsGroupProperty parent,
            ChangeHandler changeHandler) {

        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_ENUMERATION);
        pti.setDemanded(true);
        addEnumElements(codeToLabel, pti);

        final SpsCombo spsCombo = new SpsCombo(codeToLabel, pti.getEnumerationNullValue())
                .withCaption(label);
        spsCombo.setMandatory(pti.isDemanded()); // do not allow to set "no value"
        spsCombo.setOmitMandatoryIcon(pti.isDemanded()); // do not show a mandatory marker if we do not allow to set the "no value"
        spsCombo.setBaseStyle("sps-edit"); // $NON-NLS$
        spsCombo.setStyle("combo"); // $NON-NLS$

        spsCombo.getBindFeature().setProperty(SimpleStandaloneEngine.createLeafProperty(bindKey, parent, pti, changeHandler));

        SimpleStandaloneEngine.configureSpsWidget(spsCombo, true);

        return spsCombo;
    }

    private static void addEnumElements(Map<String, String> codeToLabel, ParsedTypeInfo pti) {
        final List<MM> enumElements = pti.getEnumElements();
        for (Map.Entry<String, String> entry : codeToLabel.entrySet()) {
            final MMIndexedString mmIndexedString = new MMIndexedString();
            mmIndexedString.setCode(entry.getKey());
            mmIndexedString.setValue(entry.getValue());
            enumElements.add(mmIndexedString);
        }
    }

    private String getNumberValue(String bindKey) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            return asString(((SpsLeafProperty) spsProperty).getDataItem());
        }
        return null;
    }

    private void setNumberValue(String bindKey, String value) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            ((SpsLeafProperty) spsProperty).setValue(value, false, true);
        }
    }

    private String getPercentValue(String bindKey) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            final String value = asString(((SpsLeafProperty) spsProperty).getDataItem());
            if (StringUtil.hasText(value)) {
                try {
                    return new BigDecimal(value).movePointRight(2).toPlainString();
                } catch (Exception e) {
                    Firebug.error("<AsApplicationConfigFormView.getPercentValue> parse percent value failed " + value, e);
                }
            }
            return null;
        }
        return null;
    }

    private void setPercentValue(String bindKey, String value) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            try {
                if (StringUtil.hasText(value)) {
                    value = new BigDecimal(value).movePointLeft(2).toPlainString();
                }
                ((SpsLeafProperty) spsProperty).setValue(value, false, true);
            } catch (Exception e) {
                Firebug.error("<AsApplicationConfigFormView.setPercentValue> parse percent value failed " + value, e);
            }
        }
    }

    private Integer toInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            Firebug.warn("<AsApplicationConfigFormView.toInteger> returning null", e);
            return null;
        }
    }

    private boolean getBooleanValue(String bindKey) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            final Boolean aBoolean = asBoolean(((SpsLeafProperty) spsProperty).getDataItem());
            return aBoolean != null ? aBoolean : false;
        }
        return false;
    }

    private void setBooleanValue(String bindKey, boolean value) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            ((SpsLeafProperty) spsProperty).setValue(toDataItemSimple(value), false, true);
        }
    }

    private String getStringValue(String bindKey) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            return asString(((SpsLeafProperty) spsProperty).getDataItem());
        }
        return null;
    }

    private void setStringValue(String bindKey, String value) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            ((SpsLeafProperty) spsProperty).setValue(value, false, true);
        }
    }

    private String getEnumValue(String bindKey) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            return asCode(((SpsLeafProperty) spsProperty).getDataItem());
        }
        return null;
    }

    private void setEnumValue(String bindKey, String code) {
        final SpsProperty spsProperty = this.rootProperty.get(bindKey);
        if (spsProperty instanceof SpsLeafProperty) {
            ((SpsLeafProperty) spsProperty).setValue(code, false, true);
        }
    }

    private SimpleMM toDataItemSimple(Boolean value) {
        if (value == null) {
            return new DefaultMM();
        }

        final MMBool di = new MMBool();
        di.setValue(value
                ? ThreeValueBoolean.TV_TRUE
                : ThreeValueBoolean.TV_FALSE);
        return di;
    }

    private void setRowOfSpsWidgetVisible(SpsColumnSection spsColumnSection, String propertyName,
            boolean visible) {
        final SpsWidget spsWidget = spsColumnSection.findWidget(BindToken.create("/" + propertyName)); // $NON-NLS$
        spsColumnSection.setWidgetVisibility(spsWidget, visible);
    }

    private Presenter getPresenter() {
        return this.presenter;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDisplayVwdCode(boolean value) {
        setBooleanValue(AppConfig.DISPLAY_VWDCODE, value);
    }

    @Override
    public boolean isDisplayVwdCode() {
        return getBooleanValue(AppConfig.DISPLAY_VWDCODE);
    }

    @Override
    public void setSearchByValor(boolean value) {
        setBooleanValue(AppConfig.SEARCH_BY_VALOR, value);
    }

    @Override
    public boolean isSearchByValor() {
        return getBooleanValue(AppConfig.SEARCH_BY_VALOR);
    }

    @Override
    public void setNewsHeadlinesAutoReload(boolean value) {
        setBooleanValue(AppConfig.NEWS_HEADLINES_AUTO_RELOAD, value);
    }

    @Override
    public boolean isNewsHeadlineAutoReload() {
        return getBooleanValue(AppConfig.NEWS_HEADLINES_AUTO_RELOAD);
    }

    @Override
    public void setShowCashInPortfolio(boolean value) {
        setBooleanValue(AppConfig.SHOW_CASH_IN_PORTFOLIO, value);
    }

    @Override
    public boolean isShowCashInPortfolio() {
        return getBooleanValue(AppConfig.SHOW_CASH_IN_PORTFOLIO);
    }

    @Override
    public void setShowCashInPortfolioVisible(boolean visible) {
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.SHOW_CASH_IN_PORTFOLIO, visible);
    }

    @Override
    public void setOpenWatchlistOnAdd(boolean value) {
        setBooleanValue(AppConfig.OPEN_WATCHLIST_ON_ADD, value);
    }

    @Override
    public boolean isOpenWatchlistOnAdd() {
        return getBooleanValue(AppConfig.OPEN_WATCHLIST_ON_ADD);
    }

    @Override
    public void setHideAmazon(boolean value) {
        setBooleanValue(AppConfig.HIDE_AMAZON, value);
    }

    @Override
    public boolean isHideAmazon() {
        return getBooleanValue(AppConfig.HIDE_AMAZON);
    }

    @Override
    public void setHideAmazonVisible(boolean visible) {
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.HIDE_AMAZON, visible);
    }

    @Override
    public void setHideProdTeaser(boolean value) {
        setBooleanValue(AppConfig.HIDE_PROD_TEASER, value);
    }

    @Override
    public boolean isHideProdTeaser() {
        return getBooleanValue(AppConfig.HIDE_PROD_TEASER);
    }

    @Override
    public void setHideProdTeaserVisible(boolean visible) {
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.HIDE_PROD_TEASER, visible);
    }

    @Override
    public void setTriggerBrowserPrintDialog(boolean value) {
        setBooleanValue(AppConfig.TRIGGER_BROWSER_PRINT_DIALOG, value);
    }

    @Override
    public boolean isTriggerBrowserPrintDialog() {
        return getBooleanValue(AppConfig.TRIGGER_BROWSER_PRINT_DIALOG);
    }

    @Override
    public void setHideLimits(boolean value) {
        setBooleanValue(AppConfig.HIDE_LIMITS, value);
    }

    @Override
    public boolean isHideLimits() {
        return getBooleanValue(AppConfig.HIDE_LIMITS);
    }

    @Override
    public void setHideLimitsVisible(boolean visible) {
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.HIDE_LIMITS, visible);
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        this.setStringValue(EMAIL_ADDRESS, emailAddress);
    }

    @Override
    public String getEmailAddress() {
        return this.getStringValue(EMAIL_ADDRESS);
    }

    @Override
    public void setEmailAddressVisible(boolean visible) {
        setRowOfSpsWidgetVisible(this.generalSection, EMAIL_ADDRESS, visible);
    }

    @Override
    public void setDyeBackgroundOnPush(PushColorType value) {
        setEnumValue(AppConfig.COLOR_PUSH, value != null ? value.name() : null);
    }

    @Override
    public PushColorType getDyeBackgroundOnPush() {
        final String enumValue = getEnumValue(AppConfig.COLOR_PUSH);
        return enumValue != null ? PushColorType.valueOf(enumValue) : null;
    }

    @Override
    public void setShowMainWindowPush(boolean value) {
        setBooleanValue(AppConfig.SHOW_PUSH_IN_TITLE, value);
    }

    @Override
    public boolean isShowMainWindowPush() {
        return getBooleanValue(AppConfig.SHOW_PUSH_IN_TITLE);
    }

    @Override
    public void setPushDependingPropertiesVisible(boolean visible) {
        this.pushSection.setVisible(visible);
    }

    @Override
    public void setCustomIssuerOnly(boolean value) {
        setBooleanValue(AppConfig.CUSTOM_ISSUER_ONLY, value);
    }

    @Override
    public boolean isCustomIssuerOnly() {
        return getBooleanValue(AppConfig.CUSTOM_ISSUER_ONLY);
    }

    @Override
    public void setCustomIssuerSectionVisible(boolean visible) {
        this.issuerSection.setVisible(visible);
    }

    @Override
    public void setMarketDataPropertiesVisible(boolean visible) {
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.DISPLAY_VWDCODE, visible);
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.SEARCH_BY_VALOR, visible);
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.NEWS_HEADLINES_AUTO_RELOAD, visible);
        setRowOfSpsWidgetVisible(this.generalSection, AppConfig.OPEN_WATCHLIST_ON_ADD, visible);

        // hides AppConfig.PROP_KEY_MIN_CHARGE and AppConfig.PROP_KEY_PERCENTAGE_CHARGE
        this.samplePortfolioSection.setVisible(visible);
    }

    @Override
    public void setMinCharge(String minCharge) {
        setNumberValue(AppConfig.PROP_KEY_MIN_CHARGE, minCharge);
    }

    @Override
    public String getMinCharge() {
        return getNumberValue(AppConfig.PROP_KEY_MIN_CHARGE);
    }

    @Override
    public void setPercentageCharge(String percentageCharge) {
        setPercentValue(AppConfig.PROP_KEY_PERCENTAGE_CHARGE, percentageCharge);
    }

    @Override
    public String getPercentageCharge() {
        return getPercentValue(AppConfig.PROP_KEY_PERCENTAGE_CHARGE);
    }

    @Override
    public void setFinderPageSizeVisible(boolean visible) {
        setRowOfSpsWidgetVisible(generalSection, AppConfig.PROP_KEY_FINDER_PAGE_SIZE, visible);
    }

    @Override
    public Integer getFinderPageSize() {
        return toInteger(getNumberValue(AppConfig.PROP_KEY_FINDER_PAGE_SIZE));
    }

    @Override
    public void setFinderPageSize(Integer finderPageSize) {
        setNumberValue(AppConfig.PROP_KEY_FINDER_PAGE_SIZE, finderPageSize != null ? Integer.toString(finderPageSize) : null);
    }

    @Override
    public void setResetButtonEnabled(boolean enabled) {
        if (!enabled) {
            this.rootProperty.resetChanged(); //do this here, because the presenter does not know anything about SPS properties.
        }
        this.resetButton.setEnabled(enabled);
    }

    @Override
    public void setSaveButtonEnabled(boolean enabled) {
        if (!enabled) {
            this.rootProperty.resetChanged(); //do this here, because the presenter does not know anything about SPS properties.
        }
        this.submitButton.setEnabled(enabled);
    }

    @Override
    public void updateSouthPanelPinnedMode() {
        this.tvp.updateSouthWidgetPinned();
        this.tvp.layout();
    }

    @Override
    public Widget asWidget() {
        return this.tvp;
    }
}