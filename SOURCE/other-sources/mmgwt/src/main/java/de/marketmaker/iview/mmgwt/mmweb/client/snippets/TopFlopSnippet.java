/*
 * TopFlopSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCTopFlop;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectListMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractContextRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ErrorMessageUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TopFlopSnippet extends AbstractSnippet<TopFlopSnippet, TopFlopSnippetView>
        implements PushRegisterHandler, SymbolSnippet {
    private ArrayList<String> modes;

    private DefaultTableDataModel dtmTop;

    private DefaultTableDataModel dtmFlop;

    public static class Class extends SnippetClass {
        public Class() {
            super("TopFlop", I18n.I.topsAndFlopsSnippetTitle()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new TopFlopSnippet(context, config);
        }
    }

    protected enum Mode {
        CHANGE, VOLUME, CALL_PUT
    }

    protected static final AbstractContextRowMapper<MSCListDetailElement> CALL_PUT_MAPPER = new AbstractContextRowMapper<MSCListDetailElement>() {
        @Override
        public Object[] mapRow(MSCListDetailElement e, List<MSCListDetailElement> list) {
            final Price p = Price.create(e);
            final QuoteWithInstrument qwi = toQwi(e).withHistoryContext(
                    ItemListContext.createForPortrait(e, list, getContextName())
            );
            return new Object[]{
                    SessionData.INSTANCE.isAnonymous() ? qwi.getName() : qwi,
                    p,
                    qwi,
                    qwi
            };
        }
    };

    protected static final AbstractContextRowMapper<MSCListDetailElement> MAPPER = new AbstractContextRowMapper<MSCListDetailElement>() {
        @Override
        public Object[] mapRow(MSCListDetailElement e, List<MSCListDetailElement> list) {
            Price p = Price.create(e);
            final QuoteWithInstrument qwi = toQwi(e).withHistoryContext(
                    ItemListContext.createForPortrait(e, list, getContextName())
            );
            return new Object[]{
                    SessionData.INSTANCE.isAnonymous() ? qwi.getName() : qwi,
                    p,
                    p,
                    p
            };
        }
    };

    private static QuoteWithInstrument toQwi(MSCListDetailElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }

    private final MultiViewSupport multiViewSupport;

    protected String type;

    protected ArrayList<String> types;

    private DmxmlContext.Block<MSCTopFlop> block;

    private final boolean titleFromTopFlopIndex;

    private String countFlop;

    private String countTop;

    private AbstractContextRowMapper<MSCListDetailElement> rowMapper = null;

    protected Mode mode;

    private final PriceSupport priceSupport = new PriceSupport(this);

    protected TopFlopSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MSC_TopFlop"); // $NON-NLS-0$

        final String[] viewNames = config.getArray("viewNames"); // $NON-NLS-0$
        if (viewNames != null) {
            this.modes = config.getList("modes"); // $NON-NLS-0$
            assert this.modes.size() == viewNames.length;
            this.multiViewSupport = new MultiViewSupport(viewNames, null);
            this.multiViewSupport.addValueChangeHandler(new ValueChangeHandler<Integer>() {
                public void onValueChange(ValueChangeEvent<Integer> e) {
                    setMode(modes.get(e.getValue()));
                    ackParametersChanged();
                }
            });
            setMode(this.modes.get(0));
        }
        else {
            this.multiViewSupport = null;
            setMode(config.getString("mode", "CHANGE")); // $NON-NLS$
        }
        this.titleFromTopFlopIndex = config.getBoolean("titleFromTopFlopIndex", false); // $NON-NLS$
        setView(new TopFlopSnippetView(this));
        onParametersChanged();
    }

    public void configure(Widget triggerWidget) {
        SelectListMenu.configure(this, triggerWidget, new Command() {
            @Override
            public void execute() {
                ackParametersChanged();
            }
        });
    }

    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    IndexedViewSelectionModel getViewSelectionModel() {
        if (this.multiViewSupport != null) {
            return multiViewSupport.getViewSelectionModel();
        }
        return null;
    }

    public void setListid(String symbol, String marketStrategy) {
        getConfiguration().put("listid", symbol); // $NON-NLS-0$
        getConfiguration().put("marketStrategy", marketStrategy); // $NON-NLS-0$
        onParametersChanged();
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        setListid(symbol, null);
    }

    public void setTitleSuffix(String suffix) {
        getConfiguration().put("titleSuffix", suffix); // $NON-NLS-0$
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!block.isResponseOk()) {
            final String errorMsg = ErrorMessageUtil.getMessage(block.getError());
            if (StringUtil.hasText(errorMsg)) {
                getView().update(DefaultTableDataModel.NULL, DefaultTableDataModel.NULL, errorMsg);
            }
            else {
                getView().update(DefaultTableDataModel.NULL, DefaultTableDataModel.NULL, "--", "--"); // $NON-NLS-0$ $NON-NLS-1$
            }
            return;
        }

        final MSCTopFlop topFlop = this.block.getResult();
        final int countTop = Integer.parseInt(this.countTop);
        final int countFlop = Integer.parseInt(this.countFlop);

        final IdentifierData index = topFlop.getIndex();
        final String indexName;
        if (index != null) {
            if (this.titleFromTopFlopIndex) {
                setTitleSuffix(index.getInstrumentdata().getName());
            }
            indexName = topFlop.getIndex().getInstrumentdata().getName();
        }
        else {
            indexName = "";
        }
        this.dtmTop = getTableDataModel(topFlop.getElement(), 0, countTop, "Tops " + indexName); // $NON-NLS$
        this.dtmFlop = getTableDataModel(topFlop.getElement(), countTop, countFlop, "Flops " + indexName); // $NON-NLS$
        getView().update(this.dtmTop, this.dtmFlop, topFlop.getNumUp(), topFlop.getNumDown());
        this.priceSupport.activate();
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        final String listid = config.getString("listid"); // $NON-NLS-0$
        this.block.setParameter("listid", listid); // $NON-NLS-0$
        this.block.setParameter("marketStrategy", config.getString("marketStrategy")); // $NON-NLS-0$ $NON-NLS-1$

        this.countTop = config.getString("countTop", "5"); // $NON-NLS-0$ $NON-NLS-1$
        this.countFlop = config.getString("countFlop", "5"); // $NON-NLS-0$ $NON-NLS-1$

        this.block.setParameter("countTop", this.countTop); // $NON-NLS-0$
        this.block.setParameter("countFlop", this.countFlop); // $NON-NLS-0$

        this.block.setParameter("sortfield", config.getString("sortfield")); // $NON-NLS-0$ $NON-NLS-1$
    }

    private void setMode(String m) {
        this.mode = Mode.valueOf(m);
        switch (this.mode) {
            case CHANGE:
                getConfiguration().remove("sortfield"); // $NON-NLS-0$
                this.rowMapper = MAPPER;
                break;
            case VOLUME:
                getConfiguration().put("sortfield", "volume"); // $NON-NLS-0$ $NON-NLS-1$
                this.rowMapper = MAPPER;
                break;
            case CALL_PUT:
                getConfiguration().remove("sortfield"); // $NON-NLS-0$
                this.rowMapper = CALL_PUT_MAPPER;
                break;
            default:
                assert false;
                break;
        }
    }

    private DefaultTableDataModel getTableDataModel(List<MSCListDetailElement> elements, int offset, int count, String contextTitle) {
        if (elements.isEmpty()) {
            return DefaultTableDataModel.NULL;
        }
        return DefaultTableDataModel.create(elements, this.rowMapper
                        .withContextName(contextTitle).withAllElements(elements.subList(offset, Math.min(offset + count, elements.size()))),
                offset, count
        );
    }

    public void gotoList() {
        PlaceUtil.fire(HistoryToken.builder("P_IND", this.block.getResult().getIndex().getQuotedata().getQid(), "L") // $NON-NLS$
                .buildEvent()
                .withProperty("marketStrategy", getConfiguration().getString("marketStrategy"))); // $NON-NLS$
    }

    @Override
    public void deactivate() {
        this.priceSupport.deactivate();
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (!event.isPushedUpdate() && this.block.isResponseOk()) {
            final MSCTopFlop topFlop = this.block.getResult();
            getView().update(this.dtmTop, this.dtmFlop, topFlop.getNumUp(), topFlop.getNumDown());
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElement().size()) {
                event.addComponentToReload(this.block, this);
            }
            if (numAdded > 0) {
                return getView().getRenderItems(this.dtmTop, this.dtmFlop);
            }
        }
        return null;
    }
}