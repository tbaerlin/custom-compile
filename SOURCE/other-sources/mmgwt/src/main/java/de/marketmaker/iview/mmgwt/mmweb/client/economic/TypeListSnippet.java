/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.economic;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.MERItems;
import de.marketmaker.iview.dmxml.MERItemsMetadata;
import de.marketmaker.iview.dmxml.MerItem;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;
import java.util.Set;

/**
 * @author Ulrich Maurer
 */
public class TypeListSnippet extends AbstractSnippet<TypeListSnippet, TypeListSnippetView> implements
        AsyncCallback<ResponseType> {

    public static class Class extends SnippetClass {
        public Class() {
            super("economic.TypeList", I18n.I.value()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new TypeListSnippet(context, config);
        }
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[]{};

    protected DmxmlContext.Block<MERItemsMetadata> blockTypes;

    protected DmxmlContext.Block<MERItems> blockValues;

    private EconomicChartSnippet snippetChart;

    private TypeListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.blockTypes = createBlock("MER_ItemsMetadata"); // $NON-NLS-0$

        this.blockValues = new DmxmlContext().addBlock("MER_Items"); // $NON-NLS-0$

        setView(new TypeListSnippetView(this));
    }

    public void destroy() {
        destroyBlock(this.blockTypes);
    }

    public void onControllerInitialized() {
        this.snippetChart = (EconomicChartSnippet) this.contextController.getSnippet("ec"); // $NON-NLS-0$
    }

    public void setCountries(Set<String> countries) {
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        final String[] country = countries.toArray(EMPTY_STRING_ARRAY);
        this.blockTypes.setParameters("country", country); // $NON-NLS-0$
        this.contextController.reload();
    }

    public void selectType(String type) {
        this.blockValues.setParameters("country", getCountries()); // $NON-NLS-0$
        this.blockValues.setParameter("type", type); // $NON-NLS-0$
        this.blockValues.issueRequest(this);
    }

    private String[] getCountries() {
        final MERItemsMetadata metadata = this.blockTypes.getResult();
        final List<FinderMetaList.Element> listCountries = metadata.getCountry().getElement();
        final String[] result = new String[listCountries.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = listCountries.get(i).getKey();
        }
        return result;
    }

    public void updateView() {
        updateTypes();
    }

    public void updateTypes() {
        if (!this.blockTypes.isResponseOk()) {
            getView().setMessage(I18n.I.noDataAvailable(), false); 
            return;
        }

        final MERItemsMetadata metadata = this.blockTypes.getResult();
        getView().updateTypes(metadata.getType().getElement());

        selectType(getView().getSelectedType());        
    }

    public void onFailure(Throwable throwable) {
        updateValues();
    }

    public void onSuccess(ResponseType responseType) {
        updateValues();
    }

    private void updateValues() {
        if (!this.blockValues.isResponseOk()) {
            getView().setMessage(I18n.I.noDataAvailable(), false); 
            return;
        }

        getView().updateCountries(this.blockValues.getResult().getCountry());
        adaptChart();
    }

    void adaptChart() {
        final List<String> names = getView().getSelectedNames();
        final List<String> symbols = getView().getSelectedSymbols();
//        DebugUtil.logToFirebugConsole(String.valueOf(names));
//        DebugUtil.logToFirebugConsole(String.valueOf(symbols));
//        DebugUtil.logToFirebugConsole(String.valueOf(snippetChart));
        this.snippetChart.setSymbols(names, this.blockValues.getParameter("type"), symbols); // $NON-NLS-0$
    }

    public static String getName(MerItem merItem, String country) {
        String name = merItem.getInstrumentdata().getName();
        name = name.replaceAll(country + "$", "").trim(); // $NON-NLS-0$ $NON-NLS-1$
        name = name.replace(I18n.I.unemployment(), "").trim();  // $NON-NLS-0$
        return StringUtil.hasText(name) ? name : country;
    }
}
