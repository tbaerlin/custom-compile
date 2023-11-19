/*
 * LiveFinderCER.java
 *
 * Created on 10.06.2008 13:44:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ListBox;

import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FinderLinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.DZ_BANK_USER;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LiveFinderCER extends LiveFinder<CERFinder, CERFinderMetadata> {

    public static final String CER_FINDER_ID = "LCER"; // $NON-NLS-0$

    public static final String LEV_FINDER_ID = "LLEV"; // $NON-NLS-0$

    public static final String UNDERLYING_ID = "cerunderlying"; // $NON-NLS$

    public static final String BASE_ID = "cerbase"; // $NON-NLS$

    public static final String FIXED_TOP_ID = "cerfixedtop"; // $NON-NLS$

    public static final String HIDE_NOT_ACTIVE = "notActive"; // $NON-NLS$

    public static final String LEVERAGE_TYPE = "leverageType";  // $NON-NLS$

    public static final String DIS_ID = CertificateTypeEnum.CERT_DISCOUNT.toString();

    public static final String BON_ID = CertificateTypeEnum.CERT_BONUS.toString();

    public static final String IND_ID = CertificateTypeEnum.CERT_INDEX.toString();

    public static final String REV_ID = CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE.toString();

    public static final String STRUCT_ID = CertificateTypeEnum.CERT_STRUCTURED_BOND.toString();

    public static final String SPRINTER_ID = CertificateTypeEnum.CERT_SPRINTER.toString();

    public static final String SPRINT_ID = CertificateTypeEnum.CERT_SPRINT.toString();

    public static final String OUT_ID = CertificateTypeEnum.CERT_OUTPERFORMANCE.toString();

    public static final String EXP_ID = CertificateTypeEnum.CERT_EXPRESS.toString();

    public static final String REVERSE_CONVERTIBLE_COM_ID = CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE_COM.toString();

    public static final String TWIN_ID = CertificateTypeEnum.CERT_TWINWIN.toString();

    private String currentType = "";

    private static final String PROVIDER_PREFERENCE = "providerPreference", SEDEX_PROVIDER = "SEDEX"; // $NON-NLS$

    private final LeverageConfig leverage;

    private TypeFinderLinkListener linkListener = null;

    public enum LeverageConfig {
        CER, LEV, BOTH;
    }

    public static class TypeFinderLinkListener implements FinderLinkListener<String> {

        private final LiveFinderCER controller;

        private final String place;

        private TypeFinderLinkListener(LiveFinderCER controller, final String place) {
            this.controller = controller;
            this.place = place;
        }

        public void onClick(LinkContext<String> context, Element e) {
            final String type = context.data;
            final FinderController controller = this.controller;
            final FinderFormConfig ffc = new FinderFormConfig("temp", this.controller.getId()); // $NON-NLS-0$
            ffc.put(BASE_ID, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.TYPE, "true"); // $NON-NLS$
            ffc.put(FinderFormKeys.TYPE + "-item", type); // $NON-NLS-0$
            ffc.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
            ffc.put(FinderFormKeys.SORT + "-item", "wkn"); // $NON-NLS$
            Customer.INSTANCE.prepareCerBestToolFinderLinkConfig(ffc);
            controller.prepareFind(ffc);
            PlaceUtil.goTo(this.place);
        }

        public LinkContext<String> createContext(String type) {
            return new LinkContext<>(this, type);
        }
    }

    public static final LiveFinderCER INSTANCE_CER = new LiveFinderCER(DZ_BANK_USER.isAllowed()
            ? LeverageConfig.CER : LeverageConfig.BOTH);

    public static final LiveFinderCER INSTANCE_LEV = DZ_BANK_USER.isAllowed()
            ? new LiveFinderCER(LeverageConfig.LEV) : null;

    private final ArrayList<Item> types = new ArrayList<>();

    private ArrayList<Item> subtypes = new ArrayList<>();

    private ArrayList<Item> leverageTypes = new ArrayList<>();

    private final ArrayList<FinderSection> typeSpecificSections = new ArrayList<>();

    private LiveListBoxOption typeOption;

    private LiveListBoxOption subTypeOption;

    private LiveListBoxOption leverageTypeOption;

    private final Map<CertificateTypeEnum, DataLoader<TableDataModel>> typeDataModelMap = new HashMap<>();

    private LiveFinderCER(LeverageConfig leverage) {
        super("CER_Finder"); // $NON-NLS-0$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
        this.metaBlock.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$

        this.sortFields.add(new Item(I18n.I.name(), "name"));  // $NON-NLS-0$
        this.sortFields.add(new Item("WKN", "wkn")); // $NON-NLS$
        this.sortFields.add(new Item(I18n.I.unchangedYieldPercentAbbr(), "unchangedYieldRelative")); // $NON-NLS$
        this.sortFields.add(new Item(I18n.I.maximumYieldPercentAbbr(), "maximumYieldRelative")); // $NON-NLS$
        this.sortFields.add(new Item(I18n.I.performanceAlltimePercentAbbr(), "performanceAlltime")); // $NON-NLS$
        this.sortFields.add(new Item(I18n.I.performanceAbbr("3M"), "performance3m")); // $NON-NLS$
        this.sortFields.add(new Item(I18n.I.leverage(), "leverage")); // $NON-NLS$
        if (Selector.EDG_RATING.isAllowed()) {
            this.sortFields.add(new Item(I18n.I.riskClass(), "edgTopClass"));  // $NON-NLS-0$
        }

        for (CertificateTypeEnum certificateType : CertificateTypeEnum.values()) {
            if (CertificateTypeEnum.isCertificateAllowed(certificateType, isLeverage())) {
                this.types.add(new Item(certificateType.getDescription(), certificateType.toString()));
            }
        }

        this.leverage = leverage;

        this.linkListener = new TypeFinderLinkListener(this, LeverageConfig.LEV.equals(leverage) ? "M_LF_LEV" : "M_LF_CER"); // $NON-NLS$

        initTypeDataModelMap();
    }

    @Override
    protected int getDefaultViewOffset() {
        return Selector.EDG_RATING.isAllowed()
                ? 2
                : 1;
    }

    private void initTypeDataModelMap() {
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_DISCOUNT, this::createDiscountModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_BONUS, this::createBonusModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_INDEX, this::createIndexModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE, this::createReverseConvertibleModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_SPRINTER, this::createSprintModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_EXPRESS, this::createExpressModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_BASKET, this::createBasketModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_GUARANTEE, this::createGuaranteeModel);
        this.typeDataModelMap.put(CertificateTypeEnum.KNOCK, this::createKnockoutModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_OTHER, this::createOtherModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_OUTPERFORMANCE, this::createOutperformanceModel);

        // Sedex
        this.typeDataModelMap.put(CertificateTypeEnum.CALL, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ESOT_STRUTT, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_BESTIAME, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_ENERGIA, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_INDICE_DI_C, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_LEVERAGED, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_MATERIE_PRI, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_METALLI_IND, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_METALLI_PRE, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_PRODOTTI_AG, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETCS_ETC_LEVERA, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETCS_ETC_SHORT, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETC_SHORT, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETCS_INDEX_COMM, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETCS_INDUSTRIAL, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.ETCS_PRECIOUS_M, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.INV_CERT, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.LEV_CERT_BEAR, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.LEV_CERT_BULL, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.PUT, this::createBaseModel);
        this.typeDataModelMap.put(CertificateTypeEnum.CERT_FACTOR, this::createOtherModel);
    }

    @Override
    protected ViewSpec[] getResultViewSpec() {
        return createResultViewSpec();
    }

    static ViewSpec[] createResultViewSpec() {
        List<ViewSpec> viewSpec = new ArrayList<>();
        viewSpec.add(new ViewSpec(I18n.I.basis()));
        if (Selector.EDG_RATING.isAllowed()) {
            viewSpec.add(new ViewSpec("EDG")); // $NON-NLS-0$
        }
        viewSpec.add(new ViewSpec(I18n.I.typeSpecific()));
        return viewSpec.toArray(new ViewSpec[viewSpec.size()]);
    }

    public String getId() {
        if (isLeverage()) {
            return LEV_FINDER_ID;
        }
        return CER_FINDER_ID;
    }

    public String getViewGroup() {
        return "finder-cer"; // $NON-NLS-0$
    }

    @Override
    public void prepareFind(String field1, String value1, String field2, String value2) {
        final FinderFormConfig result = new FinderFormConfig("multifind", getId()); // $NON-NLS-0$
        result.put(BASE_ID, "true"); // $NON-NLS$
        if (FinderFormKeys.ISSUER_NAME.equals(field1) && value1 != null) {
            result.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
            result.put(FinderFormKeys.ISSUER_NAME + "-item", value1); // $NON-NLS$
        }
        if (FinderFormKeys.TYPE.equals(field2) && value2 != null) {
            result.put(FinderFormKeys.TYPE, "true"); // $NON-NLS$
            result.put(FinderFormKeys.TYPE + "-item", value2); // $NON-NLS$
        }
        else if (LEVERAGE_TYPE.equals(field2) && value2 != null) {
            result.put(LEVERAGE_TYPE, "true"); // $NON-NLS$
            result.put(LEVERAGE_TYPE + "-item", value2); // $NON-NLS$
        }
        result.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
        result.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS$
        prepareFind(result);
    }

    public LiveFinderCER findNewProducts() {
        final FinderFormConfig result = new FinderFormConfig("newproducts", getId()); // $NON-NLS-0$
        result.put(BASE_ID, "true"); // $NON-NLS$
        result.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS$
        result.put(FinderFormKeys.NEW_PRODUCTS_ISSUE_DATE, "true"); // $NON-NLS$
        result.put(FinderFormKeys.NEW_PRODUCTS_ISSUE_DATE + "-item", "ONE_DAY"); // $NON-NLS$
        prepareFind(result);
        return this;
    }

    public LiveFinderCER findDzPibs() {
        final FinderFormConfig result = new FinderFormConfig("withDzPibs", getId()); // $NON-NLS-0$
        result.put(BASE_ID, "true"); // $NON-NLS$
        result.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS-0$
        result.put(FinderFormKeys.PIB_AVAILABLE, "true"); // $NON-NLS-0$
        prepareFind(result);
        return this;
    }

    protected FinderFormConfig createFormConfig(HistoryToken historyToken) {
        final String issuername = historyToken.getFromAll(1, null);
        final String type = historyToken.getFromAll(2, null);
        final String underlyingIid = historyToken.getFromAll(3, null);

        final FinderFormConfig result = new FinderFormConfig("temp", getId()); // $NON-NLS-0$
        result.put(BASE_ID, "true"); // $NON-NLS$
        if (issuername != null) {
            result.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
            result.put(FinderFormKeys.ISSUER_NAME + "-item", issuername); // $NON-NLS$
        }
        if (type != null) {
            result.put(FinderFormKeys.TYPE, "true"); // $NON-NLS$
            result.put(FinderFormKeys.TYPE + "-item", type); // $NON-NLS-0$
        }
        if (underlyingIid != null) {
            result.put(FinderFormKeys.UNDERLYING, "true"); // $NON-NLS$
            result.put(FinderFormKeys.UNDERLYING + "-item", underlyingIid); // $NON-NLS-0$
        }
        result.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
        result.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS$
        return result;
    }

    private boolean hasSedexProvider() {
        return SEDEX_PROVIDER.equals(SessionData.INSTANCE.getGuiDefValue(PROVIDER_PREFERENCE));
    }

    protected void addSections() {
        addFixedTopSection();
        addSectionUnderlying();
        addSectionBase();
        //TODO: do nicer...
        if (!hasSedexProvider()) {
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_DISCOUNT, isLeverage())) {
                addSectionDiscount();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_BONUS, isLeverage())) {
                addSectionBonus();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE, isLeverage())) {
                addSectionAktienanleihe();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_OUTPERFORMANCE, isLeverage())) {
                addSectionOutperformance();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_SPRINTER, isLeverage())) {
                addSectionSprinter();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_EXPRESS, isLeverage())) {
                addSectionExpress();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE_COM, isLeverage())) {
                addSectionRohstoffanleihe();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_SPRINT, isLeverage())) {
                addSectionSprint();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_STRUCTURED_BOND, isLeverage())) {
                addSectionStrukturierteAnleihe();
            }
            if (CertificateTypeEnum.isCertificateAllowed(CertificateTypeEnum.CERT_TWINWIN, isLeverage())) {
                addSectionTwinWin();
            }
        }
        if (Selector.EDG_RATING.isAllowed()) {
            addSectionEdg();
        }
    }

    private void addFixedTopSection() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();

            if (!isLeverage() || Selector.DZ_BANK_USER.isAllowed() || !Selector.WGZ_BANK_USER.isAllowed()) {
                addCustomIssuerOnlyElement(elements);
            }

            if (Selector.PRODUCT_WITH_PIB.isAllowed() && Selector.DZ_BANK_USER.isAllowed()) {
                elements.add(new LiveBooleanOption(FinderFormKeys.PIB_AVAILABLE,
                        I18n.I.offerWithPib(), searchHandler));
            }

            if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                AbstractOption hideNotActive = new LiveBooleanOption(HIDE_NOT_ACTIVE, I18n.I.hideInactiveInstruments(), true, searchHandler);
                hideNotActive.setValue(true);
                elements.add(hideNotActive);
            }

            return elements;
        };

        final FinderSection finderSection = addSection(FIXED_TOP_ID,
                "fixedTopSection", true, elementsLoader, searchHandler).loadElements(); // $NON-NLS$
        finderSection.setAlwaysExpanded();
    }

    private void addSectionTwinWin() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(TWIN_ID);

            int defaultOrder = 0;
            elements.add(createLiveFromToTextOption("gapStrikeRelative", "gapStrikeRelative", // $NON-NLS$
                    I18n.I.gapStrike(), "%", searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("underlyingToCapRelative", "gapBarrierRelative", I18n.I.gapBarrier(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf, defaultOrder)); // $NON-NLS$

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(TWIN_ID, I18n.I.forTwinWin(), false, elementsLoader,
                this.searchHandler).loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionStrukturierteAnleihe() {
        final DataLoader<List<FinderFormElement>> elementsLoader = getListDataLoader(STRUCT_ID);
        final FinderSection section = addSection(STRUCT_ID, I18n.I.forStrukturierteAnleihe(), false, elementsLoader,
                this.searchHandler).loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionSprint() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(SPRINT_ID);

            elements.add(createLiveFromToTextOption("gapCap", "gapCap", I18n.I.gapCap(), // $NON-NLS$
                    "", searchHandler).withConf(sectionConf, 0));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(SPRINT_ID, I18n.I.forSprint(), false, elementsLoader,
                this.searchHandler).loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionRohstoffanleihe() {
        final DataLoader<List<FinderFormElement>> elementsLoader = getListDataLoader(REVERSE_CONVERTIBLE_COM_ID);
        final FinderSection section = addSection(REVERSE_CONVERTIBLE_COM_ID, I18n.I.forRohstoffAnleihe(), false, elementsLoader,
                this.searchHandler).loadElements();
        this.typeSpecificSections.add(section);
    }

    private DataLoader<List<FinderFormElement>> getListDataLoader(String sectionId) {
        return () -> {
                final List<FinderFormElement> elements = new ArrayList<>();
                final String[] sectionConf = SectionConfigUtil.getSectionConf(sectionId);

                elements.add(createLiveFromToTextOption("coupon", "coupon", // $NON-NLS$
                        I18n.I.couponIR(), "%", searchHandler).withConf(sectionConf, 0));

                elements.addAll(handleClones(sectionConf, elements));
                return orderBySectionConf(elements, sectionConf);
            };
    }

    private void addSectionExpress() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(EXP_ID);

            elements.add(createLiveFromToTextOption("underlyingToCapRelative", "gapBarrierRelative", I18n.I.gapBarrier(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf, 0)); // $NON-NLS$

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(EXP_ID, I18n.I.forExpressCertificate(), false, elementsLoader,
                this.searchHandler).loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionDiscount() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(DIS_ID);

            int defaultOrder = 0;
            elements.add(createLiveFromToTextOption("cap", I18n.I.cap(), searchHandler).withConf(sectionConf, defaultOrder++));  // $NON-NLS-0$
            elements.add(createLiveFromToTextOption("capLevel", "capLevel", I18n.I.capLevel(), "%", searchHandler).withConf(sectionConf, defaultOrder++)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("maximumYieldRelativePerYear", "maximumYieldRelativePerYear", // $NON-NLS$
                    I18n.I.maximumYieldRelativePerYearAbbr(), "%", searchHandler).withConf(sectionConf, defaultOrder));// $NON-NLS$
            elements.add(createLiveFromToTextOption("discountRelative", "discountRelative", I18n.I.discount(), "%", searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLiveFromToTextOption("gapCapRelative", "gapCapRelative", I18n.I.gapCapAbbr(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("unchangedYieldRelativePerYear", "unchangedYieldRelativePerYear", // $NON-NLS$
                    I18n.I.unchangedYieldRelativePerYear(), "%", searchHandler).withConf(sectionConf)); // $NON-NLS$

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(DIS_ID, I18n.I.forDiscountCertificate(), false, elementsLoader,
                this.searchHandler).withConfigurable().loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionBonus() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(BON_ID);
            int defaultOrder = 0;
            elements.add(createLiveFromToTextOption("yield", "yield", I18n.I.bonusProfit(), I18n.I.currency(), searchHandler) // $NON-NLS$
                    .withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("yieldRelative", "yieldRelative", I18n.I.bonusYield(), "%", searchHandler) // $NON-NLS$
                    .withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("underlyingToCapRelative", "underlyingToCapRelative", I18n.I.underlyingToCapRelative(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf, defaultOrder)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("yieldRelativePerYear", "yieldRelativePerYear", I18n.I.yieldRelativePerYearAbbr(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("gapBonusLevelRelative", "gapBonusLevelRelative", I18n.I.gapBonusLevel(), "%", // $NON-NLS$
                    searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("agioRelative", "agioRelative", I18n.I.agioRelative(), "%", searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(createLiveFromToTextOption("agioRelativePerYear", "agioRelativePerYear", I18n.I.agioRelativePerYearAbbr(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
            final LiveBooleanOption barrierOption = new LiveBooleanOption("isknockout", I18n.I.intactBarrier(), searchHandler); // $NON-NLS$
            barrierOption.setValue(true);
            elements.add(barrierOption.withConf(sectionConf));
            elements.add(createLiveFromToTextOption("gapCap", "gapCap", I18n.I.gapCap(), // $NON-NLS$
                    "", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("cap", I18n.I.cap(), searchHandler).withConf(sectionConf));  // $NON-NLS-0$
            elements.add(createLiveFromToTextOption("capLevel", "capLevel", I18n.I.capLevel(), "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("gapStrikeRelative", "gapStrikeRelative", // $NON-NLS$
                    I18n.I.gapStrike2(), "%", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("gapStrike", "gapStrike", // $NON-NLS$
                    I18n.I.gapStrike(), I18n.I.currency(), searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("barrier", "barrier", I18n.I.barrier(), I18n.I.currency(), searchHandler) // $NON-NLS$
                    .withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(BON_ID, I18n.I.forBonusCertificate(), false, elementsLoader, this.searchHandler)
                .withConfigurable().loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionAktienanleihe() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(REV_ID);

            int defaultOrder = 0;
            elements.add(createLiveFromToTextOption("gapStrikeRelative", "gapStrikeRelative", // $NON-NLS$
                    I18n.I.gapStrike(), "%", searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("maximumYieldRelativePerYear", "maximumYieldRelativePerYear", // $NON-NLS$
                    I18n.I.maximumYieldRelativePerYearAbbr(), "%", searchHandler).withConf(sectionConf, defaultOrder++)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("unchangedYieldRelativePerYear", "unchangedYieldRelativePerYear", // $NON-NLS$
                    I18n.I.unchangedYieldRelativePerYear(), "%", searchHandler).withConf(sectionConf, defaultOrder)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("underlyingToCapRelative", "underlyingToCapRelative", I18n.I.allowedDownturn(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("capToUnderlyingRelative", "capToUnderlyingRelative", // $NON-NLS$
                    I18n.I.necessaryPerformance(), "%", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("coupon", "coupon", // $NON-NLS$
                    I18n.I.couponIR(), "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
            final LiveBooleanOption barrierOption = new LiveBooleanOption("barrierIntact", I18n.I.intactBarrier(), searchHandler) { // $NON-NLS$
                @Override
                protected String doGetQuery() {
                    return "gapStrike>'0'"; // $NON-NLS$
                }
            };
            barrierOption.setValue(true);
            elements.add(barrierOption.withConf(sectionConf));
            elements.add(createLiveFromToTextOption("gapStrike", "gapStrike", // $NON-NLS$
                    I18n.I.gapStrike(), "", searchHandler).withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(REV_ID, I18n.I.forStockBonds(), false, elementsLoader,
                this.searchHandler).withConfigurable().loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionSprinter() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(SPRINTER_ID);

            int defaultOrder = 0;
            elements.add(createLiveFromToTextOption("maximumYieldRelativePerYear", "maximumYieldRelativePerYear", // $NON-NLS$
                    I18n.I.maximumYieldRelativePerYearAbbr(), "%", searchHandler).withConf(sectionConf, defaultOrder++)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("gapStrike", "gapStrike", // $NON-NLS$
                    I18n.I.gapStrike(), "", searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("cap", I18n.I.cap(), searchHandler).withConf(sectionConf, defaultOrder));  // $NON-NLS-0$
            elements.add(createLiveFromToTextOption("gapCap", "gapCap", I18n.I.gapCap(), // $NON-NLS$
                    "", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("participationLevel", "participationLevel", I18n.I.participationLevel(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(SPRINTER_ID, I18n.I.forSprinter(), false, elementsLoader,
                searchHandler).withConfigurable().loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionOutperformance() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(OUT_ID);

            int defaultOrder = 0;
            elements.add(createLiveFromToTextOption("gapStrikeRelative", "gapStrikeRelative", // $NON-NLS$
                    I18n.I.gapStrike(), "%", searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("gapStrike", "gapStrike", // $NON-NLS$
                    I18n.I.gapStrike(), "", searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("gapCapRelative", "gapCapRelative", I18n.I.gapCapRelative(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf, defaultOrder++));
            elements.add(createLiveFromToTextOption("participationLevel", "participationLevel", I18n.I.participationLevel2(), // $NON-NLS$
                    searchHandler).withConf(sectionConf, defaultOrder));
            elements.add(createLiveFromToTextOption("agioRelative", "agioRelative", I18n.I.agioRelative(), "%", // $NON-NLS$
                    searchHandler).withConf(sectionConf));
            elements.add(createLiveFromToTextOption("agioRelativePerYear", "agioRelativePerYear", I18n.I.agioRelativePerYearAbbr(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf)); // $NON-NLS$
            elements.add(createLiveFromToTextOption("participationLevel", "participationLevel", I18n.I.participationLevel(), // $NON-NLS$
                    "%", searchHandler).withConf(sectionConf));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        final FinderSection section = addSection(OUT_ID, I18n.I.forOutperformanceCertificate(), false, elementsLoader,
                searchHandler).withConfigurable().loadElements();
        this.typeSpecificSections.add(section);
    }

    private void addSectionBase() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);

            int defaultOrder = 0;

            createIssuerElement(elements, sectionConf, defaultOrder++);

            final DynamicSearchHandler typeOptionHandler = new DynamicSearchHandler();
            typeOption = createTypeOption(typeOptionHandler);
            typeOptionHandler.withElement(typeOption.withConf(sectionConf, defaultOrder++));
            currentType = types.get(0).item;
            elements.add(typeOption);
            if (!hasSedexProvider()) {
                subTypeOption = createLiveListBoxOption(FinderFormKeys.SUBTYPE, I18n.I.subcategory(), subtypes, null, searchHandler);
                elements.add(subTypeOption.withConf(sectionConf, defaultOrder++));
            }

            if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                leverageTypeOption = createLiveListBoxOption(LEVERAGE_TYPE, "Put/Call", leverageTypes, null, searchHandler);  // $NON-NLS$
                elements.add(leverageTypeOption.withConf(sectionConf, defaultOrder++));
            }
            elements.add(new LiveTextOption(FinderFormKeys.MULTI_ASSET_NAME, I18n.I.searchString(), searchHandler)
                    .withWidth("160px").withConf(sectionConf)); // $NON-NLS$
            addMarkets(elements, sectionConf);
            elements.add(new LiveStartEndOption(FinderFormKeys.ISSUE_DATE, FinderFormKeys.ISSUE_DATE, I18n.I.issueDate2(), "", LIST_EXPIRATION_DATES, // $NON-NLS$
                    DateTimeUtil.PeriodMode.PAST, searchHandler).withConf(sectionConf));
            elements.add(new LiveStartEndOption(FinderFormKeys.NEW_PRODUCTS_ISSUE_DATE, "issueDate", I18n.I.newProductsTradedSince(), "", LIST_ISSUE_DATES, // $NON-NLS$
                    DateTimeUtil.PeriodMode.PAST, searchHandler).withHideFromTo().withConf(sectionConf));
            elements.add(new LiveBooleanOption("isEndless", // $NON-NLS$
                    I18n.I.endless(), searchHandler).withConf(sectionConf));
            elements.add(createLiveStartEndOption(FinderFormKeys.EXPIRATION_DATE, FinderFormKeys.EXPIRATION_DATE, I18n.I.maturity2(), "", LIST_EXPIRATION_DATES, // $NON-NLS$
                    DateTimeUtil.PeriodMode.FUTURE, searchHandler).withConf(sectionConf));
            final FromToTextOption remaining = new LiveFromToTextOption(FinderFormKeys.REMAINING, "expirationDate", I18n.I.remainingTime(), // $NON-NLS$
                    I18n.I.months(), searchHandler) {
                protected String toQueryValue(String input, boolean query) {
                    return toQueryValueMonths(input, -1, -1, query);
                }
            };
            elements.add(createLiveFromToTextOption("strike", I18n.I.strike(), searchHandler) // $NON-NLS$
                    .withConf(sectionConf));
            elements.add(new LiveBooleanOption("quanto", // $NON-NLS$
                    I18n.I.isQuanto(), searchHandler).withConf(sectionConf));

            elements.add(remaining.withConf(sectionConf));

            if (isLeverage()) {
                elements.add(createLiveFromToTextOption("gapStrikeRelative", "gapStrikeRelative", // $NON-NLS$
                        I18n.I.gapStrike(), "%", searchHandler).withConf(sectionConf));
            }

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        FinderSection cerBaseConf = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, this.searchHandler);
        if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
            cerBaseConf.setValue(true);
        }
        setDefaultQuery(cerBaseConf.expand().withConfigurable()).loadElements();
    }

    private FinderSection setDefaultQuery(FinderSection section) {
        switch (this.leverage) {
            case CER:
                return section.withDefaultQuery("dzIsLeverageProduct=='false'"); // $NON-NLS-0$
            case LEV:
                return section.withDefaultQuery("dzIsLeverageProduct=='true'"); // $NON-NLS-0$
            default:
                return section;
        }
    }

    private void createIssuerElement(List<FinderFormElement> elements, String[] sectionConf,
            int defaultOrder) {
        final FinderFormElements.LiveMultiEnumOption issuerElement = createSortableLiveMultiEnum(
                FinderFormKeys.ISSUER_NAME, I18n.I.issuer(), "+DZ BANK", getMetaLists(), FinderFormKeys.ISSUER_NAME, searchHandler); // $NON-NLS$
        elements.add(issuerElement.withConf(sectionConf, defaultOrder));
    }

    private void addSectionUnderlying() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(UNDERLYING_ID);

            final SymbolOption symbolOption = createUnderlyingOption(false, "CER",  // $NON-NLS$
                    DZ_BANK_USER.isAllowed() ? isLeverage() : null);
            elements.add(symbolOption.withConf(sectionConf, 0));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
        addSection(UNDERLYING_ID, I18n.I.underlyingInstrument(), false,
                elementsLoader, this.searchHandler).loadElements();
    }

    private static void fillTypeListBox(ListBox lb, FinderMetaList list, String selected) {
        if (list == null || list.getElement() == null || list.getElement().isEmpty()) {
            return;
        }

        final Map<String, String> nameKeyMap = new LinkedHashMap<>();
        for (FinderMetaList.Element element : list.getElement()) {
            if (StringUtil.hasText(element.getName())) {
                final CertificateTypeEnum type = CertificateTypeEnum.valueOf(element.getKey());
                final String itemName = StringUtil.hasText(element.getCount())
                        ? (type.getDescription() + " (" + element.getCount() + ")")
                        : type.getDescription();
                nameKeyMap.put(itemName, element.getKey());
            }
        }
        addItems(nameKeyMap, selected, lb);
    }

    protected static void addItems(Map<String, String> nameKeyMap, String selected, ListBox lb) {
        final List<String> itemNames = new ArrayList<>(nameKeyMap.keySet());
        Collections.sort(itemNames);
        for (String itemName : itemNames) {
            final String value = nameKeyMap.get(itemName);
            lb.addItem(itemName, value);
            if (selected != null && selected.equals(value)) {
                lb.setSelectedIndex(lb.getItemCount() - 1);
            }
        }
    }

    private LiveListBoxOption createTypeOption(final DynamicSearchHandler typeOptionHandler) {
        return new LiveListBoxOption(FinderFormKeys.TYPE, FinderFormKeys.TYPE, I18n.I.category(), types,
                types.get(0).item, typeOptionHandler) {

            @Override
            public String getSelectedValue() {
                if (getValue()) {
                    return super.getSelectedValue();
                }
                return isLeverage()
                        ? CertificateTypeEnum.KNOCK.toString()
                        : CertificateTypeEnum.CERT_OTHER.toString();
            }

            @Override
            public void updateMetadata(Map<String, FinderMetaList> map, boolean force) {
                if (getValue() && !force) {
                    return;
                }
                final String selected = selectedValue(lb);
                this.lb.clear();

                final List<FinderMetaList.Element> elements = map.get(this.field).getElement();
                final FinderMetaList metaList = new FinderMetaList();
                for (FinderMetaList.Element element : elements) {
                    if (contains(types, element)) {
                        metaList.getElement().add(element);
                    }
                }
                fillTypeListBox(lb, metaList, selected != null
                        ? selected
                        : this.defaultKey);
                this.cb.setEnabled(this.lb.getItemCount() > 0);
            }
        };
    }

    private boolean contains(ArrayList<Item> types, FinderMetaList.Element element) {
        for (Item type : types) {
            if (type.getValue().equals(element.getKey())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected TableDataModel createDataModel(int view) {
        switch (view) {
            case 0:
                return createBaseModel();
            case 1:
                if (Selector.EDG_RATING.isAllowed()) {
                    return createEdgModel();
                }
                else {
                    return createTypeModel();
                }
            case 2:
                return createTypeModel();
            default:
                return null;
        }
    }

    protected AbstractFinderView createView() {
        return new FinderCERView<>(this);
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    protected Map<String, FinderMetaList> getMetaLists() {
        final CERFinderMetadata result = this.metaBlock.getResult();
        return createMetaMap(result);
    }

    private Map<String, FinderMetaList> createMetaMap(CERFinderMetadata result) {
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        map.put("typeKey", result.getTypeKey()); // $NON-NLS-0$
        map.put(FinderFormKeys.SUBTYPE, result.getSubtype());
        map.put("marketgroups", result.getMarkets()); // $NON-NLS$
        map.put(FinderFormKeys.ISSUER_NAME, Customer.INSTANCE.addCustomerIssuers(result.getIssuername()));
        if (this.subtypes.isEmpty()) {
            final FinderMetaList subtype = result.getSubtype();
            final List<FinderMetaList.Element> elements = subtype.getElement();
            for (FinderMetaList.Element element : elements) {
                this.subtypes.add(new Item(element.getName(), element.getKey()));
            }
        }
        map.put(LEVERAGE_TYPE, result.getLeverageType());
        if (this.leverageTypes.isEmpty()) {
            final FinderMetaList leverageType = result.getLeverageType();
            final List<FinderMetaList.Element> elements = leverageType.getElement();
            for (FinderMetaList.Element element : elements) {
                this.leverageTypes.add(new Item(Renderer.CERT_LEVERAGE_TYPE.render(element.getName()), element.getKey()));
            }
        }
        return map;
    }

    private TableDataModel createBaseModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.BASE_ROW_MAPPER);
    }

    private TableDataModel createTypeModel() {
        final String selectedValue = this.typeOption.getSelectedValue();
        if (StringUtil.hasText(selectedValue)) {
            final CertificateTypeEnum selectedType = CertificateTypeEnum.valueOf(selectedValue);
            if (this.typeDataModelMap.containsKey(selectedType)) {
                return this.typeDataModelMap.get(selectedType).loadData();
            }
        }
        return this.typeDataModelMap.get(isLeverage()
                ? CertificateTypeEnum.KNOCK
                : CertificateTypeEnum.CERT_OTHER).loadData();
    }

    private TableDataModel createDiscountModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.DISCOUNT_ROW_MAPPER);
    }

    private TableDataModel createBonusModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.BONUS_ROW_MAPPER);
    }

    private TableDataModel createIndexModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.INDEX_ROW_MAPPER);
    }

    private TableDataModel createReverseConvertibleModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.REVERSE_CONV_ROW_MAPPER);
    }

    private TableDataModel createOutperformanceModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.OUTPERF_ROW_MAPPER);
    }

    private TableDataModel createSprintModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.SPRINT_ROW_MAPPER);
    }

    private TableDataModel createExpressModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.EXPRESS_ROW_MAPPER);
    }

    private TableDataModel createKnockoutModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.KNOCKOUT_ROW_MAPPER);
    }

    private TableDataModel createBasketModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.BASKET_ROW_MAPPER);
    }

    private TableDataModel createGuaranteeModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.GUARANTEE_ROW_MAPPER);
    }

    private TableDataModel createOtherModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.OTHER_ROW_MAPPER);
    }

    private TableDataModel createEdgModel() {
        return createModel(getResult().getElement(), CERFinderElementMapper.EDG_ROW_MAPPER);
    }

    public int getTypeCount() {
        return this.types.size();
    }

    ArrayList<Item> getTypes() {
        return this.types;
    }

    @Override
    protected void beforeSearch() {
        if (this.typeOption == null) {
            return;
        }

        final String selectedType = this.typeOption.getSelectedValue();
        for (FinderSection section : typeSpecificSections) {
            boolean correspondingCategoryIsSelected = this.typeOption.isEnabled() && selectedType.equals(section.id);
            section.setSilent(!correspondingCategoryIsSelected);
            if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled()) {
                if (correspondingCategoryIsSelected) {
                    section.show();
                }
                else {
                    section.hide();
                }
            }
        }
    }

    @Override
    protected void onResult() {
        super.onResult();
        if (this.typeOption == null) {
            return;
        }
        final String selectedType = this.typeOption.getSelectedValue();
        if (selectedType == null) {
            return;
        }
        else if (selectedType.equals(this.currentType)) {
            return;
        }
        this.currentType = selectedType;
        ((LiveFinderForm) ff).updateMetadata(getLiveMetaLists(), false);

    }

    public String getSelectedValue() {
        return typeOption.getSelectedValue();
    }

    public boolean isLeverage() {
        return leverage == LeverageConfig.LEV;
    }

    public TypeFinderLinkListener getLinkListener() {
        return linkListener;
    }
}