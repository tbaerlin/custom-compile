package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.dmxml.BndIssuerRating;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModelBuilder;
import de.marketmaker.iview.tools.i18n.NonNLS;

@NonNLS
class FinderBNDIssuerView<F extends LiveFinderBNDIssuer> extends AbstractFinderView<F> {

    private static final String MOODYS_CTP = "Counterparty";
    private static final String MOODYS_CTP_BACKED = "Counterparty Backed";
    private static final String MOODYS_SU = "Senior Unsecured";
    private static final String MOODYS_SU_BACKED = "Senior Unsecured Backed";
    private static final String MOODYS_BDR = "Bank Deposit";
    private static final String MOODYS_BDR_BACKED = "Bank Deposit Backed";
    private static final String MOODYS_IFS = "Insurance Financial Strength";
    private static final String MOODYS_IFS_BACKED = "Insurance Financial Strength Backed";

    private static final String ST = I18n.I.shortTerm();
    private static final String LT = I18n.I.longTerm();

    private static String date(String term) {
        return term + ' ' + I18n.I.date();
    }

    FinderBNDIssuerView(F controller) {
        super(controller);
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    protected int getSelectedView() {
        final int n = super.getSelectedView();
        if (n == 0) {
            return 0;
        }
        // second view depends on the user-selected source
        LiveFinderBNDIssuer.RatingSection source = controller.getSourceValue();
        if(source != null) {
            switch (source) {
                case FITCH_ID:
                    return 1;
                case MOODYS_ID:
                case MOODYS_ID_CTP:
                case MOODYS_ID_CTP_B:
                case MOODYS_ID_SU:
                case MOODYS_ID_SU_B:
                case MOODYS_ID_BDR:
                case MOODYS_ID_BDR_B:
                case MOODYS_ID_IFS:
                case MOODYS_ID_IFS_B:
                    return 2;
                case SNP_ID:
                    return 3;
                default:
                    return 0;
            }
        }
        return 0;
    }

    @Override
    protected void initColumnModels(TableColumnModel[] columnModels) {
        // common
        columnModels[0] = new DefaultTableColumnModel(new TableColumn[] {
                new TableColumn(I18n.I.ratingAgency(), 10, TableCellRenderers.DEFAULT_28, "source"),
                new TableColumn(I18n.I.name(), 150, TableCellRenderers.DEFAULT_28, "issuername"),
                new TableColumn(I18n.I.country(), 10f, TableCellRenderers.DEFAULT, "countryIso"),
                new TableColumn(I18n.I.currency(), 10f, TableCellRenderers.DEFAULT, "currencyIso"),
        });
        // Fitch
        columnModels[1] = new DefaultTableColumnModel(new TableColumn[] {
                new TableColumn(I18n.I.ratingAgency(), 10, TableCellRenderers.DEFAULT_28, "source"),
                new TableColumn(I18n.I.name(), 170, TableCellRenderers.DEFAULT_28, "issuername"),
                new TableColumn("IFS", 10f, TableCellRenderers.DEFAULT, "ratingFitchIssuerIFS"),
                new TableColumn("Shortterm", 50f, TableCellRenderers.DEFAULT, "ratingFitchIssuerShortTerm"),
                new TableColumn("Shortterm Date", 50f, TableCellRenderers.DEFAULT, "ratingFitchIssuerShortTermDate"),
                new TableColumn("Longterm", 50f, TableCellRenderers.DEFAULT, "ratingFitchIssuerLongTerm"),
                new TableColumn("Longterm Date", 50f, TableCellRenderers.DEFAULT, "ratingFitchIssuerLongTermDate"),
        });
        // Moodys
        columnModels[2] = new TableColumnModelBuilder().addColumns(
                new TableColumn(I18n.I.ratingAgency(), 10, TableCellRenderers.DEFAULT_28, "source"),
                new TableColumn(I18n.I.name(), 170, TableCellRenderers.DEFAULT_28, "issuername")
        ).addGroup(MOODYS_CTP,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTerm"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDate"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTerm"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDate")
        ).addGroup(MOODYS_CTP_BACKED,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermBacked"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateBacked"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermBacked"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateBacked")
        ).addGroup(MOODYS_SU,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermSu"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateSu"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermSu"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateSu")
        ).addGroup(MOODYS_SU_BACKED,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermSuBacked"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateSuBacked"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermSuBacked"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateSuBacked")
        ).addGroup(MOODYS_BDR,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermBdr"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateBdr"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermBdr"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateBdr")
        ).addGroup(MOODYS_BDR_BACKED,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermBdrBacked"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateBdrBacked"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermBdrBacked"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateBdrBacked")
        ).addGroup(MOODYS_IFS,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermIfsr"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateIfsr"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermIfsr"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateIfsr")
        ).addGroup(MOODYS_IFS_BACKED,
                new TableColumn(ST, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermIfsrBacked"),
                new TableColumn(date(ST), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerShortTermDateIfsrBacked"),
                new TableColumn(LT, 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermIfsrBacked"),
                new TableColumn(date(LT), 50f, TableCellRenderers.DEFAULT, "ratingMoodysIssuerLongTermDateIfsrBacked")
        ).asTableColumnModel();
        // SnP
        columnModels[3] = new DefaultTableColumnModel(new TableColumn[] {
                new TableColumn(I18n.I.ratingAgency(), 10, TableCellRenderers.DEFAULT_28, "source"),
                new TableColumn(I18n.I.name(), 170, TableCellRenderers.DEFAULT_28, "issuername"),
                new TableColumn("LTR", 10f, TableCellRenderers.DEFAULT, "ratingStandardAndPoorsIssuerLongTermRegulatoryId"),
                new TableColumn("Shortterm", 50f, TableCellRenderers.DEFAULT, "ratingStandardAndPoorsIssuerShortTerm"),
                new TableColumn("Shortterm Date", 50f, TableCellRenderers.DEFAULT, "ratingStandardAndPoorsIssuerShortTermDate"),
                new TableColumn("Longterm", 50f, TableCellRenderers.DEFAULT, "ratingStandardAndPoorsIssuerLongTerm"),
                new TableColumn("Longterm Date", 50f, TableCellRenderers.DEFAULT, "ratingStandardAndPoorsIssuerLongTermDate"),
        });
    }

    public static final RowMapper<BndIssuerRating> BASE_ROW_MAPPER = new AbstractRowMapper<BndIssuerRating>() {
        @Override
        public Object[] mapRow(BndIssuerRating r) {
            return new Object[]{
                    r.getSource(),
                    r.getIssuername(),
                    r.getCountryIso(),
                    r.getCurrencyIso(),
            };
        }
    };

    public static final RowMapper<BndIssuerRating> SNP_ROW_MAPPER = new AbstractRowMapper<BndIssuerRating>() {
        @Override
        public Object[] mapRow(BndIssuerRating r) {
            return new Object[]{
                    r.getSource(),
                    r.getIssuername(),
                    r.getRatingStandardAndPoorsIssuerLongTermRegulatoryId(),
                    r.getRatingStandardAndPoorsIssuerShortTerm(),
                    r.getRatingStandardAndPoorsIssuerShortTermDate(),
                    r.getRatingStandardAndPoorsIssuerLongTerm(),
                    r.getRatingStandardAndPoorsIssuerLongTermDate(),
            };
        }
    };

    public static final RowMapper<BndIssuerRating> MOODYS_ROW_MAPPER = new AbstractRowMapper<BndIssuerRating>() {
        @Override
        public Object[] mapRow(BndIssuerRating r) {
            return new Object[]{
                    r.getSource(),
                    r.getIssuername(),
                    r.getRatingMoodysIssuerShortTerm(),
                    r.getRatingMoodysIssuerShortTermDate(),
                    r.getRatingMoodysIssuerLongTerm(),
                    r.getRatingMoodysIssuerLongTermDate(),
                    r.getRatingMoodysIssuerShortTermBacked(),
                    r.getRatingMoodysIssuerShortTermDateBacked(),
                    r.getRatingMoodysIssuerLongTermBacked(),
                    r.getRatingMoodysIssuerLongTermDateBacked(),
                    r.getRatingMoodysIssuerShortTermSu(),
                    r.getRatingMoodysIssuerShortTermDateSu(),
                    r.getRatingMoodysIssuerLongTermSu(),
                    r.getRatingMoodysIssuerLongTermDateSu(),
                    r.getRatingMoodysIssuerShortTermSuBacked(),
                    r.getRatingMoodysIssuerShortTermDateSuBacked(),
                    r.getRatingMoodysIssuerLongTermSuBacked(),
                    r.getRatingMoodysIssuerLongTermDateSuBacked(),
                    r.getRatingMoodysIssuerShortTermBdr(),
                    r.getRatingMoodysIssuerShortTermDateBdr(),
                    r.getRatingMoodysIssuerLongTermBdr(),
                    r.getRatingMoodysIssuerLongTermDateBdr(),
                    r.getRatingMoodysIssuerShortTermBdrBacked(),
                    r.getRatingMoodysIssuerShortTermDateBdrBacked(),
                    r.getRatingMoodysIssuerLongTermBdrBacked(),
                    r.getRatingMoodysIssuerLongTermDateBdrBacked(),
                    r.getRatingMoodysIssuerShortTermIfsr(),
                    r.getRatingMoodysIssuerShortTermDateIfsr(),
                    r.getRatingMoodysIssuerLongTermIfsr(),
                    r.getRatingMoodysIssuerLongTermDateIfsr(),
                    r.getRatingMoodysIssuerShortTermIfsrBacked(),
                    r.getRatingMoodysIssuerShortTermDateIfsrBacked(),
                    r.getRatingMoodysIssuerLongTermIfsrBacked(),
                    r.getRatingMoodysIssuerLongTermDateIfsrBacked()
            };
        }
    };

    public static final RowMapper<BndIssuerRating> FITCH_ROW_MAPPER = new AbstractRowMapper<BndIssuerRating>() {
        @Override
        public Object[] mapRow(BndIssuerRating r) {
            return new Object[]{
                    r.getSource(),
                    r.getIssuername(),
                    r.getRatingFitchIssuerIFS(),
                    r.getRatingFitchIssuerShortTerm(),
                    r.getRatingFitchIssuerShortTermDate(),
                    r.getRatingFitchIssuerLongTerm(),
                    r.getRatingFitchIssuerLongTermDate(),
            };
        }
    };

}
