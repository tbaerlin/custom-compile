/*
 * MscTicks.java
 *
 * Created on 08.03.13 10:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.MarketTickFields;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;

import static de.marketmaker.istar.common.featureflags.FeatureFlags.Flag.VWD_RIMPAR_MSC_TICKDATA;
import static de.marketmaker.istar.domain.data.TickImpl.Type.*;
import static de.marketmaker.istar.merger.context.RequestContextHolder.getRequestContext;

/**
 * Replacement for MscTickData, uses improved template(s) and allows to query additional tick fields.
 * @author oflege
 */
public class MscTicks extends MscTickData {
    public static class Command extends MscTickData.Command {
        private boolean ignoreUnpermissionedFields;

        private String[] field;

        /**
         * Name or number of vwd feed field(s) that are also stored tick-by-tick. These fields will
         * be returned for each tick of the specified <tt>tickType</tt>; if no tickType is specified, these
         * fields will be returned for each tick in which they occured.
         * <br><b>Note: </b>Ignored if aggregated ticks are requested.<p>
         * Data may be available for the following fields:
         * <ul>
         * <li>ADF_Anfang</li>
         * <li>ADF_Anzahl_Handel</li>
         * <li>ADF_Auktion</li>
         * <li>ADF_Auktion_Umsatz</li>
         * <li>ADF_Ausgabe</li>
         * <li>ADF_Indicative_Price</li>
         * <li>ADF_Indicative_Qty</li>
         * <li>ADF_Kassa</li>
         * <li>ADF_Kassa_Kurszusatz</li>
         * <li>ADF_Mittelkurs</li>
         * <li>ADF_NAV</li>
         * <li>ADF_Notierungsart</li>
         * <li>ADF_Open_Interest</li>
         * <li>ADF_Rendite</li>
         * <li>ADF_Rendite_Brief</li>
         * <li>ADF_Rendite_Geld</li>
         * <li>ADF_Rendite_ISMA</li>
         * <li>ADF_Ruecknahme</li>
         * <li>ADF_Schluss</li>
         * <li>ADF_Settlement</li>
         * <li>ADF_Tageshoch</li>
         * <li>ADF_Tagestief</li>
         * <li>ADF_Umsatz_gesamt</li>
         * <li>ADF_Volatility</li>
         * </ul>
         * For symbols at market <tt>LME</tt>, data may also be available for these additional fields:
         * <ul>
         * <li>ADF_Benchmark
         * <li>ADF_Interpo_Closing
         * <li>ADF_Official_Ask
         * <li>ADF_Official_Bid
         * <li>ADF_Prov_Evaluation
         * <li>ADF_Unofficial_Ask
         * <li>ADF_Unofficial_Bid
         * <li>ADF_VWAP
         * </ul>
         * @sample ADF_Rendite
         */
        public String[] getField() {
            return field;
        }

        public void setField(String[] field) {
            if (field.length == 1 && field[0].contains(",")) {
                this.field = field[0].split(",");
            }
            else {
                this.field = field;
            }
        }

        @Override
        protected TickImpl.Type getDefaultTickType() {
            return (field != null) ? ADDITIONAL_FIELDS : super.getDefaultTickType();
        }

        @MmInternal
        public void setIgnoreUnpermissionedFields(boolean ignoreUnpermissionedFields) {
            this.ignoreUnpermissionedFields = ignoreUnpermissionedFields;
        }
    }

    private static final EnumSet<TickImpl.Type> TICK_TYPES_WITH_ADDITIONAL_FIELDS =
            EnumSet.of(TRADE, ASK, BID, BID_ASK, BID_ASK_TRADE, ADDITIONAL_FIELDS);

    public MscTicks() {
        super(Command.class);
    }

    protected String getTemplateName(boolean aggregated) {
        if (aggregated && getRequestContext().isEnabled(VWD_RIMPAR_MSC_TICKDATA)) {
            // Rimpar is using this block with response of type MSC_TickData
            return super.getTemplateName(true);
        }
        return "mscticks";
    }

    protected BitSet fieldsAsBitSet(MscTickData.Command c, BitSet allowedFields, Quote quote) {
        final Command cmd = (Command) c;

        if (cmd.getField() == null || cmd.isWithAggregation()
                || !TICK_TYPES_WITH_ADDITIONAL_FIELDS.contains(cmd.getTickType())) {
            return null;
        }

        final List<VwdFieldDescription.Field> notAllowed = new ArrayList<>();

        final BitSet result = new BitSet();
        for (String s : cmd.getField()) {
            VwdFieldDescription.Field f = getFieldForNameOrId(s);
            if (f == null) {
                throw new BadRequestException("Unknown field '" + s + "'");
            }
            if (!allowedFields.get(f.id())) {
                notAllowed.add(f);
                continue;
            }
            final int order = VwdFieldOrder.getOrder(f.id());
            if (order > 0 && MarketTickFields.isTickField(order)) {
                result.set(f.id());
            }
        }
        if (!notAllowed.isEmpty() && !cmd.ignoreUnpermissionedFields) {
            permissionDenied(quote, notAllowed);
        }
        return result.isEmpty() ? null : result;
    }

    private VwdFieldDescription.Field getFieldForNameOrId(String s) {
        if (s.matches("\\d+")) {
            return VwdFieldDescription.getField(Integer.parseInt(s));
        }
        return VwdFieldDescription.getFieldByName(s);
    }
}
