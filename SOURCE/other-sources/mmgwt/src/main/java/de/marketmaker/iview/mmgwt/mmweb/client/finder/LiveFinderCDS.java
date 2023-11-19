package de.marketmaker.iview.mmgwt.mmweb.client.finder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.CDSFinder;
import de.marketmaker.iview.dmxml.CDSFinderElement;
import de.marketmaker.iview.dmxml.CDSFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;


public class LiveFinderCDS extends LiveFinder<CDSFinder, CDSFinderMetadata> {
    public static final LiveFinderCDS INSTANCE = new LiveFinderCDS();

    private static final String BASE_ID = "cdsbase"; // $NON-NLS$

    private LiveFinderCDS() {
        super("CDS_Finder"); // $NON-NLS$
    }

    @Override
    protected ViewSpec[] getResultViewSpec() {
        return new ViewSpec[] {new ViewSpec(I18n.I.result())};
    }

    @Override
    public String getId() {
        return "LCDS";  // $NON-NLS$
    }

    @Override
    public String getViewGroup() {
        return "finder-cds";  // $NON-NLS$
    }

    @Override
    public void prepareFind(String field1, String value1, String field2, String value2) {
    }

    @Override
    protected void addSections() {
        addSectionBase();
    }

    @Override
    protected List<FinderTypedMetaList> getLiveMetadata() {
        return this.block.getResult().getMetadata();
    }

    @Override
    protected AbstractFinderView createView() {
        return new FinderCDSView<>(this);
    }

    @Override
    protected TableDataModel createDataModel(int view) {
        return createModel(addHeader(getResult().getElement()),
            new AbstractRowMapper<CDSFinderElement>() {
                public Object[] mapRow(CDSFinderElement elem) {
                    final QuoteWithInstrument qwi = new QuoteWithInstrument(elem.getInstrumentdata(), elem.getQuotedata());
                    return new Object[]{
                            elem.getIssuername(),
                            qwi,
                            elem.getMaturity(),
                            elem.getDebtRanking(),
                            elem.getIssuerType(),
                            elem.getRestructuringRule(),
                            elem.getCurrency(),
                            elem.getPrice(),
                            elem.getDate(),
                            elem.getChangeNet(),
                            elem.getChangePercent(),
                    };
            }
        });
    }

    private List<CDSFinderElement> addHeader(List<CDSFinderElement> list) {
        if (!this.block.isResponseOk()
                || this.block.getResult().getElement() == null
                || this.block.getResult().getElement().isEmpty()) {
            return list;
        }
        List<CDSFinderElement> result = new ArrayList<>();
        final List<CDSFinderElement> indices = this.block.getResult().getElement();
        for (CDSFinderElement elem : indices) {
            result.add(elem);
        }
        return result;
    }

    @Override
    protected Map<String, FinderMetaList> getMetaLists() {
        final CDSFinderMetadata result = this.metaBlock.getResult();
        final HashMap<String, FinderMetaList> map = new HashMap<>();
        map.put("issuername", result.getIssuername()); // $NON-NLS$
        map.put("maturity", result.getMaturity()); // $NON-NLS$
        map.put("debtRanking", result.getDebtRanking()); // $NON-NLS$
        map.put("issuerType", result.getIssuerType()); // $NON-NLS$
        map.put("restructuringRule", result.getRestructuringRule()); // $NON-NLS$
        map.put("currency", result.getCurrency()); // $NON-NLS$
        return map;
    }

    private void addSectionBase() {

        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(BASE_ID);
            int defaultOrder = 0;
            final Map<String, FinderMetaList> metaLists = getMetaLists();

            final String issuername = FinderFormKeys.ISSUER_NAME;
            final FinderFormElements.LiveMultiEnumOption issuerNameSelectBox =
                    createSortableLiveMultiEnum(issuername, I18n.I.issuer(), null, metaLists, issuername, new DynamicSearchHandler());
            elements.add(issuerNameSelectBox.withConf(sectionConf, defaultOrder++));

            final String currency = "currency"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption currencySelectBox =
                    createSortableLiveMultiEnum(currency, I18n.I.currency(), null, metaLists, currency, new DynamicSearchHandler());
            elements.add(currencySelectBox.withConf(sectionConf, defaultOrder++));

            final String debtRanking = "debtRanking"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption deptRankingSelectBox =
                    createSortableLiveMultiEnum(debtRanking, I18n.I.debtRankings(), null, metaLists, debtRanking, new DynamicSearchHandler());
            elements.add(deptRankingSelectBox.withConf(sectionConf, defaultOrder++));

            final String restructuringRule = "restructuringRule"; // $NON-NLS$
            final FinderFormElements.LiveMultiEnumOption restructuringRuleSelectBox =
                    createSortableLiveMultiEnum(restructuringRule, I18n.I.restructuringRule(), null, metaLists, restructuringRule, new DynamicSearchHandler());
            elements.add(restructuringRuleSelectBox.withConf(sectionConf, defaultOrder++));

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);
        };

        final FinderSection section = addSection(BASE_ID, I18n.I.baseInfo(), false, elementsLoader, searchHandler).expand().withConfigurable().loadElements();
        section.setValue(true);
    }
}
