/*
 * UserDataLoader.java
 *
 * Created on 03.02.2009 14:29:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCNamedQuoteElement;
import de.marketmaker.iview.dmxml.MSCProfiledQuoteList;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListLoader implements Command {
    private List<DmxmlContext.Block<MSCProfiledQuoteList>> blocks
            = new ArrayList<DmxmlContext.Block<MSCProfiledQuoteList>>();

    public static final String LIST_PREFIX = "list_"; // $NON-NLS-0$

    private final DmxmlContext context = new DmxmlContext();

    private final String progressMessage;

    public ListLoader(final String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public void execute() {
        AbstractMainController.INSTANCE.updateProgress(progressMessage);
        if (!"true".equals(SessionData.INSTANCE.getGuiDefValue("nolists"))) { // $NON-NLS$
            final JSONWrapper defs = SessionData.INSTANCE.getGuiDefs();
            final Set<String> keys = defs.keySet();
            for (String key : keys) {
                if (!key.startsWith(LIST_PREFIX)) {
                    continue;
                }
                this.blocks.add(createBlock(this.context, key));
            }
        }

        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable throwable) {
                Firebug.error("cannot load lists", throwable);
            }

            public void onSuccess(ResponseType result) {
                SessionData.INSTANCE.setLists(createListsMap(blocks));
                AbstractMainController.INSTANCE.runInitSequence();
            }
        });
    }

    public static DmxmlContext.Block<MSCProfiledQuoteList> createBlock(DmxmlContext context, String key) {
        final DmxmlContext.Block<MSCProfiledQuoteList> block = context.addBlock("MSC_ProfiledQuoteList", key); // $NON-NLS$
        final JSONWrapper jsonList = SessionData.INSTANCE.getGuiDef(key);
        final String[] symbols = getAttributeList(jsonList, "id"); // $NON-NLS$
        if (symbols.length == 0) {
            block.setEnabled(false);
        }
        else {
            block.setParameters("symbol", symbols); // $NON-NLS$
            final String[] strategies = getMarketStrategies(jsonList);
            if (strategies != null) {
                block.setParameters("marketStrategy", strategies); // $NON-NLS$
            }

            final JSONWrapper constituents = jsonList.get("constituents"); // $NON-NLS$
            if (constituents.isBoolean() && constituents.getValue().isBoolean().booleanValue()) {
                block.setParameter("constituents", "true"); // $NON-NLS$
            }
        }
        return block;
    }

    private static String[] getAttributeList(JSONWrapper jsonList, String key) {
        final JSONWrapper jsonElements = jsonList.get("elements"); // $NON-NLS$
        final int elementCount = jsonElements.size();
        final String[] result = new String[elementCount];
        for (int i = 0; i < elementCount; i++) {
            result[i] = jsonElements.get(i).get(key).stringValue();
        }
        return result;
    }

    private static String[] getMarketStrategies(JSONWrapper jsonList) {
        final String[] result = getAttributeList(jsonList, "marketStrategy"); // $NON-NLS$
        if (!StringUtil.hasText(result)) {
            return null;
        }
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                result[i] = "";
            }
        }
        return result;
    }

    private static Map<String, JSONWrapper> createMapJsonElements(JSONWrapper jsonList) {
        final JSONWrapper jsonElements = jsonList.get("elements"); // $NON-NLS$
        final int elementCount = jsonElements.size();
        final HashMap<String, JSONWrapper> map = new HashMap<String, JSONWrapper>(elementCount * 4 / 3 + 1);
        for (int i = 0; i < elementCount; i++) {
            final JSONWrapper jsonElement = jsonElements.get(i);
            map.put(jsonElement.get("id").stringValue(), jsonElement); // $NON-NLS$
        }
        return map;
    }

    public static Map<String, List<QuoteWithInstrument>> createListsMap(List<DmxmlContext.Block<MSCProfiledQuoteList>> blocks) {
        final Map<String, List<QuoteWithInstrument>> m = new HashMap<String, List<QuoteWithInstrument>>();
        for (DmxmlContext.Block<MSCProfiledQuoteList> block : blocks) {
            m.put(block.getId(), collectQuotes(block));
        }
        return m;
    }

    private static List<QuoteWithInstrument> collectQuotes(DmxmlContext.Block<MSCProfiledQuoteList> block) {
        if (!block.isEnabled()) {
            return Collections.emptyList();
        }

        if (!block.isResponseOk()) {
            Firebug.log("BAD BLOCK: " + block.getKey() + " / " + block.getId()); // $NON-NLS$
            Firebug.log("ErrorCode: " + block.getError().getCode()); // $NON-NLS$
            Firebug.log("ErrorDescription: " + block.getError().getDescription()); // $NON-NLS$
            return Collections.emptyList();
        }

        final List<MSCNamedQuoteElement> listElements = block.getResult().getElement();
        final List<QuoteWithInstrument> result = new ArrayList<QuoteWithInstrument>(listElements.size());
        final Map<String, JSONWrapper> mapJsonElements = createMapJsonElements(SessionData.INSTANCE.getGuiDef(block.getId()));
        for (MSCNamedQuoteElement e : listElements) {
            final JSONWrapper jsonElement = mapJsonElements.get(e.getRequestSymbol());
            jsonElement.getValue().isObject().put("symbol", new JSONString(e.getQuotedata().getQid())); // $NON-NLS$
            result.add(new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), jsonElement.get("name").stringValue())); // $NON-NLS$
        }
        return result;
    }

    public static Map<String, QuoteWithInstrument> getQuotesByRequestSymbol(DmxmlContext.Block<MSCProfiledQuoteList> block) {
        if (!block.isEnabled()) {
            return Collections.emptyMap();
        }

        if (!block.isResponseOk()) {
            Firebug.log("BAD BLOCK: " + block.getKey() + " / " + block.getId()); // $NON-NLS$
            Firebug.log("ErrorCode: " + block.getError().getCode()); // $NON-NLS$
            Firebug.log("ErrorDescription: " + block.getError().getDescription()); // $NON-NLS$
            return Collections.emptyMap();
        }

        final List<MSCNamedQuoteElement> listElements = block.getResult().getElement();
        final Map<String, JSONWrapper> mapJsonElements = createMapJsonElements(SessionData.INSTANCE.getGuiDef(block.getId()));
        final Map<String, QuoteWithInstrument> result = new HashMap<String, QuoteWithInstrument>(listElements.size() * 4 / 3 + 1);
        for (MSCNamedQuoteElement e : listElements) {
            final JSONWrapper jsonElement = mapJsonElements.get(e.getRequestSymbol());
            result.put(e.getRequestSymbol(), new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), jsonElement.get("name").stringValue())); // $NON-NLS$
        }
        return result;
    }
}
