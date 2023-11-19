package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.CERRatioData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DecimalCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndDataConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StaticDataCERTypeTabConfig.java
 * Created on Nov 5, 2008 3:32:25 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author mloesch
 */
public class StaticDataCERTypeTabConfig implements TableColumnAndDataConfig<StaticDataCER> {
    public static final CellData EMPTY_CELL = new StringCellData("--", true);

    private static final RowData EMPTY_ROW = new RowData("", new StringCellData("&nbsp;", true)); // $NON-NLS$

    abstract class Cert implements TableColumnAndData<StaticDataCER> {
        protected TableColumnModel tabModel;

        protected RowData laufzeitTage;

        protected RowData bezugsverhaeltnis;

        protected RowData bonusertrag;

        protected RowData bonusRendite;

        protected RowData bonusRenditePA;

        protected RowData barriere;

        protected RowData barriereverletzung;

        protected RowData bonusPuffer;

        protected RowData bonusPufferProzent;

        protected RowData partizipationsGrenze;

        protected RowData bonusLevel;

        protected RowData abstandBonusLevel;

        protected RowData abstandBonusLevelProzent;

        protected RowData aufgeld;

        protected RowData aufgeldProzent;

        protected RowData aufgeldPA;

        protected RowData waehrungsgesichert;

        protected RowData kapitalgesichert;

        protected RowData maxAuszahlung;

        protected RowData cap;

        protected RowData maxErtrag;

        protected RowData maxErtragProzent;

        protected RowData maxRenditePA;

        protected RowData discount;

        protected RowData discountProzent;

        protected RowData abstandCap;

        protected RowData abstandCapProzent;

        protected RowData capLevel;

        protected RowData seitwaertsertragProzent;

        protected RowData seitwaertsrenditePA;

        protected RowData outperformance;

        protected RowData erlaubterRueckgangProzent;

        protected RowData notwendigePerformanceProzent;

        protected RowData basispreis;

        protected RowData kuponProzent;

        protected RowData abstandBasispreis;

        protected RowData abstandBasispreisProzent;

        protected RowData abstandBarriere;

        protected RowData abstandBarriereProzent;

        protected RowData bewertungsdatum1;

        protected RowData bewertungsdatum2;

        protected RowData bewertungsdatum3;

        protected RowData bewertungsdatum4;

        protected RowData hebel;

        protected RowData stoploss;

        protected RowData performanceProzent;

        protected RowData partizipationsgrenze;

        protected RowData partizipation;

        protected RowData start;

        protected RowData stop;

        protected RowData knockout;

        protected RowData knockin;

        protected RowData barrierActive;

        protected RowData lowerBarrierActive;

        protected RowData upperBarrierActive;

        protected RowData barrierStart;

        protected RowData barrierInactiveSince;

        protected RowData lowerBarrierInactiveSince;

        protected RowData upperBarrierInactiveSince;

        protected boolean lowerBarrierInactive;

        protected boolean upperBarrierInactive;

        protected RowData untereBarriere;

        protected RowData obereBarriere;

        protected RowData abstandUntereBarriere;

        protected RowData abstandUntereBarriereProzent;

        protected RowData abstandObereBarriere;

        protected RowData abstandObereBarriereProzent;

        protected CellData laufzeitTageCell;

        protected CellData bezugsverhaeltnisCell;

        protected CellData bonusertragCell;

        protected CellData bonusRenditeCell;

        protected CellData bonusRenditePACell;

        protected CellData barriereCell;

        protected CellData barriereverletzungCell;

        protected CellData bonusPufferCell;

        protected CellData bonusPufferProzentCell;

        protected CellData partizipationsGrenzeCell;

        protected CellData bonusLevelCell;

        protected CellData abstandBonusLevelCell;

        protected CellData abstandBonusLevelProzentCell;

        protected CellData aufgeldCell;

        protected CellData aufgeldProzentCell;

        protected CellData aufgeldPACell;

        protected CellData waehrungsgesichertCell;

        protected CellData kapitalgesichertCell;

        protected CellData maxAuszahlungCell;

        protected CellData capCell;

        protected CellData maxErtragCell;

        protected CellData maxErtragProzentCell;

        protected CellData maxRenditePACell;

        protected CellData discountCell;

        protected CellData discountProzentCell;

