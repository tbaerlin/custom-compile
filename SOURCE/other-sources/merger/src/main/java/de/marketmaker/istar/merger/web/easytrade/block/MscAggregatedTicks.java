/*
 * MscTicks.java
 *
 * Created on 08.03.13 10:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * Performs aggregation on arbitrary fields stored in tick data.
 *
 * @author oflege
 */
public class MscAggregatedTicks extends AbstractTicksBlock {
    public static class Command extends DefaultSymbolCommand {

        private static final boolean WITH_VOLUME_DEFAULT = false;

        private String baseField;

        private String volumeField;

        private boolean withVolume = WITH_VOLUME_DEFAULT;

        /**
         * Name or number of vwd feed <em>price</em> field to be aggregated<p>
         * Data may be available for the following fields:
         * <ul>
         * <li>ADF_Anfang
         * <li>ADF_Auktion
         * <li>ADF_Auktion_Umsatz
         * <li>ADF_Ausgabe
         * <li>ADF_Bezahlt
         * <li>ADF_Brief
         * <li>ADF_Geld
         * <li>ADF_Indicative_Price
         * <li>ADF_Kassa
         * <li>ADF_Mittelkurs
         * <li>ADF_NAV
         * <li>ADF_Rendite
         * <li>ADF_Rendite_Brief
         * <li>ADF_Rendite_Geld
         * <li>ADF_Rendite_ISMA
         * <li>ADF_Ruecknahme
         * <li>ADF_Schluss
         * <li>ADF_Settlement
         * <li>ADF_Tageshoch
         * <li>ADF_Tagestief
         * <li>ADF_Volatility
         * </ul>
         * For symbols at euronext exchanges, data may also be available for these additional fields:
         * <ul>
         * <li>ADF_Bezahlt_Forerunner
         * <li>ADF_EDSP
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
         * @sample ADF_Bezahlt
         */
        public String getBaseField() {
            return this.baseField;
        }

        public void setBaseField(String baseField) {
            this.baseField = baseField;
        }

        /**
         * Name or number of vwd feed <em>size</em> field used to compute each aggregate's volume<p>
         * Data may be available for the following fields:
         * <ul>
         * <li>ADF_Anzahl_Handel
         * <li>ADF_Bezahlt_Umsatz
         * <li>ADF_Brief_Umsatz
         * <li>ADF_Geld_Umsatz
         * <li>ADF_Indicative_Qty
         * <li>ADF_Open_Interest
         * <li>ADF_Umsatz_gesamt
         * </ul>
         * @sample ADF_Bezahlt
         */
        public String getVolumeField() {
            return volumeField;
        }

        public void setVolumeField(String volumeField) {
            this.volumeField = volumeField;
        }

        /**
         * Whether to include a volume in the aggregated values, default is {@value #WITH_VOLUME_DEFAULT}.
         * The volume is the sum of the value of the field {@link #getVolumeField()} for all ticks
         * in the aggregation. If <tt>withVolume</tt> is <tt>true</tt>
         * but no <tt>volumeField</tt> is specified, the following defaults are used:
         * <table><tr><th>baseField</th><th>volumeField</th></tr>
         * <tr><td>ADF_Bezahlt</td><td>ADF_Bezahlt_Umsatz</td></tr>
         * <tr><td>ADF_Geld</td><td>ADF_Geld_Umsatz</td></tr>
         * <tr><td>ADF_Brief</td><td>ADF_Brief_Umsatz</td></tr>
         * <tr><td><em>other</em></td><td><em>all volumes will be 0</em></td></tr>
         * </table>
         */
        public boolean isWithVolume() {
            return withVolume;
        }

        public void setWithVolume(boolean withVolume) {
            this.withVolume = withVolume;
        }
    }

    public MscAggregatedTicks() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        return null;
    }

    private VwdFieldDescription.Field getFieldForNameOrId(String s) {
        if (s.matches("\\d+")) {
            return VwdFieldDescription.getField(Integer.parseInt(s));
        }
        return VwdFieldDescription.getFieldByName(s);
    }
}
