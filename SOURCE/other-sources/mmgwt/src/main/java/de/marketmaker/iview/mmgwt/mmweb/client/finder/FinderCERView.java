/*
 * FinderCERView.java
 *
 * Created on 11.06.2008 13:43:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderCERView<F extends LiveFinderCER> extends AbstractFinderView<F> {

    private Map<CertificateTypeEnum, Integer> typeColumnModelMap;

    FinderCERView(F controller) {
        super(controller);
    }

    protected int getSelectedView() {
        final int n = super.getSelectedView();
        if (n == 0) {
            return 0; // BaseView, the same across all types
        }
        if (Selector.EDG_RATING.isAllowed() && n == 1) {
            return 1;
        }
        final String selectedTypeId = controller.getSelectedValue();
        if (selectedTypeId == null) {
            return this.typeColumnModelMap.get(controller.isLeverage()
                    ? CertificateTypeEnum.KNOCK
                    : CertificateTypeEnum.CERT_OTHER);
        }
        final CertificateTypeEnum selectedType = CertificateTypeEnum.valueOf(selectedTypeId);
        if (!this.typeColumnModelMap.containsKey(selectedType)) {
            return this.typeColumnModelMap.get(controller.isLeverage()
                    ? CertificateTypeEnum.KNOCK
                    : CertificateTypeEnum.CERT_OTHER);
        }
        return this.typeColumnModelMap.get(selectedType);
    }

    protected int getViewCount() {
        return controller.getTypeCount() + 2;
    }

    protected ArrayList<FinderFormElements.Item> getTypes() {
        return controller.getTypes();
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        if (this.typeColumnModelMap == null) {
            this.typeColumnModelMap = new HashMap<>();
        }

        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());

        final TableColumn wknColumn = new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck); // $NON-NLS-0$
        final TableColumn isinColumn = new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck); // $NON-NLS-0$
        final TableColumn nameColumn = new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name");  // $NON-NLS-0$
        final TableColumn yieldRelativePerYear = new TableColumn(I18n.I.yieldPerYearAbbr(), -1f, PERCENT, "yieldRelativePerYear");  // $NON-NLS-0$
        final TableColumn maximumYieldRelative = new TableColumn(I18n.I.maximumYieldPercentAbbr(), -1f, PERCENT, "maximumYieldRelative");  // $NON-NLS-0$
        final TableColumn maximumYieldRelativePerYear = new TableColumn(I18n.I.maximumYieldRelativePerYearAbbr(), -1f, PERCENT, "maximumYieldRelativePerYear");  // $NON-NLS-0$
        final TableColumn agioRelative = new TableColumn(I18n.I.agioRelative(), -1f, PERCENT, "agioRelative");  // $NON-NLS-0$
        final TableColumn agioRelativePerYear = new TableColumn(I18n.I.agioRelativePerYearAbbr(), -1f, PERCENT, "agioRelativePerYear");  // $NON-NLS-0$
        final TableColumn dateBarrierReached = new TableColumn(I18n.I.barrierReached(), -1f, DATE_RIGHT, "dateBarrierReached");  // $NON-NLS-0$
        final TableColumn barrierOK = new TableColumn(I18n.I.intactBarrier(), -1f).alignCenter();
        final TableColumn cap = new TableColumn(I18n.I.cap(), -1f, PRICE23, "cap");  // $NON-NLS-0$
        final TableColumn performanceAlltime = new TableColumn(I18n.I.performanceAlltimePercentAbbr(), -1f, PERCENT, "performanceAlltime");  // $NON-NLS-0$
        final TableColumn pibColumn = new TableColumn(I18n.I.info(), 20, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI)
                .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.DZ_BANK_USER.isAllowed()));

        final VisibilityCheck dzBankLink = SimpleVisibilityCheck.valueOf(Selector.DZ_BANK_USER.isAllowed());

        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.info(), 20, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI).withVisibilityCheck(dzBankLink),
                wknColumn, isinColumn, nameColumn
                , new TableColumn(I18n.I.issuer(), -1f, "issuername")  // $NON-NLS-0$
                , new TableColumn(I18n.I.productNameIssuer(), -1f)
                , new TableColumn(I18n.I.price(), -1f, PRICE)
                , new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE).alignRight()
                , new TableColumn(I18n.I.time(), -1f, TableCellRenderers.COMPACT_TIME).alignRight()
                , new TableColumn(I18n.I.bid(), -1f, PRICE)
                , new TableColumn(I18n.I.ask(), -1f, PRICE)
                , new TableColumn(I18n.I.marketName(), -1f, TableCellRenderers.STRING_10)
                , new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.STRING_10)
                , new TableColumn(I18n.I.maturity2(), -1f, TableCellRenderers.DATE, "expirationDate")  // $NON-NLS-0$
        });

        columnModels[1] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn,
                new TableColumn(I18n.I.riskClass(), -1f, TableCellRenderers.STRING, "edgTopClass"),  // $NON-NLS-0$
                new TableColumn(I18n.I.rating(), -1f, TableCellRenderers.DEFAULT, "edgTopScore")  // $NON-NLS-0$
        });
        // CERT_DISCOUNT
        columnModels[2] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn, cap
                , new TableColumn(I18n.I.discount(), -1f, PERCENT, "discountRelative")  // $NON-NLS-0$
                , new TableColumn(I18n.I.unchangedYieldPercentAbbr(), -1f, PERCENT, "unchangedYieldRelative")  // $NON-NLS-0$
                , maximumYieldRelative
                , maximumYieldRelativePerYear
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_DISCOUNT, 2);

        // CERT_BONUS
        columnModels[3] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn, cap
                , new TableColumn(I18n.I.bonusProfit(), -1f, PRICE23, "yield")  // $NON-NLS-0$
                , new TableColumn(I18n.I.bonusYield(), -1f, PERCENT, "yieldRelative")  // $NON-NLS-0$
                , new TableColumn(I18n.I.yieldRelativePerYearAbbr(), -1f, PERCENT, "yieldRelativePerYear")  // $NON-NLS-0$
                , new TableColumn(I18n.I.gapBonusLevel(), -1f, PERCENT, "gapBonusLevelRelative")  // $NON-NLS-0$
                , new TableColumn(I18n.I.underlyingToCapRelative(), -1f, PERCENT, "underlyingToCapRelative")  // $NON-NLS-0$
                , agioRelative, agioRelativePerYear, barrierOK
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_BONUS, 3);

        // CERT_INDEX
        columnModels[4] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn, performanceAlltime
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_INDEX, 4);

        // CERT_REVERSE_CONVERTIBLE
        columnModels[5] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn
                , maximumYieldRelativePerYear
                , new TableColumn(I18n.I.unchangedYieldRelativePerYear(), -1f, PERCENT, "unchangedYieldRelativePerYear")  // $NON-NLS-0$
                , new TableColumn(I18n.I.allowedDownturn(), -1f, PERCENT, "underlyingToCapRelative")  // $NON-NLS-0$
                , new TableColumn(I18n.I.necessaryPerformance(), -1f, PERCENT, "capToUnderlyingRelative")  // $NON-NLS-0$
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE, 5);

        // CERT_OUTPERFORMANCE
        columnModels[6] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn
                , new TableColumn(I18n.I.gapStrike1(), -1f, PERCENT, "gapStrikeRelative")  // $NON-NLS-0$
                , new TableColumn(I18n.I.gapCapRelative(), -1f, PERCENT, "gapCapRelative")  // $NON-NLS-0$
                , new TableColumn(I18n.I.participationLevel(), -1f, PERCENT, "participationLevel")  // $NON-NLS-0$
                , agioRelative, agioRelativePerYear
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_OUTPERFORMANCE, 6);

        // CERT_SPRINTER
        columnModels[7] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn, cap
                , new TableColumn(I18n.I.gapCapPercentAbbr(), -1f, PRICE23, "gapCapRelative")  // $NON-NLS-0$
                , maximumYieldRelativePerYear
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_SPRINTER, 7);

        // CERT_EXPRESS
        columnModels[8] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn
                , new TableColumn(I18n.I.gapBarrier(), -1f, PRICE_2, "gapBarrier")  // $NON-NLS-0$
                , new TableColumn(I18n.I.gapBarrierPercentAbbr(), -1f, PERCENT, "gapBarrierRelative")  // $NON-NLS-0$
                , barrierOK
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_EXPRESS, 8);

        // CERT_BASKET
        columnModels[9] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn, yieldRelativePerYear, performanceAlltime
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_BASKET, 9);

        // CERT_GUARANTEE
        columnModels[10] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn, cap, maximumYieldRelativePerYear, dateBarrierReached
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_GUARANTEE, 10);

        // CERT_KNOCKOUT
        columnModels[11] = new DefaultTableColumnModel(new TableColumn[]{
                wknColumn, isinColumn, nameColumn
                , new TableColumn(I18n.I.stopLoss(), -1f, PRICE_2, "stoploss")  // $NON-NLS-0$
                , agioRelative, agioRelativePerYear
                , new TableColumn(I18n.I.gapBarrier(), -1f, PRICE_2, "gapBarrier")  // $NON-NLS-0$
                , new TableColumn(I18n.I.gapBarrierPercentAbbr(), -1f, PERCENT, "gapBarrierRelative")  // $NON-NLS-0$
                , barrierOK
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.KNOCK, 11);

        // CERT_OTHER
        columnModels[12] = new DefaultTableColumnModel(new TableColumn[]{
                pibColumn, wknColumn, isinColumn, nameColumn, yieldRelativePerYear, performanceAlltime
        });
        this.typeColumnModelMap.put(CertificateTypeEnum.CERT_OTHER, 12);

        // From here: Sedex certificate categories
        this.typeColumnModelMap.put(CertificateTypeEnum.CALL, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ESOT_STRUTT, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_BESTIAME, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_ENERGIA, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_INDICE_DI_C, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_LEVERAGED, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_MATERIE_PRI, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_METALLI_IND, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_METALLI_PRE, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_PRODOTTI_AG, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETCS_ETC_LEVERA, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETCS_ETC_SHORT, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETC_SHORT, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETCS_INDEX_COMM, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETCS_INDUSTRIAL, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.ETCS_PRECIOUS_M, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.INV_CERT, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.LEV_CERT_BEAR, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.LEV_CERT_BULL, 0);
        this.typeColumnModelMap.put(CertificateTypeEnum.PUT, 0);
    }
}