        protected CellData abstandCapCell;

        protected CellData abstandCapProzentCell;

        protected CellData capLevelCell;

        protected CellData seitwaertsertragProzentCell;

        protected CellData seitwaertsrenditePACell;

        protected CellData outperformanceCell;

        protected CellData erlaubterRueckgangProzentCell;

        protected CellData notwendigePerformanceProzentCell;

        protected CellData basispreisCell;

        protected CellData kuponProzentCell;

        protected CellData abstandBasispreisCell;

        protected CellData abstandBasispreisProzentCell;

        protected CellData abstandBarriereCell;

        protected CellData abstandBarriereProzentCell;

        protected CellData bewertungsdatum1Cell;

        protected CellData bewertungsdatum2Cell;

        protected CellData bewertungsdatum3Cell;

        protected CellData bewertungsdatum4Cell;

        protected CellData hebelCell;

        protected CellData stoplossCell;

        protected CellData performanceProzentCell;

        protected CellData partizipationsgrenzeCell;

        protected CellData partizipationCell;

        protected CellData startCell;

        protected CellData stopCell;

        protected CellData knockoutCell;

        protected CellData barrierActiveCell;

        protected CellData lowerBarrierActiveCell;

        protected CellData upperBarrierActiveCell;

        protected CellData barrierStartCell;

        protected CellData knockinLevel;

        protected CellData barrierInactiveSinceCell;

        protected CellData lowerBarrierInactiveSinceCell;

        protected CellData upperBarrierInactiveSinceCell;

        protected CellData untereBarriereCell;

        protected CellData obereBarriereCell;

        protected CellData abstandUntereBarriereCell;

        protected CellData abstandUntereBarriereProzentCell;

        protected CellData abstandObereBarriereCell;

        protected CellData abstandObereBarriereProzentCell;


        protected Cert() {
            this.tabModel = getColModel();
        }

        public TableColumnModel getTableColumnModel() {
            return tabModel;
        }

