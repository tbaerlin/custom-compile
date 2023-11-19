/*
 * IntEntitlement.java
 *
 * Created on 10.07.2012 09:00:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.profile.SelectorDefinition;
import de.marketmaker.istar.merger.provider.profile.SelectorDefinitionProvider;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Returns for a given (feed) symbol all entitlements of an instrument.
 * @author Markus Dick
 */
public class IntEntitlement extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;
    private EntitlementProvider entitlementProvider;
    private SelectorDefinitionProvider selectorDefinitionProvider;

    public static enum ShowEntitlements {
        BOTH, ENTITLED, NOT_ENTITLED
    }

    public static class Command {
        private String[] symbol;
        private SymbolStrategyEnum symbolStrategy;

        private String market;
        private String marketStrategy;

        private String[] field = new String[] { VwdFieldDescription.ADF_Bezahlt.name() };
        private ShowEntitlements showEntitlements = ShowEntitlements.BOTH;
        private boolean showDescription = false;

        /**
         * @return The symbols of instruments to be checked.
         * @sample 710000, 851399, 623100
         */
        @NotNull
        public String[] getSymbol() {
            return this.symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        /**
         * @return This option defines how the provided symbols are to be interpreted.
         */
        public SymbolStrategyEnum getSymbolStrategy() {
            return this.symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        /**
         * @return  Limits the search results to quotes or instruments with a quote at any of
         *          the given markets.
         */
        public String getMarket() {
            return this.market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        /**
         * @return Name of market strategy used to determine the reference quote for instruments
         *         in the search result; if undefined, a client specific default strategy will be used.
         */
        public String getMarketStrategy() {
            return this.marketStrategy;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        /**
         * @return A list of feed fields by name (e.g. ADF_Bezahlt) or by selektorId (e.g. 80).
         * @sample ADF_Bezahlt
         */
        public String[] getField() {
            return field;
        }

        public void setField(String[] field) {
            this.field = field;
        }

        /**
         * @return Defines which entitlements should be returned for the currently authenticated user.
         * @sample BOTH
         */
        @RestrictedSet("BOTH,ENTITLED,NOT_ENTITLED")
        public ShowEntitlements getShowEntitlements() {
            return showEntitlements;
        }

        public void setShowEntitlements(ShowEntitlements showEntitlements) {
            this.showEntitlements = showEntitlements;
        }

        /**
         * @return True to show the description of the selector.
         */
        @RestrictedSet("true,false")
        public boolean isShowDescription() {
            return showDescription;
        }

        public void setShowDescription(boolean showDescription) {
            this.showDescription = showDescription;
        }
    }

    public IntEntitlement() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o,
                                    BindException errors) throws Exception {

        final Command cmd = (Command)o;
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final String[] symbols = cmd.getSymbol();
        final boolean showDescription = cmd.isShowDescription();

        final List<Quote> defaultQuotes = this.instrumentProvider
                .identifyQuotes(Arrays.asList(symbols), cmd.getSymbolStrategy(),
                        cmd.getMarket(), cmd.getMarketStrategy());

        final List<List<QuoteWrapper>> quoteWrapperLists = new ArrayList<>(symbols.length);
        for (final Quote quote : defaultQuotes) {
            if (quote == null) {
                quoteWrapperLists.add(Collections.<QuoteWrapper>emptyList());
            } else {
                quoteWrapperLists.add(QuoteWrapper.createList(quote, profile));
            }
        }

        // level1: symbols |  level2: quotes | level3: entitlements
        final List<List<List<Entitlement>>> entitlementLists = new ArrayList<>(symbols.length);
        for (final List<QuoteWrapper> quotelist : quoteWrapperLists) {
            List<List<Entitlement>> perQuote = new ArrayList<>(quotelist.size());
            for (final QuoteWrapper quoteWrapper : quotelist) {
                perQuote.add(createEntitlementList(quoteWrapper, profile, cmd));
            }
            entitlementLists.add(perQuote);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("showDescription", showDescription);
        model.put("symbols", cmd.getSymbol());
        model.put("defaultQuotes", defaultQuotes);
        model.put("quoteWrapperLists", quoteWrapperLists);
        model.put("entitlementLists", entitlementLists);
        return new ModelAndView("intentitlement", model);
    }

    private List<Entitlement> createEntitlementList(QuoteWrapper quotewrapper, Profile profile, Command cmd) {
        final VwdFieldDescription.Field[] fields = parseFieldParameters(cmd);
        final ShowEntitlements showEntitlements = cmd.getShowEntitlements();
        final boolean showDescription = cmd.isShowDescription();

        final String symbolVwdFeed = quotewrapper.getQuote().getSymbolVwdfeed();
        if (symbolVwdFeed == null) {
            quotewrapper.setVisible(false);
            return Collections.emptyList();
        }

        final int[] iEnt = entitlementProvider.getEntitlements(symbolVwdFeed);
        final List<Entitlement> ent = new ArrayList<>(iEnt.length);

        for (int selektorId : iEnt) {
            final BitSet bsFields = entitlementProvider.getFields(symbolVwdFeed, selektorId);
            final FieldEntitlement[] fieldEntitlements = new FieldEntitlement[fields.length];
            for (int j = 0, length = fields.length; j < length; j++) {
                fieldEntitlements[j] = new FieldEntitlement(fields[j], bsFields.get(fields[j].id()));
            }

            boolean entitled = profile.isAllowed(Profile.Aspect.PRICE, Integer.toString(selektorId));
            if (isAddEntitlement(showEntitlements, entitled)) {
                ent.add(createEntitlement(selektorId, fieldEntitlements, entitled, showDescription));
            }
        }
        return ent;
    }

    private boolean isAddEntitlement(final ShowEntitlements showEntitlements, final boolean entitled) {
        boolean add = false;
        switch(showEntitlements) {
            case ENTITLED:
                if(entitled) {
                    add = true;
                }
                break;
            case NOT_ENTITLED:
                if(!entitled) {
                    add=true;
                }
                break;
            case BOTH:
            default:
                add = true;
        }
        return add;
    }

    private VwdFieldDescription.Field[] parseFieldParameters(final Command cmd) {
        final String[] cmdFields = cmd.getField();
        final VwdFieldDescription.Field[] fields = new VwdFieldDescription.Field[cmdFields.length];

        for (int i = 0, length = cmdFields.length; i < length; i++) {
            if (cmdFields[i].matches("[0-9]+")) {
                fields[i] = VwdFieldDescription.getField(Integer.parseInt(cmdFields[i]));
            }
            else {
                fields[i] = VwdFieldDescription.getFieldByName(cmdFields[i]);
            }
        }
        return fields;
    }

    private Entitlement createEntitlement(final int selectorId, final FieldEntitlement[] fieldEntitlements,
                                          final boolean entitled, final boolean showDescription) {
        Entitlement entitlement = new Entitlement(selectorId, fieldEntitlements, entitled);
        if (showDescription && selectorDefinitionProvider != null) {
            SelectorDefinition selectorDefinition = selectorDefinitionProvider.getSelectorDefinition(selectorId);
            if(selectorDefinition != null) {
                entitlement.setDescription(selectorDefinition.getDescription());
            }
        }
        return entitlement;
    }

    public EasytradeInstrumentProvider getInstrumentProvider() {
        return instrumentProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public EntitlementProvider getEntitlementProvider() {
        return entitlementProvider;
    }

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public SelectorDefinitionProvider getSelectorDefinitionProvider() {
        return selectorDefinitionProvider;
    }

    public void setSelectorDefinitionProvider(SelectorDefinitionProvider selectorDefinitionProvider) {
        this.selectorDefinitionProvider = selectorDefinitionProvider;
    }

    public static class QuoteWrapper {
        private final Quote quote;
        private boolean isVisible = false;
        private boolean isDefault = false;

        static List<QuoteWrapper> createList(Quote defaultQuote, Profile profile) {
            final Instrument instrument = defaultQuote.getInstrument();
            final List<Quote> allQuotes = instrument.getQuotes();
            final List<Quote> visibleQuotes = ProfiledInstrument.quotesWithPrices(instrument, profile);

            List<QuoteWrapper> result = new ArrayList<>();
            for (Quote quote : allQuotes) {
                QuoteWrapper wrapper = new QuoteWrapper(quote);
                wrapper.setVisible(visibleQuotes.contains(quote));
                wrapper.setDefault(quote.getId() == defaultQuote.getId());
                result.add(wrapper);
            }
            return result;
        }

        QuoteWrapper(Quote quote) {
            this.quote = quote;
        }

        public Quote getQuote() {
            return quote;
        }

        public boolean isVisible() {
            return isVisible;
        }

        void setVisible(boolean isVisible) {
            this.isVisible = isVisible;
        }

        public boolean isDefault() {
            return isDefault;
        }

        void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public String getTagname() {
            if (!isVisible) {
                return "invisibleQuotedata";
            }
            if (isDefault) {
                return "defaultQuotedata";
            }
            return "quotedata";
        }
    }

    public static class FieldEntitlement {
        private final VwdFieldDescription.Field field;
        private final boolean inFieldGroup;

        public FieldEntitlement(VwdFieldDescription.Field field, boolean inFieldGroup) {
            this.field = field;
            this.inFieldGroup = inFieldGroup;
        }

        public int getId() {
            return field.id();
        }

        public String getName() {
            return field.name();
        }

        public VwdFieldDescription.Field getField() {
            return field;
        }

        public boolean isInFieldGroup() {
            return inFieldGroup;
        }
    }

    public static class Entitlement {
        private final int id;
        private final String selector;
        private final FieldEntitlement[] fieldEntitlements;
        private final boolean entitled;
        private String description;

        public Entitlement(int id, FieldEntitlement[] fieldEntitlements, boolean entitled) {
            this.id = id;
            final String selector = EntitlementsVwd.toEntitlement(this.id);
            this.selector = String.valueOf(id).equals(selector) ? null : selector;
            this.entitled = entitled;
            this.fieldEntitlements = fieldEntitlements;
        }

        public int getId() {
            return id;
        }

        public String getSelector() {
            return selector;
        }

        public boolean isEntitled() {
            return entitled;
        }

        public String toString() {
            return EntitlementsVwd.toEntitlement(this.id);
        }

        public FieldEntitlement[] getFieldEntitlements() {
            return fieldEntitlements;
        }

        public boolean isWithFields() {
            if (this.fieldEntitlements == null) {
                return false;
            }
            for (FieldEntitlement fieldEntitlement : fieldEntitlements) {
                if (fieldEntitlement.isInFieldGroup()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @return the description of this selector -- if any.
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description the description of this selector.
         */
        public void setDescription(String description) {
            this.description = description;
        }
    }

}
