/*
 * SelectorDictionaryReader.java
 *
 * Created on 30.12.13 12:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;

/**
 * @author oflege
 */
class SelectorDictionaryReader {
    private static class NotFilter implements SelectorDictionary.Filter {
        private final SelectorDictionary.Filter delegate;

        protected NotFilter(SelectorDictionary.Filter delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean appliesTo(Object o) {
            return !this.delegate.appliesTo(o);
        }

        @Override
        public String toString() {
            return "!(" + delegate + ")";
        }
    }

    private static class AndFilter implements SelectorDictionary.Filter {
        private final List<SelectorDictionary.Filter> terms = new ArrayList<>(4);

        private SelectorDictionary.Filter rewrite() {
            switch (terms.size()) {
                case 0:
                    return null;
                case 1:
                    return terms.get(0);
                default:
                    return this;
            }
        }

        @Override
        public boolean appliesTo(Object o) {
            for (SelectorDictionary.Filter term : terms) {
                if (!term.appliesTo(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "AndFilter{" + terms + '}';
        }
    }

    private abstract static class QuoteFilter implements SelectorDictionary.Filter {
        @Override
        public boolean appliesTo(Object o) {
            return (o instanceof Quote) && appliesTo((Quote) o);
        }

        protected abstract boolean appliesTo(Quote q);
    }

    private class SectypeQuoteFilter extends QuoteFilter {
        private final int sectypes;

        private SectypeQuoteFilter(Set<String> mdpsSectypes) {
            int tmp = 0;
            for (String mdpsSectype : mdpsSectypes) {
                String s = MdpsTypeMappings.toNumericType(mdpsSectype);
                if (s != null) {
                    int t = Integer.parseInt(s);
                    tmp |= (1 << (t < 30 ? t : 30));
                }
            }
            this.sectypes = tmp;
        }

        @Override
        protected boolean appliesTo(Quote q) {
            final String s = q.getSymbolVwdfeed();
            int t = Integer.parseInt(s.substring(0, s.indexOf('.')));
            return (this.sectypes & (1 << (t < 30 ? t : 30))) != 0;
        }

        @Override
        public String toString() {
            return "SectypeQuoteFilter" + BitSet.valueOf(new long[] { this.sectypes});
        }
    }

    private class VwdcodeQuoteFilter extends QuoteFilter {
        private final Set<String> vwdcodes;

        private VwdcodeQuoteFilter(Set<String> vwdcodes) {
            this.vwdcodes = vwdcodes;
        }

        @Override
        protected boolean appliesTo(Quote q) {
            return this.vwdcodes.contains(q.getSymbolVwdcode());
        }

        @Override
        public String toString() {
            return "VwdcodeQuoteFilter{" + vwdcodes + '}';
        }
    }

    private class MarketQuoteFilter extends QuoteFilter {
        private final Set<String> markets;

        private MarketQuoteFilter(Set<String> markets) {
            this.markets = markets;
        }

        @Override
        protected boolean appliesTo(Quote q) {
            return this.markets.contains(q.getSymbolVwdfeedMarket());
        }

        @Override
        public String toString() {
            return "MarketQuoteFilter{" + markets + '}';
        }
    }


    private final SelectorDictionary.Entry[][] result = new SelectorDictionary.Entry[8][];

    private final int[] numEntries = new int[result.length];

    SelectorDictionaryReader() {
        for (int i = 0; i < result.length; i++) {
            result[i] = new SelectorDictionary.Entry[10];
        }
    }

    private void addEntry(int context, SelectorDictionary.Entry e) {
        if (numEntries[context] == result[context].length) {
            result[context] = Arrays.copyOf(result[context], result[context].length * 2);
        }
        result[context][numEntries[context]++] = e;
    }

    @SuppressWarnings("unchecked")
    SelectorDictionary.Entry[][] read(File f) throws Exception {
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(f);

        final Element root = document.getRootElement();
        List<Element> selectors = root.getChild("selectors").getChildren();
        for (Element selector : selectors) {
            int vwdContextId = Integer.parseInt(selector.getChildText("vwdContextId"));
            if (vwdContextId != 7) {
                continue;
            }

            final int entitlement = parseEntitlement(selector);
            if (entitlement == 0) {
                continue;
            }

            Profile.Aspect aspect = parseAspect(selector);

            List<Element> filters = selector.getChild("definition").getChild("filters").getChildren("filter");
            AndFilter andFilter = new AndFilter();
            for (Element filter : filters) {
                List<Element> items = filter.getChildren("filterCriteriaItem");
                SelectorDictionary.Filter itemsFilter = createCriteriaFilter(items, isInclude(filter));
                andFilter.terms.add(itemsFilter);
            }

            addEntry(vwdContextId, new SelectorDictionary.Entry(entitlement, aspect,
                    getVwdFieldInstanceIds(selector), andFilter.rewrite()));
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.copyOf(result[i], numEntries[i]);
        }
        return result;
    }

    private Profile.Aspect parseAspect(Element selector) {
        String type = selector.getChildText("type");
        switch (type) {
            case "Data":
                return Profile.Aspect.PRICE;
            default:
                throw new IllegalArgumentException("cannot handle type '" + type + "'");
        }
    }

    @SuppressWarnings("unchecked")
    private int[] getVwdFieldInstanceIds(Element selector) {
        List<Element> fgs = selector.getChild("fieldGroups").getChildren("fieldGroup");
        int n = 0;
        for (Element fg : fgs) {
            n += Integer.parseInt(fg.getChild("vwdFieldInstances").getAttributeValue("count"));
        }
        int[] result = new int[n];
        n = 0;
        for (Element fg : fgs) {
            List<Element> fields = fg.getChild("vwdFieldInstances").getChildren("vwdFieldInstanceId");
            for (Element field : fields) {
                result[n++] = Integer.parseInt(field.getText());
            }
        }
        return result;
    }

    private boolean isInclude(Element filter) {
        final String mode = filter.getAttributeValue("mode");
        switch (mode) {
            case "include":
                return true;
            case "exclude":
                return false;
            default:
                throw new IllegalArgumentException("unknown filter mode '" + mode + "'");
        }
    }

    private SelectorDictionary.Filter createCriteriaFilter(List<Element> items, boolean include) {
        AndFilter andFilter = new AndFilter();
        for (Element item : items) {
            SelectorDictionary.Filter f = createFilter(item);
            andFilter.terms.add(f);
        }
        SelectorDictionary.Filter f = andFilter.rewrite();
        return include ? f : new NotFilter(f);
    }

    private SelectorDictionary.Filter createFilter(Element item) {
        final Set<String> valueSet;
        if (item.getChild("valueList") != null) {
            valueSet = asSet(item);
        }
        else {
/* todo: handle Ranges
            if (item.getChild("valueRange") != null) {
                Element lower = item.getChild("valueRange").getChild("lowerLimit");
                Element upper = item.getChild("valueRange").getChild("upperLimit");
            }
*/
            throw new IllegalArgumentException("no valueList defined");
        }

        int vwdFieldInstanceId = Integer.parseInt(item.getChildText("vwdFieldInstanceId"));
        switch (vwdFieldInstanceId) {
            case 102175:
                return new VwdcodeQuoteFilter(valueSet);
            case 102102:
                return new MarketQuoteFilter(valueSet);
            case 102103:
                return new SectypeQuoteFilter(valueSet);
            default:
                throw new IllegalArgumentException("cannot create filter for vwdFieldInstanceId " + vwdFieldInstanceId);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> asSet(Element item) {
        List<Element> values = item.getChild("valueList").getChildren();
        if (values.size() == 1) {
            return Collections.singleton(values.get(0).getText());
        }
        Set<String> result = new HashSet<>();
        for (Element value : values) {
            result.add(value.getText());
        }
        return result;
    }

    private int parseEntitlement(Element selector) {
        Element code = selector.getChild("code");
        switch (code.getAttributeValue("codeType")) {
            case "Numeric":
                return Integer.parseInt(code.getText());
            case "20Z":
                return EntitlementsVwd.toValue(code.getText());
            default:
                return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        SelectorDictionary.Entry[][] read = new SelectorDictionaryReader().read(f);
        System.out.println(Arrays.deepToString(read));
    }
}
