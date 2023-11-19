/*
 * LiveFinderBNDIssuer.java
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;


import de.marketmaker.iview.dmxml.BNDIssuerFinder;
import de.marketmaker.iview.dmxml.BNDIssuerFinderMetaData;
import de.marketmaker.iview.dmxml.BndIssuerRating;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.LIST_EXPIRATION_DATES;

@NonNLS
@Singleton
public class LiveFinderBNDIssuer extends LiveFinder<BNDIssuerFinder, BNDIssuerFinderMetaData> {
    private static final String RATING_SOURCE_FITCH = "Fitch";

    private static final String RATING_SOURCE_MOODYS = "Moodys";

    private static final String RATING_SOURCE_SNP = "SnP";

    private static final String BASE_ID = "bndissbase";

    private final FeatureFlags featureFlags;

    public enum RatingSection {
        FITCH_ID(RATING_SOURCE_FITCH, RATING_SOURCE_FITCH, FinderBNDIssuerView.FITCH_ROW_MAPPER),
        MOODYS_ID(RATING_SOURCE_MOODYS, RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_CTP("MoodysCtp", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_CTP_B("MoodysCtpBacked", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_SU("MoodysSu", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_SU_B("MoodysSuBacked", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_BDR("MoodysBdr", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_BDR_B("MoodysBdrBacked", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_IFS("MoodysIfs", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        MOODYS_ID_IFS_B("MoodysIfsBacked", RATING_SOURCE_MOODYS, FinderBNDIssuerView.MOODYS_ROW_MAPPER),
        SNP_ID(RATING_SOURCE_SNP, RATING_SOURCE_SNP, FinderBNDIssuerView.SNP_ROW_MAPPER);

        private final String sectionId;
        private final String ratingSource;
        private final RowMapper<BndIssuerRating> rowMapper;

        RatingSection(String sectionId, String ratingSource, RowMapper<BndIssuerRating> rowMapper) {
            this.sectionId = sectionId;
            this.ratingSource = ratingSource;
            this.rowMapper = rowMapper;
        }

    }

    // section ids must match the string returned from the sourceOption.getValueStr()
    // in order to show/hide sections when a source is selected
    private FinderFormElements.LiveListBoxOption sourceOption;

    private final List<FinderSection> sourceSpecificSections = new ArrayList<>();


    @Inject
    public LiveFinderBNDIssuer(FeatureFlags featureFlags) {
        super("BND_IssuerFinder");
        this.featureFlags = featureFlags;
        // hacked for the uppercase D in BND_IssuerFinderMetaData
        context.removeBlock(metaBlock);
        metaBlock = this.context.addBlock("BND_IssuerFinderMetaData");
    }

    @Override
    protected AbstractFinderForm createFinderForm() {
        final AbstractFinderForm finderForm = super.createFinderForm();
        if(finderForm instanceof LiveFinderForm) {
            ((LiveFinderForm) finderForm).setLiveFinderParamPanelWidth(340);
        }
        return finderForm;
    }

    @Override
    protected int getDefaultViewOffset() {
        return 1;
    }

    @Override
    protected ViewSpec[] getResultViewSpec() {
        List<ViewSpec> viewSpec = new ArrayList<>();
        viewSpec.add(new ViewSpec(I18n.I.basis()));
        viewSpec.add(new ViewSpec(I18n.I.extended()));
        return viewSpec.toArray(new ViewSpec[viewSpec.size()]);
    }

    @Override
    public String getId() {
        return "LBNDI";
    }

    @Override
    public String getViewGroup() {
        return "finder-bndi";
    }

    @Override
    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    @Override
    protected TableDataModel createDataModel(int view) {
        RatingSection source = getSourceValue();
        if (view == 0 || source == null) {
            return createModel(getResult().getIssuerRating(), FinderBNDIssuerView.BASE_ROW_MAPPER);
        }
        return createModel(getResult().getIssuerRating(), source.rowMapper);
    }

    public RatingSection getSourceValue() {
        String value = this.sourceOption.getValueStr();
        for (RatingSection source : RatingSection.values()) {
            if (source.sectionId.equals(value)) {
                return source;
            }
        }
        return null;
    }

    @Override
    protected void addSections() {
        addSection(BASE_ID, I18n.I.baseInfo(), false,
                createBaseSection(SectionConfigUtil.getSectionConf(BASE_ID)), this.searchHandler)
                .expand().withConfigurable().loadElements();

        FinderSection fitchSection = addSection(RatingSection.FITCH_ID.sectionId, "Fitch", false,
                createFitchSection(SectionConfigUtil.getSectionConf(RatingSection.FITCH_ID.sectionId)), this.searchHandler)
                .expand().withConfigurable().loadElements();
        this.sourceSpecificSections.add(fitchSection);


        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_CTP.sectionId, "Moody's CTP", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_CTP.sectionId), "", "CTP"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_CTP_B.sectionId, "Moody's CTPB", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_CTP_B.sectionId), "Backed", "CTP Backed"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_SU.sectionId, "Moody's SU", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_SU.sectionId), "Su", "SU"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_SU_B.sectionId, "Moody's SUB", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_SU_B.sectionId), "SuBacked", "SU Backed"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_BDR.sectionId, "Moody's BDR", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_BDR.sectionId), "Bdr", "BDR"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_BDR_B.sectionId, "Moody's BDRB", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_BDR_B.sectionId), "BdrBacked", "BDR Backed"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_IFS.sectionId, "Moody's IFS", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_IFS.sectionId), "Ifsr", "IFS"), this.searchHandler)
                .expand().withConfigurable().loadElements());

        this.sourceSpecificSections.add(addSection(RatingSection.MOODYS_ID_IFS_B.sectionId, "Moody's IFSB", false,
                createMoodysSection(SectionConfigUtil.getSectionConf(RatingSection.MOODYS_ID_IFS_B.sectionId), "IfsrBacked", "IFS Backed"), this.searchHandler)
                    .expand().withConfigurable().loadElements());

        FinderSection snpSection = addSection(RatingSection.SNP_ID.sectionId, "Standard & Poor's", false,
                createSnpSection(SectionConfigUtil.getSectionConf(RatingSection.SNP_ID.sectionId)), this.searchHandler)
                .expand().withConfigurable().loadElements();
        this.sourceSpecificSections.add(snpSection);
    }

    @Override
    protected void beforeSearch() {
        if (!this.sourceOption.isEnabled()) {
            for (FinderSection section : this.sourceSpecificSections) {
                section.setSilent(false);
                section.show();
            }
            return;
        }
        final String ratingSource = this.sourceOption.getValueStr();
        for (FinderSection section : this.sourceSpecificSections) {
            if (belongsSectionToSource(section, ratingSource)) {
                section.setSilent(false);
                section.show();
            }
            else {
                section.setSilent(true);
                section.hide();
            }
        }
    }

    private boolean belongsSectionToSource(FinderSection finderSection, String source) {
        for (RatingSection rs : EnumSet.allOf(RatingSection.class)) {
            if (rs.ratingSource.equals(source) && rs.sectionId.equals(finderSection.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    @Override
    protected AbstractFinderView createView() {
        return new FinderBNDIssuerView<>(this);
    }

    @Override
    protected Map<String, FinderMetaList> getMetaLists() {
        final BNDIssuerFinderMetaData result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        map.put("source", result.getSource());
        map.put("countryIso", result.getCountryIso());
        map.put("currencyIso", result.getCurrencyIso());

        map.put("ratingFitchIssuerLongTerm", result.getRatingFitchIssuerLongTerm());
        map.put("ratingFitchIssuerShortTerm", result.getRatingFitchIssuerShortTerm());
        map.put("ratingFitchIssuerIFS", result.getRatingFitchIssuerIFS());
        map.put("ratingMoodysIssuerLongTerm", result.getRatingMoodysIssuerLongTerm());
        map.put("ratingMoodysIssuerShortTerm", result.getRatingMoodysIssuerShortTerm());
        map.put("ratingMoodysIssuerLongTermBacked", result.getRatingMoodysIssuerLongTermBacked());
        map.put("ratingMoodysIssuerShortTermBacked", result.getRatingMoodysIssuerShortTermBacked());
        map.put("ratingMoodysIssuerLongTermSu", result.getRatingMoodysIssuerLongTermSu());
        map.put("ratingMoodysIssuerShortTermSu", result.getRatingMoodysIssuerShortTermSu());
        map.put("ratingMoodysIssuerLongTermSuBacked", result.getRatingMoodysIssuerLongTermSuBacked());
        map.put("ratingMoodysIssuerShortTermSuBacked", result.getRatingMoodysIssuerShortTermSuBacked());
        map.put("ratingMoodysIssuerLongTermBdr", result.getRatingMoodysIssuerLongTermBdr());
        map.put("ratingMoodysIssuerShortTermBdr", result.getRatingMoodysIssuerShortTermBdr());
        map.put("ratingMoodysIssuerLongTermBdrBacked", result.getRatingMoodysIssuerLongTermBdrBacked());
        map.put("ratingMoodysIssuerShortTermBdrBacked", result.getRatingMoodysIssuerShortTermBdrBacked());
        map.put("ratingMoodysIssuerLongTermIfsr", result.getRatingMoodysIssuerLongTermIfsr());
        map.put("ratingMoodysIssuerShortTermIfsr", result.getRatingMoodysIssuerShortTermIfsr());
        map.put("ratingMoodysIssuerLongTermIfsrBacked", result.getRatingMoodysIssuerLongTermIfsrBacked());
        map.put("ratingMoodysIssuerShortTermIfsrBacked", result.getRatingMoodysIssuerShortTermIfsrBacked());
        map.put("ratingStandardAndPoorsIssuerLongTerm", result.getRatingStandardAndPoorsIssuerLongTerm());
        map.put("ratingStandardAndPoorsIssuerShortTerm", result.getRatingStandardAndPoorsIssuerShortTerm());
        map.put("ratingStandardAndPoorsIssuerLongTermRegulatoryId", result.getRatingStandardAndPoorsIssuerLongTermRegulatoryId());

        return map;
    }

    private DataLoader<List<FinderFormElement>> createBaseSection(final String[] sectionConf) {

        return () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            int defaultOrder = 0;
            final Map<String, FinderMetaList> metaLists = getMetaLists();

            final String source = "source";
            this.sourceOption = new FinderFormElements.LiveListBoxOption(source, source, I18n.I.ratingAgency(),
                    new ArrayList<>(), null, this.searchHandler) {

            };
            elements.add(this.sourceOption.withConf(sectionConf, defaultOrder++));

            final String issuername = FinderFormKeys.ISSUER_NAME;
            final FinderFormElements.AbstractOption issuernameSearch =
                    new FinderFormElements.LiveTextOption(issuername, I18n.I.issuer(), this.searchHandler)
                            .withWidth("190px")
                            .withConf(sectionConf, defaultOrder++);
            elements.add(issuernameSearch);
            this.sortFields.add(new FinderFormElements.Item(I18n.I.name(), "issuername"));

            final FinderFormElements.LiveMultiEnumOption countySelectBox =
                    createLiveMultiEnum("countryIso", I18n.I.country(), null,
                            metaLists, "countryIso", this.searchHandler);
            elements.add(countySelectBox.withConf(sectionConf, defaultOrder++));

            final FinderFormElements.LiveMultiEnumOption currencySelectBox =
                    createLiveMultiEnum("currencyIso", I18n.I.currency(), null,
                            metaLists, "currencyIso", this.searchHandler);
            elements.add(currencySelectBox.withConf(sectionConf, defaultOrder++));

            removeSortFields("countryIso", "currencyIso");
            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };

    }

    protected void removeSortFields(String... fieldIds) {
        final List<String> candidates = Arrays.asList(fieldIds);
        final Iterator<FinderFormElements.Item> iter = this.sortFields.iterator();
        while (iter.hasNext()) {
            final FinderFormElements.Item next = iter.next();
            if (candidates.contains(next.getValue())) {
                iter.remove();
            }
        }
    }


    private DataLoader<List<FinderFormElement>> createFitchSection(final String[] sectionConf) {
        return () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            int defaultOrder = 0;

            elements.add(
                    createLiveFromToBoxOption("ratingFitchIssuerShortTerm", "Shortterm (Fitch)", null, new DynamicSearchHandler())
                        .withSortReverse(true)
                        .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingFitchIssuerLongTerm", "Longterm (Fitch)", null, new DynamicSearchHandler())
                        .withSortReverse(true)
                        .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingFitchIssuerIFS", "IFS (Fitch)", null, new DynamicSearchHandler())
                        .withSortReverse(true)
                        .withConf(sectionConf, defaultOrder++));

            final String ratingFitchIssuerShortTermDate = "ratingFitchIssuerShortTermDate";
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingFitchIssuerShortTermDate, ratingFitchIssuerShortTermDate,
                    "Datum Shortterm ",
                    "",
                    LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            final String ratingFitchIssuerLongTermDate = "ratingFitchIssuerLongTermDate";
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingFitchIssuerLongTermDate, ratingFitchIssuerLongTermDate,
                    "Datum Longterm ",
                    "",
                    LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
    }

    private DataLoader<List<FinderFormElement>> createMoodysSection(final String[] sectionConf) {
        return () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            int defaultOrder = 0;

            elements.add(
                    createLiveFromToBoxOption("ratingMoodysIssuerShortTerm", "Shortterm (Moody's)", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingMoodysIssuerLongTerm", "Longterm (Moody's)", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingMoodysIssuerBFS", "BFS Rating (Moody's)", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));

            final String ratingMoodysIssuerShortTermDate = "ratingMoodysIssuerShortTermDate";
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingMoodysIssuerShortTermDate, ratingMoodysIssuerShortTermDate,
                    I18n.I.date() + " Shortterm ",
                    "", LIST_EXPIRATION_DATES,
                    DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            final String ratingMoodysIssuerLongTermDate = "ratingMoodysIssuerLongTermDate";
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingMoodysIssuerLongTermDate, ratingMoodysIssuerLongTermDate,
                    I18n.I.date() + " Longterm ",
                    "", LIST_EXPIRATION_DATES,
                    DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
    }

    private DataLoader<List<FinderFormElement>> createMoodysSection(final String[] sectionConf, String fieldSuffix, String description) {
        return () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            int defaultOrder = 0;

            final String suffix = fieldSuffix == null ? "" : fieldSuffix;

            elements.add(
                    createLiveFromToBoxOption("ratingMoodysIssuerLongTerm" + suffix, "Moody's Longterm (" + description + ")", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingMoodysIssuerShortTerm" + suffix, "Moody's Shortterm (" + description + ")", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));

            final String ratingMoodysIssuerLongTermDate = "ratingMoodysIssuerLongTermDate" + suffix;
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingMoodysIssuerLongTermDate, ratingMoodysIssuerLongTermDate,
                    I18n.I.date() + " Longterm (" + description + ")",
                    "", LIST_EXPIRATION_DATES,
                    DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            final String ratingMoodysIssuerShortTermDate = "ratingMoodysIssuerShortTermDate" + suffix;
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingMoodysIssuerShortTermDate, ratingMoodysIssuerShortTermDate,
                    I18n.I.date() + " Shortterm (" + description + ")",
                    "", LIST_EXPIRATION_DATES,
                    DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
    }


    private DataLoader<List<FinderFormElement>> createSnpSection(final String[] sectionConf) {
        return () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            int defaultOrder = 0;

            elements.add(
                    createLiveFromToBoxOption("ratingStandardAndPoorsIssuerShortTerm",
                            "Shortterm (S & P)" , null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingStandardAndPoorsIssuerLongTerm",
                            "Longterm (S & P)", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));
            elements.add(
                    createLiveFromToBoxOption("ratingStandardAndPoorsIssuerLongTermRegulatoryId",
                            "Endorsement Indicator (S & P)", null, new DynamicSearchHandler())
                            .withSortReverse(true)
                            .withConf(sectionConf, defaultOrder++));

            final String ratingSnpIssuerShortTermDate = "ratingStandardAndPoorsIssuerShortTermDate";
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingSnpIssuerShortTermDate, ratingSnpIssuerShortTermDate,
                    I18n.I.date() + " Shortterm ",
                    "", LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            final String ratingSnpIssuerLongTermDate = "ratingStandardAndPoorsIssuerLongTermDate";
            elements.add(new FinderFormElements.LiveStartEndOption(
                    ratingSnpIssuerLongTermDate, ratingSnpIssuerLongTermDate,
                    I18n.I.date() + " Longterm ",
                    "", LIST_EXPIRATION_DATES, DateTimeUtil.PeriodMode.PAST, this.searchHandler)
                    .withConf(sectionConf, defaultOrder));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };
    }
}
