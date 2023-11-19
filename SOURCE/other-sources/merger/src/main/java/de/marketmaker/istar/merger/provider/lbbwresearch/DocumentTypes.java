package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This class holds all document types
 */
final class DocumentTypes {

    private DocumentTypes() {}

    private static final List<String> STOCK_DOCUMENT_TYPES =
            ImmutableList.of(
                    "AKTIE_KOMPAKT",
                    "BLICKPUNKT_EQUITIES",
                    "DIVIDENDENKALENDER",
                    "LBBW_EQUITY_WEEKLY"
            );

    private static final List<String> COMPANY_DOCUMENT_TYPES =
            ImmutableList.of(
                    "COMPANY_ALERT",
                    "COMPANY_FLASH",
                    "COMPANY_REPORT"
            );

    private static final List<String> SECTOR_DOCUMENT_TYPES =
            ImmutableList.of(
                    "SECTOR_ALERT",
                    "SECTOR_FLASH",
                    "SECTOR_KOMPAKT",
                    "SECTOR_REPORT"
            );

    private static final List<String> CREDIT_DOCUMENT_TYPES =
            ImmutableList.of(
                    "LBBW_CREDIT_COMPASS",
                    "LBBW_CREDIT_OUTLOOK",
                    "LBBW_CREDITS"
            );

    private static final List<String> BONDS_DOCUMENT_TYPES =
            ImmutableList.of(
                    "BLICKPUNKT_BOND",
                    "COVERED_BOND_ISSUER_PROFILE",
                    "COVERED_BOND_NII",
                    "COVERED_BOND_UPDATE",
                    "COVEREDBONDSWEEKLY",
                    "FINANCIAL_NACHRANG_QUARTERLY",
                    "STRATEGY_ALERT_RENTEN",
                    "STRATEGY_FLASH_RENTEN"
            );

    private static final List<String> COMMODITIES_DOCUMENT_TYPES =
            ImmutableList.of(
                    "BLICKPUNKT_COMMODITIES",
                    "COMMODITIES_STRATEGY",
                    "COMMODITIES_WEEKLY",
                    "COMMODITY_RESEARCH_FOKUS",
                    "COMMODITY_YEARBOOK"
            );

    private static final List<String> FX_DOCUMENT_TYPES =
            ImmutableList.of(
                    "BLICKPUNKT_FX",
                    "FOREX_SCORING_DAILY",
                    "FOREX_SCORING_MONTHLY",
                    "FOREX_SCORING_WEEKLY",
                    "FX_FLASH",
                    "LBBW_FX_WEEKLY",
                    "LBBW_FX_WEEKLY_ANLAGEEMPF"
            );

    private static final List<String> MISC_DOCUMENT_TYPES =
            ImmutableList.of(
                    "4130_BRANCHENUPDATE",
                    "BLICKPUNKT",
                    "BURKERTS_BLICK",
                    "CB_ISSUER_PROFILE",
                    "CB_NEW_ISSUE_INFORMATION",
                    "CB_UPDATE",
                    "FINANCIAL_ISSUER_PROFILE",
                    "FINANCIAL_NEW_ISSUE_INFO",
                    "FINANCIAL_NEW_ISSUE_INFO",
                    "FINANCIALS_UPDATE",
                    "INDEX_KOMPAKT",
                    "INVESTMENT_DAILY",
                    "KAPITALMAERKTE",
                    "KAPITALMARKT-KOMPASS",
                    "LAENDERKOMPASS",
                    "LBBW_AGENCY_MONITOR",
                    "LBBW_FINANCIAL_COMPASS",
                    "MACRO_ALERT",
                    "MAERKTE_IM_BLICK",
                    "SSA_FLASH",
                    "STRATEGY_ALERT",
                    "STRATEGY_FLASH",
                    "STRATEGY_REPORT",
                    "SZENARIEN_MARKT_KONJUNKTUR",
                    "TECH_ALERT_DAILY",
                    "TECH_ALERT_WEEKLY",
                    "TECHNICAL_MARKET_VIEW",
                    "TREND_EXPERT"
            );

    static final Map<String, List<String>> ALL_DOCUMENT_TYPES =
            ImmutableMap.<String, List<String>>builder()
                    .put("Stock", STOCK_DOCUMENT_TYPES)
                    .put("Company", COMPANY_DOCUMENT_TYPES)
                    .put("Sector", SECTOR_DOCUMENT_TYPES)
                    .put("Credit", CREDIT_DOCUMENT_TYPES)
                    .put("Bonds", BONDS_DOCUMENT_TYPES)
                    .put("Commodities", COMMODITIES_DOCUMENT_TYPES)
                    .put("FX", FX_DOCUMENT_TYPES)
                    .put("Sonstiges", MISC_DOCUMENT_TYPES)
                    .build();
}