        protected TableColumnModel getColModel() {
            return new DefaultTableColumnModel(new TableColumn[]{
                    new TableColumn(I18n.I.type(), 0.3f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                    new TableColumn(I18n.I.value(), 0.18f, new TableCellRenderers.StringRenderer("--", "mm-right")),  // $NON-NLS-0$ $NON-NLS-1$
                    new TableColumn("", 0.04f, new TableCellRenderers.StringRenderer("")), // $NON-NLS-0$ $NON-NLS-1$
                    new TableColumn(I18n.I.type(), 0.3f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                    new TableColumn(I18n.I.value(), 0.18f, new TableCellRenderers.StringRenderer("--", "mm-right"))  // $NON-NLS-0$ $NON-NLS-1$
            });
        }

        private void setCellData(CERDetailedStaticData data, CERRatioData ratios) {
            this.laufzeitTageCell = new StringCellData(getDaysToExpiration(data.getMaturity()));
            this.bezugsverhaeltnisCell = new StringCellData(data.getSubscriptionratioText());
            this.bonusertragCell = new DecimalCellData(Renderer.PRICE23, ratios.getYield(), (ratios.getYield() != null ? " " + ratios.getQuotedata().getCurrencyIso() : ""), CellData.Sorting.ASC);  // $NON-NLS$
            this.bonusRenditeCell = new DecimalCellData(Renderer.PERCENT, ratios.getYieldRelative(), CellData.Sorting.ASC);
            this.bonusRenditePACell = new DecimalCellData(Renderer.PERCENT, ratios.getYieldRelativePerYear(), CellData.Sorting.ASC);
            this.bonusPufferCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapBarrier(), CellData.Sorting.ASC);
            this.bonusPufferProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getUnderlyingToCapRelative(), CellData.Sorting.ASC);
            this.partizipationsGrenzeCell = new DecimalCellData(Renderer.PRICE23, data.getParticipationLevel(), CellData.Sorting.ASC);
            this.bonusLevelCell = new DecimalCellData(Renderer.PRICE23, data.getBonusLevel(), CellData.Sorting.ASC);
            this.abstandBonusLevelCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapBonusLevel(), CellData.Sorting.ASC);
            this.abstandBonusLevelProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getGapBonusLevelRelative(), CellData.Sorting.ASC);
            this.aufgeldCell = new DecimalCellData(Renderer.PRICE23, ratios.getAgio(), CellData.Sorting.ASC);
            this.aufgeldProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getAgioRelative(), CellData.Sorting.ASC);
            this.aufgeldPACell = new DecimalCellData(Renderer.PERCENT, ratios.getAgioRelativePerYear(), CellData.Sorting.ASC);
            this.waehrungsgesichertCell = new StringCellData(data.isQuanto() != null && data.isQuanto() ? I18n.I.yes() : I18n.I.no());
            this.kapitalgesichertCell = new DecimalCellData(Renderer.PERCENT, data.getGuaranteelevel(), CellData.Sorting.ASC);
            this.maxAuszahlungCell = new DecimalCellData(Renderer.PRICE23, data.getRefundMaximum(), CellData.Sorting.ASC);
            this.capCell = new DecimalCellData(Renderer.PRICE23, data.getCap(), CellData.Sorting.ASC);
            this.maxErtragCell = new DecimalCellData(Renderer.PRICE23, ratios.getMaximumYield(), CellData.Sorting.ASC);
            this.maxErtragProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getMaximumYieldRelative(), CellData.Sorting.ASC);
            this.maxRenditePACell = new DecimalCellData(Renderer.PERCENT, ratios.getMaximumYieldRelativePerYear(), CellData.Sorting.ASC);
            this.discountCell = new DecimalCellData(Renderer.PRICE23, ratios.getDiscount(), CellData.Sorting.ASC);
            this.discountProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getDiscountRelative(), CellData.Sorting.ASC);
            this.abstandCapCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapCap(), CellData.Sorting.ASC);
            this.abstandCapProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getGapCapRelative(), CellData.Sorting.ASC);
            this.capLevelCell = new DecimalCellData(Renderer.PERCENT, ratios.getCapLevel(), CellData.Sorting.ASC);
            this.seitwaertsertragProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getUnchangedYieldRelative(), CellData.Sorting.ASC);
            this.seitwaertsrenditePACell = new DecimalCellData(Renderer.PERCENT, ratios.getUnchangedYieldRelativePerYear(), CellData.Sorting.ASC);
            this.outperformanceCell = new DecimalCellData(Renderer.PRICE23, ratios.getOutperformanceValue(), CellData.Sorting.ASC);
            this.erlaubterRueckgangProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getUnderlyingToCapRelative(), CellData.Sorting.ASC);
            this.notwendigePerformanceProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getCapToUnderlyingRelative(), CellData.Sorting.ASC);
            this.basispreisCell = new DecimalCellData(Renderer.PRICE23, data.getStrike(), CellData.Sorting.ASC);
            this.kuponProzentCell = new DecimalCellData(Renderer.PERCENT23, data.getCoupon(), CellData.Sorting.ASC);
            this.abstandBasispreisCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapStrike(), CellData.Sorting.ASC);
            this.abstandBasispreisProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getGapStrikeRelative(), CellData.Sorting.ASC);
            this.abstandBarriereCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapBarrier(), CellData.Sorting.ASC);
            this.abstandBarriereProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getGapBarrierRelative(), CellData.Sorting.ASC);
            this.bewertungsdatum1Cell = new StringCellData(Formatter.LF.formatDateDdMmYyyy(data.getDeadlineDate1(), "--")); // $NON-NLS$
            this.bewertungsdatum2Cell = new StringCellData(Formatter.LF.formatDateDdMmYyyy(data.getDeadlineDate2(), "--")); // $NON-NLS$
            this.bewertungsdatum3Cell = new StringCellData(Formatter.LF.formatDateDdMmYyyy(data.getDeadlineDate3(), "--")); // $NON-NLS$
            this.bewertungsdatum4Cell = new StringCellData(Formatter.LF.formatDateDdMmYyyy(data.getDeadlineDate4(), "--")); // $NON-NLS$
            this.hebelCell = new DecimalCellData(Renderer.PRICE23, ratios.getLeverage(), CellData.Sorting.ASC);
            this.stoplossCell = new DecimalCellData(Renderer.PRICE23, data.getStoploss(), CellData.Sorting.ASC);
            this.performanceProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getPerformanceAlltime(), CellData.Sorting.ASC);
            this.partizipationsgrenzeCell = new DecimalCellData(Renderer.PRICE23, data.getParticipationLevel(), CellData.Sorting.ASC);
            this.partizipationCell = new DecimalCellData(Renderer.PRICE23, data.getParticipationFactor(), CellData.Sorting.ASC);
            this.startCell = new DecimalCellData(Renderer.PRICE23, data.getStartvalue(), CellData.Sorting.ASC);
            this.stopCell = new DecimalCellData(Renderer.PRICE23, data.getStopvalue(), CellData.Sorting.ASC);
            this.knockoutCell = new StringCellData(data.isKnockout() == null ? "--" : data.isKnockout() ? I18n.I.yes() : I18n.I.no());  // $NON-NLS$
            this.abstandUntereBarriereCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapLowerBarrier(), CellData.Sorting.ASC);
            this.abstandUntereBarriereProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getGapLowerBarrierRelative(), CellData.Sorting.ASC);
            this.abstandObereBarriereCell = new DecimalCellData(Renderer.PRICE23, ratios.getGapUpperBarrier(), CellData.Sorting.ASC);
            this.abstandObereBarriereProzentCell = new DecimalCellData(Renderer.PERCENT, ratios.getGapUpperBarrierRelative(), CellData.Sorting.ASC);
            this.knockinLevel = new DecimalCellData(Renderer.PRICE23, data.getBarrier(), " (" + Formatter.LF.formatDateDdMmYyyy(data.getKnockindate(), "--")+")", CellData.Sorting.ASC); // $NON-NLS$

            this.barriereverletzungCell = new StringCellData(Formatter.LF.formatTimestamp(ratios.getDateBarrierReached(), "--"));  // $NON-NLS-0$

            if (data.getCap() != null && data.getFloor() != null) {
                this.barriereCell = new DecimalCellData(Renderer.PRICE23, null, CellData.Sorting.ASC);
                this.barrierActiveCell = EMPTY_CELL;
                this.barrierStartCell = EMPTY_CELL;
                this.barrierInactiveSinceCell = EMPTY_CELL;

                this.untereBarriereCell = new DecimalCellData(Renderer.PRICE23, data.getFloor(), CellData.Sorting.ASC);
                this.lowerBarrierActiveCell = new StringCellData(data.getLowerBarrierDate() == null ? I18n.I.yes() : I18n.I.no());
                this.lowerBarrierInactive = data.getLowerBarrierDate() != null;
                this.lowerBarrierInactiveSinceCell = new StringCellData(lowerBarrierInactive ? Formatter.LF.formatDate(data.getLowerBarrierDate()) : "--"); // $NON-NLS$

                this.obereBarriereCell = new DecimalCellData(Renderer.PRICE23, data.getCap(), CellData.Sorting.ASC);
                this.upperBarrierActiveCell = new StringCellData(data.getUpperBarrierDate() == null ? I18n.I.yes() : I18n.I.no());
                this.upperBarrierInactive = data.getUpperBarrierDate() != null;
                this.upperBarrierInactiveSinceCell = new StringCellData(upperBarrierInactive ? Formatter.LF.formatDate(data.getUpperBarrierDate()) : "--"); // $NON-NLS$
            }
            else {
                this.barriereCell = new DecimalCellData(Renderer.PRICE23, data.getBarrier(), CellData.Sorting.ASC);
                this.barrierActiveCell = new StringCellData(data.isKnockout() == null ? "--" : data.isKnockout() ? I18n.I.no() : I18n.I.yes()); // $NON-NLS$
                this.barrierStartCell = new StringCellData(Formatter.LF.formatDateDdMmYyyy(data.getKnockindate(), "--")); // $NON-NLS$
                this.barrierInactiveSinceCell = new StringCellData(Formatter.LF.formatDate(data.getKnockoutdate()));

                this.untereBarriereCell = new DecimalCellData(Renderer.PRICE23, null, CellData.Sorting.ASC);
                this.lowerBarrierActiveCell = EMPTY_CELL;
                this.lowerBarrierInactive = data.getLowerBarrierDate() != null;
                this.lowerBarrierInactiveSinceCell = EMPTY_CELL;

                this.obereBarriereCell = new DecimalCellData(Renderer.PRICE23, null, CellData.Sorting.ASC);
                this.upperBarrierActiveCell = EMPTY_CELL;
                this.upperBarrierInactive = data.getUpperBarrierDate() != null;
                this.upperBarrierInactiveSinceCell = EMPTY_CELL;
            }
        }

        protected void setData(CERDetailedStaticData data, CERRatioData ratios) {
            setCellData(data, ratios);

            this.laufzeitTage = new RowData(I18n.I.daysToExpiration(), this.laufzeitTageCell);
            this.bezugsverhaeltnis = new RowData(I18n.I.subscriptionRatio(), this.bezugsverhaeltnisCell);
            this.bonusertrag = new RowData(I18n.I.bonusProfit(), this.bonusertragCell);
            this.bonusRendite = new RowData(I18n.I.bonusYield() + " %", this.bonusRenditeCell);  // $NON-NLS$
            this.bonusRenditePA = new RowData(I18n.I.yieldRelativePercentPerYearAbbr(), this.bonusRenditePACell);
            this.barriereverletzung = new RowData(I18n.I.dateBarrierReached(), this.barriereverletzungCell);
            this.bonusPuffer = new RowData(I18n.I.bonusBuffer(), this.bonusPufferCell);
            this.bonusPufferProzent = new RowData(I18n.I.bonusBuffer() + " %", this.bonusPufferProzentCell); // $NON-NLS$
            this.partizipationsGrenze = new RowData(I18n.I.participationLevelCap(), this.partizipationsGrenzeCell);
            this.bonusLevel = new RowData(I18n.I.bonusLevel(), this.bonusLevelCell);
            this.abstandBonusLevel = new RowData(I18n.I.gapBonusLevelAbbr(), this.abstandBonusLevelCell);
            this.abstandBonusLevelProzent = new RowData(I18n.I.gapBonusLevelAbbr() + " %", this.abstandBonusLevelProzentCell); // $NON-NLS$
            this.aufgeld = new RowData(I18n.I.agio(), this.aufgeldCell);
            this.aufgeldProzent = new RowData(I18n.I.agio() + " %", this.aufgeldProzentCell); // $NON-NLS$
            this.aufgeldPA = new RowData(I18n.I.agioRelativePerYearAbbr(), this.aufgeldPACell);
            this.waehrungsgesichert = new RowData(I18n.I.isQuanto(), this.waehrungsgesichertCell);
            this.kapitalgesichert = new RowData(I18n.I.guaranteelevel(), this.kapitalgesichertCell);
            this.maxAuszahlung = new RowData(I18n.I.refundMaximum(), this.maxAuszahlungCell);
            this.cap = new RowData(I18n.I.cap(), this.capCell);
            this.maxErtrag = new RowData(I18n.I.maximumYield(), this.maxErtragCell);
            this.maxErtragProzent = new RowData(I18n.I.maximumYieldPercentAbbr(), this.maxErtragProzentCell);
            this.maxRenditePA = new RowData(I18n.I.maximumYieldRelativePerYearAbbr(), this.maxRenditePACell);
            this.discount = new RowData(I18n.I.discount(), this.discountCell);
            this.discountProzent = new RowData(I18n.I.discount() + " %", this.discountProzentCell); // $NON-NLS$
            this.abstandCap = new RowData(I18n.I.gapCap(), this.abstandCapCell);
            this.abstandCapProzent = new RowData(I18n.I.gapCap() + " %", this.abstandCapProzentCell); // $NON-NLS$
            this.capLevel = new RowData(I18n.I.capLevelPercent(), this.capLevelCell);
            this.seitwaertsertragProzent = new RowData(I18n.I.unchangedYieldPercentAbbr(), this.seitwaertsertragProzentCell);
            this.seitwaertsrenditePA = new RowData(I18n.I.unchangedYieldRelativePerYear(), this.seitwaertsrenditePACell);
            this.outperformance = new RowData(I18n.I.outperformanceValue(), this.outperformanceCell);
            this.erlaubterRueckgangProzent = new RowData(I18n.I.underlyingToCap() + " %", this.erlaubterRueckgangProzentCell); // $NON-NLS$
            this.notwendigePerformanceProzent = new RowData(I18n.I.capToUnderlying() + " %", this.notwendigePerformanceProzentCell); // $NON-NLS$
            this.basispreis = new RowData(I18n.I.strike(), this.basispreisCell);
            this.kuponProzent = new RowData(I18n.I.couponIRPercent(), this.kuponProzentCell);
            this.abstandBasispreis = new RowData(I18n.I.gapStrike(), this.abstandBasispreisCell);
            this.abstandBasispreisProzent = new RowData(I18n.I.gapStrike() + " %", this.abstandBasispreisProzentCell); // $NON-NLS$
            this.abstandBarriere = new RowData(I18n.I.gapToBarrier(), this.abstandBarriereCell);
            this.abstandBarriereProzent = new RowData(I18n.I.gapToBarrier() + " %", this.abstandBarriereProzentCell); // $NON-NLS$
            this.bewertungsdatum1 = new RowData(I18n.I.deadlineDate(1), this.bewertungsdatum1Cell);
            this.bewertungsdatum2 = new RowData(I18n.I.deadlineDate(2), this.bewertungsdatum2Cell);
            this.bewertungsdatum3 = new RowData(I18n.I.deadlineDate(3), this.bewertungsdatum3Cell);
            this.bewertungsdatum4 = new RowData(I18n.I.deadlineDate(4), this.bewertungsdatum4Cell);
            this.hebel = new RowData(I18n.I.leverage(), this.hebelCell);
            this.stoploss = new RowData(I18n.I.stopLoss(), this.stoplossCell);
            this.performanceProzent = new RowData(I18n.I.performanceAlltimePercentAbbr(), this.performanceProzentCell);
            this.partizipationsgrenze = new RowData(I18n.I.participationLevelCap(), this.partizipationsgrenzeCell);
            this.partizipation = new RowData(I18n.I.participationFactor(), this.partizipationCell);
            this.start = new RowData(I18n.I.startvalueStrike(), this.startCell);
            this.stop = new RowData(I18n.I.stopvalueCap(), this.stopCell);
            this.knockout = new RowData(I18n.I.isKnockout(), this.knockoutCell);
            this.knockin = new RowData(I18n.I.knockinLevel() + " (" + I18n.I.activeFrom() + ")", this.knockinLevel); // $NON-NLS$

            this.barriere = new RowData(I18n.I.barrier(), this.barriereCell);
            this.barrierActive = new RowData(I18n.I.barrierIntact(), this.barrierActiveCell);
            this.barrierStart = new RowData(I18n.I.knockinDate(), this.barrierStartCell);
            this.barrierInactiveSince = new RowData(I18n.I.knockoutDate(), this.barrierInactiveSinceCell);

            this.untereBarriere = new RowData(I18n.I.floor(), this.untereBarriereCell);
            this.lowerBarrierActive = new RowData(I18n.I.lowerBarrierIntact(), this.lowerBarrierActiveCell);
            this.lowerBarrierInactiveSince = new RowData(I18n.I.lowerBarrierInactiveDate(), this.lowerBarrierInactiveSinceCell);

            this.obereBarriere = new RowData(I18n.I.capUpperBarrier(), this.obereBarriereCell);
            this.upperBarrierActive = new RowData(I18n.I.upperBarrierIntact(), this.upperBarrierActiveCell);
            this.upperBarrierInactiveSince = new RowData(I18n.I.upperBarrierInactiveDate(), this.upperBarrierInactiveSinceCell);

            this.abstandUntereBarriere = new RowData(I18n.I.gapLowerBarrier(), this.abstandUntereBarriereCell);
            this.abstandUntereBarriereProzent = new RowData(I18n.I.gapLowerBarrier() + " %", this.abstandUntereBarriereProzentCell); // $NON-NLS$
            this.abstandObereBarriere = new RowData(I18n.I.gapUpperBarrier(), this.abstandObereBarriereCell);
            this.abstandObereBarriereProzent = new RowData(I18n.I.gapUpperBarrier() + " %", this.abstandObereBarriereProzentCell); // $NON-NLS$
        }
    }

    private String getDaysToExpiration(String date) {
        if (date == null) {
            return "--";
        }
        final long maturity = Formatter.parseDay(date).getTime();
        final long now = new Date().getTime();
        final int diffInDays = (int) ((maturity - now) / 86400 / 1000);
        return I18n.I.nDays(diffInDays);
    }

    class CertGuarantee extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.maxErtragProzent, this.maxRenditePA));
            list.add(RowData.combineRowData(this.partizipation, this.barriereverletzung));
            list.add(RowData.combineRowData(this.cap, this.maxAuszahlung));
            list.add(RowData.combineRowData(this.untereBarriere, this.abstandUntereBarriere));
            list.add(RowData.combineRowData(EMPTY_ROW, this.abstandUntereBarriereProzent));
            list.add(RowData.combineRowData(this.obereBarriere, this.abstandObereBarriere));
            list.add(RowData.combineRowData(EMPTY_ROW, this.abstandObereBarriereProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }
    }

    class CertSprint extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.start, this.stop));
            list.add(RowData.combineRowData(this.partizipation, this.maxAuszahlung));
            list.add(RowData.combineRowData(this.abstandBasispreis, this.abstandBasispreisProzent));
            list.add(RowData.combineRowData(this.maxErtrag, this.maxErtragProzent));
            list.add(RowData.combineRowData(EMPTY_ROW, this.maxRenditePA));
            list.add(RowData.combineRowData(this.outperformance, this.seitwaertsrenditePA));
            list.add(RowData.combineRowData(this.erlaubterRueckgangProzent, this.notwendigePerformanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            return list;
        }

    }

    class CertOutperformance extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.basispreis, this.partizipation));
            list.add(RowData.combineRowData(EMPTY_ROW, this.partizipationsgrenze));
            list.add(RowData.combineRowData(this.abstandBasispreis, this.abstandBasispreisProzent));
            list.add(RowData.combineRowData(this.aufgeld, this.aufgeldProzent));
            list.add(RowData.combineRowData(EMPTY_ROW, this.aufgeldPA));
            list.add(RowData.combineRowData(EMPTY_ROW, this.performanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }
    }

    class CertKnockout extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.basispreis, this.stoploss));
            list.add(RowData.combineRowData(this.hebel, this.knockout));
            list.add(RowData.combineRowData(this.abstandBarriere, this.abstandBarriereProzent));
            list.add(RowData.combineRowData(this.aufgeld, this.aufgeldProzent));
            list.add(RowData.combineRowData(this.performanceProzent, this.aufgeldPA));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            return list;
        }
    }

    class CertIndex extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.maxAuszahlung, EMPTY_ROW));
            list.add(RowData.combineRowData(EMPTY_ROW, this.performanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }
    }

    class CertExpress extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            final CERDetailedStaticData data = sdc.getData();
            setData(data, sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.basispreis, this.abstandBarriere));
            list.add(RowData.combineRowData(this.barriere, this.abstandBarriereProzent));
            list.add(RowData.combineRowData(this.barrierActive, this.bewertungsdatum1));
            list.add(RowData.combineRowData(this.barrierStart, this.bewertungsdatum2));
            list.add(RowData.combineRowData(data.getKnockoutdate() == null ? EMPTY_ROW : this.barrierInactiveSince, this.bewertungsdatum3));
            list.add(RowData.combineRowData(this.kuponProzent, this.bewertungsdatum4));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;

        }
    }

    class CertReverseConvertible extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.basispreis, this.maxAuszahlung));
            list.add(RowData.combineRowData(this.abstandBasispreis, this.abstandBasispreisProzent));
            list.add(RowData.combineRowData(this.knockin, EMPTY_ROW));
            list.add(RowData.combineRowData(this.seitwaertsrenditePA, this.maxErtragProzent));
            list.add(RowData.combineRowData(EMPTY_ROW, this.maxRenditePA));
            list.add(RowData.combineRowData(this.kuponProzent, EMPTY_ROW));
            list.add(RowData.combineRowData(this.erlaubterRueckgangProzent, this.notwendigePerformanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }
    }

    class CertDiscount extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(this.cap, this.maxAuszahlung));
            list.add(RowData.combineRowData(this.maxErtrag, this.maxErtragProzent));
            list.add(RowData.combineRowData(EMPTY_ROW, this.maxRenditePA));
            list.add(RowData.combineRowData(this.discount, this.discountProzent));
            list.add(RowData.combineRowData(this.abstandCap, this.abstandCapProzent));
            list.add(RowData.combineRowData(this.capLevel, this.outperformance));
            list.add(RowData.combineRowData(this.seitwaertsertragProzent, this.seitwaertsrenditePA));
            list.add(RowData.combineRowData(this.erlaubterRueckgangProzent, this.notwendigePerformanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }

    }

    class CertBonus extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            final CERDetailedStaticData data = sdc.getData();
            setData(data, sdc.getRatios());

            return RowData.combineRowData(
                    new RowData[]{
                            this.laufzeitTage, this.bezugsverhaeltnis, this.bonusertrag, this.bonusRendite, this.bonusRenditePA,
                            this.bonusLevel, this.abstandBonusLevel, this.abstandBonusLevelProzent, this.bonusPuffer,
                            this.bonusPufferProzent, this.aufgeld, this.aufgeldPA, this.partizipationsGrenze,
                            this.barriereverletzung,
                    },
                    new RowData[]{
                            this.barriere, this.barrierActive, this.barrierStart,
                            data.getKnockoutdate() == null ? EMPTY_ROW : this.barrierInactiveSince,
                            this.untereBarriere, this.lowerBarrierActive,
                            this.lowerBarrierInactive ? this.lowerBarrierInactiveSince : EMPTY_ROW,
                            this.obereBarriere, this.upperBarrierActive,
                            this.upperBarrierInactive ? this.upperBarrierInactiveSince : EMPTY_ROW,
                            this.maxAuszahlung, this.waehrungsgesichert, this.kapitalgesichert,
                            EMPTY_ROW,
                    });
        }
    }

    class CertOther extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(EMPTY_ROW, this.performanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }
    }

    class CertMBI extends Cert {
        public List<RowData> getRowData(StaticDataCER sdc) {
            List<RowData> list = new ArrayList<RowData>();
            setData(sdc.getData(), sdc.getRatios());
            list.add(RowData.combineRowData(this.laufzeitTage, this.bezugsverhaeltnis));
            list.add(RowData.combineRowData(EMPTY_ROW, this.performanceProzent));
            list.add(RowData.combineRowData(this.waehrungsgesichert, this.kapitalgesichert));
            list.add(RowData.combineRowData(this.barriereverletzung, EMPTY_ROW));
            return list;
        }
    }

    public static final TableColumnAndDataConfig<StaticDataCER> INSTANCE
            = new StaticDataCERTypeTabConfig();

    private final Map<String, TableColumnAndData<StaticDataCER>> configs;

    public TableColumnAndData<StaticDataCER> getTableColumnAndData(String type) {
        if (this.configs.containsKey(type)) {
            return this.configs.get(type);
        }
        Firebug.log("TableColumnAndData getConfig(String type): unknown type: " + type + " in " + this.configs.keySet()); // $NON-NLS-0$ $NON-NLS-1$
        return this.configs.get(CertificateTypeEnum.CERT_OTHER.toString());
    }

    private StaticDataCERTypeTabConfig() {
        this.configs = new HashMap<String, TableColumnAndData<StaticDataCER>>();
        final Cert other = new CertOther();

        this.configs.put(CertificateTypeEnum.CERT_EXPRESS.toString(), new CertExpress());
        this.configs.put(CertificateTypeEnum.CERT_DISCOUNT.toString(), new CertDiscount());
        this.configs.put(CertificateTypeEnum.CERT_GUARANTEE.toString(), new CertGuarantee());
        this.configs.put(CertificateTypeEnum.CERT_BONUS.toString(), new CertBonus());
        this.configs.put(CertificateTypeEnum.CERT_INDEX.toString(), new CertIndex());
        this.configs.put(CertificateTypeEnum.CERT_OUTPERFORMANCE.toString(), new CertOutperformance());
        this.configs.put(CertificateTypeEnum.CERT_OTHER.toString(), other);
        this.configs.put(CertificateTypeEnum.CERT_BASKET.toString(), other);
        this.configs.put(CertificateTypeEnum.CERT_SPRINTER.toString(), new CertSprint());
        this.configs.put(CertificateTypeEnum.KNOCK.toString(), new CertKnockout());
        this.configs.put(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE.toString(), new CertReverseConvertible());
        this.configs.put(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE_COM.toString(), new CertReverseConvertible());
        this.configs.put(CertificateTypeEnum.CERT_MBI.toString(), new CertMBI());
        this.configs.put(CertificateTypeEnum.CERT_FACTOR.toString(), other);
    }
}
