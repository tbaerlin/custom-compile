/*
 * RatioDataRecord.java
 *
 * Created on 01.08.2006 15:06:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.springframework.aop.support.AopUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("UnusedDeclaration")
public interface RatioDataRecord {
    RatioDataRecord NULL = (RatioDataRecord) Proxy.newProxyInstance(
            RatioDataRecord.class.getClassLoader(),
            new Class[]{RatioDataRecord.class}, (proxy, method, args) -> {
                if (AopUtils.isToStringMethod(method)) {
                    return RatioDataRecord.class.getSimpleName() + "[NULL]";
                }
                if (method.getReturnType() == long.class) {
                    return 0L;
                }
                return null;
            });

    enum Field {
        qid, name, symbol, wkn, isin, vwdCode, market, underlyingSymbol, underlyingVwdcode, underlyingQid, underlyingWkn, underlyingIsin, underlyingName, underlyingIid,
        issuername, vrIssuer, currency, issueSurcharge, etf, ratingMorningstar, volatility1w, volatility1m, volatility3m, volatility6m, volatility1y, volatility3y,
        volatility5y, volatility10y, volatilityCurrentYear,changePercent, bviperformanceCurrentYear, bviperformance1d, bviperformance1w, bviperformance1m, bviperformance3m, bviperformance6m,
        bviperformance1y, bviperformance3y, bviperformance5y, bviperformance10y, sharpeRatio1w, sharpeRatio1m, sharpeRatio3m, sharpeRatio6m, sharpeRatio1y, sharpeRatio3y,
        sharpeRatio5y, sharpeRatio10y, maximumLoss3y, investmentFocus, fundVolume, fundtype, country, distributionStrategy, issueDate, ratingFeri, postbankSurcharge,
        postbankRiskclass, managementFee, accountFee, ter, marketAdmission, region, fundtypeBviCoarse, benchmarkOutperformance1y, benchmarkOutperformance3y,
        probabilityOfOutperformance3y, sector, index, performanceAlltime, performance1w, performance1m, performance3m, performance6m, performance1y, performance3y,
        performance5y, performance10y, performanceToBenchmark1d, performanceToBenchmark1w, performanceToBenchmark1m, performanceToBenchmark3m, performanceToBenchmark6m,
        performanceToBenchmark1y, performanceToBenchmark3y, performanceToBenchmark5y, performanceToBenchmark10y, performanceToBenchmarkCurrentYear, beta1m, beta1y, alpha1m, alpha1y, changePercentHighAlltime,
        changePercentHigh1y, changePercentLow1y, spreadPercent, spread, correlation1w, correlation1m, correlation3m, correlation6m, correlation1y, correlation3y, correlation5y,
        correlation10y, correlationCurrentYear, averageVolume1w, averageVolume1m, averageVolume3m, averageVolume6m, averageVolume1y, averageVolume3y, averageVolume5y, averageVolume10y, screenerInterest,
        averagePrice1w, averagePrice1m, averagePrice3m, averagePrice6m, averagePrice1y, averagePrice3y, averagePrice5y, averagePrice10y,
        priceSalesRatio1y, priceEarningRatio1y, priceBookvalueRatio1y, priceCashflowRatio1y, priceSalesRatio2y, priceEarningRatio2y, priceBookvalueRatio2y, priceCashflowRatio2y,
        priceSalesRatio3y, priceEarningRatio3y, priceCashflowRatio3y, priceSalesRatio4y, priceEarningRatio4y, priceCashflowRatio4y,
        totalVolume, price, eps1y, eps2y, dividend1y, dividend2y, dividendYield1y, dividendYield2y, dividendYield3y, dividendYield4y, sales1y, sales2y, sales3y, sales4y,
        profit1y, profit2y, profit3y, profit4y, ebit1y, ebit2y, ebit3y, ebit4y, ebitda1y, ebitda2y, ebitda3y, ebitda4y,
        marketCapitalization, fiscalYear, recommendation, warrantType, strike, expirationDate, isAmerican, omega, delta, vega, rho, gamma, theta, impliedVolatility,
        fairValue, extrinsicValue, intrinsicValue, optionPrice, breakeven, postbankType, certificateType, certificateSubtype, multiassetName,
        certificateTypeDZBANK, bonusLevel, cap, capLevel, gapCap, gapCapRelative, protectLevel, dateBarrierReached, discount, discountRelative, yieldRelativePerYear, yield,
        yieldRelative, gapBonusLevelRelative, gapBonusLevel, gapBonusBufferRelative, agio, agioRelative, agioRelativePerYear, maximumYield, maximumYieldRelative,
        maximumYieldRelativePerYear, outperformanceValue, unchangedYield, unchangedYieldRelative, unchangedYieldRelativePerYear, capToUnderlyingRelative, underlyingToCapRelative,
        gapStrike, gapStrikeRelative, isknockout, stoploss, participationFactor, participationLevel, gapBarrier, gapBarrierRelative, gapLowerBarrier, gapLowerBarrierRelative,
        gapUpperBarrier, gapUpperBarrierRelative, leverage, nominalInterest, guaranteeType, leverageType, barrier, coupon, couponType, bondType, ratingFitchLongTerm,
        ratingFitchLongTermDate, ratingFitchLongTermAction, postbankBond, ratingFitchShortTerm, ratingFitchShortTermDate, ratingFitchShortTermAction, ratingMoodysShortTerm,
        ratingMoodysShortTermDate, ratingMoodysShortTermAction, ratingMoodysShortTermSource, ratingMoodysLongTerm, ratingMoodysLongTermDate, ratingMoodysLongTermAction, ratingMoodysLongTermSource, ratingSnPShortTerm, ratingSnPShortTermDate,
        ratingSnPShortTermAction, ratingSnPLongTerm, ratingSnPLongTermDate, ratingSnPLongTermAction, ratingSnPLongTermRegulatoryId, ratingSnPLongTermQualifier, basePointValue,
        duration, convexity, modifiedDuration, brokenPeriodInterest, interestPeriod, interestRateElasticity, pari, issuerCategory, optionType, version, wgzListid, startvalue,
        stopvalue, refundMaximum, knockin, knockindate, underlyingType, edgRatingDate, edgScore1, edgScore2, edgScore3, edgScore4, edgScore5, edgTopScore, edgTopClass,
        quanto, optionCategory, exerciseType, cfsRating, type, typeKey, subtype, thetaRelative, theta1w,
        theta1wRelative, theta1m, theta1mRelative, dividend, dividendYield, dividendCurrency, vwdStaticDataAvailable, pibAvailable, issueCurrency, subtypeKey,
        dzIsLeverageProduct, performanceCurrentYear, optionPricePerYear, alpha1w, alpha3m, alpha6m, alpha3y, alpha5y, isEndless, benchmarkName, ratingMoodys, turnoverDay,
        alpha10y, ongoingCharge, ongoingChargeDate, tickSize, tickValue, tickCurrency, contractValue, contractValueCalculated, contractCurrency, contractSize, generationNumber,
        tradingMonth, bisKey, znsCategory, maturity, debtRanking, issuerType, restructuringRule, source, versionNumber, srriValue, srriValueDate, diamondRating, vwdsymbol, diamondRatingDate, fidaRating,
        marketCapitalizationUSD, marketCapitalizationEUR, sedexIssuerName, sedexStrike, sedexIssueDate, sedexExpires, fwwRiskclass, notActive, smallestTransferableUnit,
        bondRank, lmeMetalCode, lmeExpirationDate, wmInvestmentAssetPoolClass, gicsSector, gicsIndustryGroup, gicsIndustry, gicsSubIndustry, isSpecialDismissal, lmeComposite,
        ratingSnPLocalLongTerm, ratingSnPLocalLongTermDate, ratingSnPLocalShortTerm, ratingSnPLocalShortTermDate, gicsSectorKey, gicsIndustryGroupKey, gicsIndustryKey, gicsSubIndustryKey,
        merInstrumentenTyp, merHandelsmonat, lei, volatility, rating, standard, fidaPermissionType, benchmarkOutperformance1m, benchmarkOutperformance6m, benchmarkOutperformance5y,
        isFlex, benchmarkOutperformanceCurrentYear, minimumInvestmentAmount, ratingSnPLongTermSource, ratingSnPShortTermSource, ratingSnPLocalLongTermSource, ratingSnPLocalShortTermSource,
        ratingSnPLocalLongTermAction, ratingSnPLocalShortTermAction
    }

    String getRegion();

    String getFundtypeBviCoarse();

    BigDecimal getBenchmarkOutperformance1m();

    BigDecimal getBenchmarkOutperformance6m();

    BigDecimal getBenchmarkOutperformance1y();

    BigDecimal getBenchmarkOutperformance3y();

    BigDecimal getBenchmarkOutperformance5y();

    BigDecimal getBenchmarkOutperformanceCurrentYear();

    String getWarrantType();

    Boolean isAmerican();

    BigDecimal getStrike();

    DateTime getExpirationDate();

    DateTime getTradingMonth();

    BigDecimal getGapBonusLevelRelative();

    BigDecimal getGapBonusLevel();

    BigDecimal getGapBonusBufferRelative();

    BigDecimal getAgio();

    BigDecimal getAgioRelative();

    BigDecimal getAgioRelativePerYear();

    BigDecimal getCapToUnderlyingRelative();

    BigDecimal getUnderlyingToCapRelative();

    BigDecimal getGapStrike();

    BigDecimal getGapStrikeRelative();

    BigDecimal getParticipationLevel();

    BigDecimal getStoploss();

    BigDecimal getProtectLevel();

    BigDecimal getGapCap();

    Boolean getIsknockout();

    BigDecimal getGapCapRelative();

    BigDecimal getOutperformanceValue();

    BigDecimal getUnchangedYield();

    BigDecimal getUnchangedYieldPercent();

    BigDecimal getGapUpperBarrier();

    BigDecimal getGapBarrier();

    BigDecimal getGapBarrierRelative();

    BigDecimal getGapLowerBarrier();

    BigDecimal getGapLowerBarrierRelative();

    BigDecimal getGapUpperBarrierRelative();

    DateTime getDateBarrierReached();

    BigDecimal getPerformanceAlltime();

    Long getWgzListid();

    BigDecimal getParticipationFactor();

    DateTime getKnockinDate();

    BigDecimal getNominalInterest();

    Price getUnderlyingPrice();

    BigDecimal getRefundMaximum();

    BigDecimal getProbabilityOfOutperformance3y();

    BigDecimal getBeta3y();

    DateTime getExpirationDateForMatrix();

    String getWarrantTypeForMatrix();

    BigDecimal getStrikeForMatrix();

    Long getFiscalYear();

    long getInstrumentId();

    long getQuoteId();

    String getIsin();

    String getWkn();

    String getName();

    String getUnderlyingName();

    String getUnderlyingType();

    String getIssuername();

    DateTime getExpires();

    String getSymbolVwdfeedMarket();

    String getCurrencySymbolIso();

    Price getPrice();

    BigDecimal getChangeNet();

    BigDecimal getChangePercent();

    BigDecimal getChangeNet1Year();

    BigDecimal getChangePercent1Month();

    BigDecimal getChangePercent3Months();

    BigDecimal getChangePercent6Months();

    BigDecimal getChangePercent1Year();

    BigDecimal getChangePercent3Years();

    BigDecimal getChangePercent5Years();

    BigDecimal getChangePercent10Years();

    Long getTotalVolume();

    BigDecimal getYield();

    BigDecimal getInterest();

    String getInterestPeriod();

    BigDecimal getIssueSurcharge();

    BigDecimal getManagementFee();

    BigDecimal getAccountFee();

    BigDecimal getBVIPerformance1Week();

    BigDecimal getBVIPerformance1Month();

    BigDecimal getBVIPerformance3Months();

    BigDecimal getBVIPerformance6Months();

    BigDecimal getBVIPerformance1Year();

    BigDecimal getBVIPerformance3Years();

    BigDecimal getBVIPerformance5Years();

    BigDecimal getBVIPerformance10Years();

    BigDecimal getNegativeMonthsPercent1Month();

    BigDecimal getNegativeMonthsPercent3Months();

    BigDecimal getNegativeMonthsPercent6Months();

    BigDecimal getNegativeMonthsPercent1Year();

    BigDecimal getNegativeMonthsPercent3Years();

    BigDecimal getNegativeMonthsPercent5Years();

    BigDecimal getNegativeMonthsPercent10Years();

    String getSector();

    Price getBid();

    Price getAsk();

    Price getHighDay();

    Price getLowDay();

    String getSectorFww();

    String getFundTypeFww();

    String getSubtypeGatrixx();

    String getGuaranteeTypeGatrixx();

    String getLeverageTypeGatrixx();

    BigDecimal getStrikePriceGatrixx();

    BigDecimal getCouponGatrixx();

    BigDecimal getCapGatrixx();

    BigDecimal getKnockinGatrixx();

    BigDecimal getBonuslevelGatrixx();

    BigDecimal getBarrierGatrixx();

    BigDecimal getDiscount();

    BigDecimal getDiscountPercent();

    BigDecimal getMaximumYield();

    BigDecimal getMaximumYieldPercent();

    BigDecimal getMaximumYieldPerAnno();

    BigDecimal getUnchangedYieldPerAnno();

    BigDecimal getYieldPercent();

    BigDecimal getYieldPerAnno();

    BigDecimal getSpread();

    BigDecimal getSpreadPercent();

    BigDecimal getUnderlyingToCap();

    BigDecimal getUnderlyingToCapPercent();

    BigDecimal getCapToUnderlying();

    BigDecimal getCapToUnderlyingPercent();

    BigDecimal getStartvalue();

    BigDecimal getStopvalue();

    Boolean isEtf();

    BigDecimal getCapLevel();

    Boolean isQuanto();

    Boolean isEndless();

    BigDecimal getVolatility3m();

    BigDecimal getVolatility1y();

    BigDecimal getCorrelation1w();

    BigDecimal getCorrelation1m();

    BigDecimal getCorrelation3m();

    BigDecimal getCorrelation6m();

    BigDecimal getCorrelation1y();

    BigDecimal getCorrelation3y();

    BigDecimal getCorrelation5y();

    BigDecimal getCorrelation10y();

    BigDecimal getCorrelationCurrentYear();

    BigDecimal getAverageVolume1w();

    BigDecimal getAverageVolume1m();

    BigDecimal getAverageVolume3m();

    BigDecimal getAverageVolume6m();

    BigDecimal getAverageVolume1y();

    BigDecimal getAverageVolume3y();

    BigDecimal getAverageVolume5y();

    BigDecimal getAverageVolume10y();

    BigDecimal getAveragePrice1w();

    BigDecimal getAveragePrice1m();

    BigDecimal getAveragePrice3m();

    BigDecimal getAveragePrice6m();

    BigDecimal getAveragePrice1y();

    BigDecimal getAveragePrice3y();

    BigDecimal getAveragePrice5y();

    BigDecimal getAveragePrice10y();

    BigDecimal getPerformanceToBenchmark1d();

    BigDecimal getPerformanceToBenchmark1w();

    BigDecimal getPerformanceToBenchmark1m();

    BigDecimal getPerformanceToBenchmark3m();

    BigDecimal getPerformanceToBenchmark6m();

    BigDecimal getPerformanceToBenchmark1y();

    BigDecimal getPerformanceToBenchmark3y();

    BigDecimal getPerformanceToBenchmark5y();

    BigDecimal getPerformanceToBenchmark10y();

    BigDecimal getPerformanceToBenchmarkCurrentYear();

    BigDecimal getPerformance1w();

    BigDecimal getPerformance1m();

    BigDecimal getPerformance3m();

    BigDecimal getPerformance6m();

    BigDecimal getPerformance1y();

    BigDecimal getPerformance3y();

    BigDecimal getPerformance5y();

    BigDecimal getPerformance10y();

    BigDecimal getPerformanceCurrentYear();

    BigDecimal getChangePercentHigh1y();

    BigDecimal getChangePercentLow1y();

    BigDecimal getChangePercentHighAlltime();

    DateTime getIssueDate();

    BigDecimal getFundVolume();

    String getCountry();

    Price getHigh1y();

    Price getLow1y();

    BigDecimal getBVIPerformanceCurrentYear();

    BigDecimal getVolatilityCurrentYear();

    BigDecimal getVolatility1w();

    BigDecimal getVolatility1m();

    BigDecimal getVolatility6m();

    BigDecimal getVolatility3y();

    BigDecimal getVolatility5y();

    BigDecimal getVolatility10y();

    Long getMarketCapitalization();

    BigDecimal getSharpeRatio1w();

    BigDecimal getSharpeRatio1m();

    BigDecimal getSharpeRatio3m();

    BigDecimal getSharpeRatio6m();

    BigDecimal getSharpeRatio1y();

    BigDecimal getSharpeRatio3y();

    BigDecimal getSharpeRatio5y();

    BigDecimal getSharpeRatio10y();

    BigDecimal getTreynor1y();

    BigDecimal getTreynor3y();

    BigDecimal getTreynor5y();

    BigDecimal getInformationRatio1y();

    BigDecimal getInformationRatio3y();

    BigDecimal getInformationRatio5y();

    BigDecimal getSterlingRatio1y();

    BigDecimal getSterlingRatio3y();

    BigDecimal getSterlingRatio5y();

    BigDecimal getTrackingError1y();

    BigDecimal getTrackingError3y();

    BigDecimal getTrackingError5y();

    BigDecimal getAlpha1w();

    BigDecimal getAlpha1y();

    BigDecimal getAlpha3m();

    BigDecimal getAlpha6m();

    BigDecimal getAlpha10y();

    BigDecimal getAlpha3y();

    BigDecimal getAlpha5y();

    BigDecimal getBeta1m();

    BigDecimal getBeta1y();

    BigDecimal getMaximumLoss3y();

    Integer getMaximumLossMonths3y();

    Price getStockProfit();

    Price getEstateProfit();

    Price getInterimProfit();

    BigDecimal getFeriBenchmarkPerformance1y();

    BigDecimal getFeriBenchmarkPerformance3y();

    BigDecimal getFeriProbOfOutperf3y();

    BigDecimal getFeriAlpha3y();

    BigDecimal getFeriBeta3y();

    String getInvestmentFocus();

    String getDistributionStrategy();

    String getRatingFeri();

    Long getRatingMorningstar();

    BigDecimal getTer();

    String getFundtype();

    String getMarketAdmission();

    BigDecimal getPriceSalesRatio1y();

    BigDecimal getPriceSalesRatio2y();

    BigDecimal getPriceSalesRatio3y();

    BigDecimal getPriceSalesRatio4y();

    BigDecimal getPriceEarningRatio1y();

    BigDecimal getPriceEarningRatio2y();

    BigDecimal getPriceEarningRatio3y();

    BigDecimal getPriceEarningRatio4y();

    BigDecimal getPriceBookvalueRatio1y();

    BigDecimal getPriceBookvalueRatio2y();

    BigDecimal getPriceCashflowRatio1y();

    BigDecimal getPriceCashflowRatio2y();

    BigDecimal getPriceCashflowRatio3y();

    BigDecimal getPriceCashflowRatio4y();

    BigDecimal getDividend1y();

    BigDecimal getDividend2y();

    BigDecimal getDividendYield1y();

    BigDecimal getDividendYield2y();

    BigDecimal getDividendYield3y();

    BigDecimal getDividendYield4y();

    BigDecimal getSales1y();

    BigDecimal getSales2y();

    BigDecimal getSales3y();

    BigDecimal getSales4y();

    BigDecimal getProfit1y();

    BigDecimal getProfit2y();

    BigDecimal getProfit3y();

    BigDecimal getProfit4y();

    BigDecimal getEBIT1y();

    BigDecimal getEBIT2y();

    BigDecimal getEBIT3y();

    BigDecimal getEBIT4y();

    BigDecimal getEBITDA1y();

    BigDecimal getEBITDA2y();

    BigDecimal getEBITDA3y();

    BigDecimal getEBITDA4y();

    Long getScreenerInterest();

    BigDecimal getOmega();

    BigDecimal getImpliedVolatility();

    BigDecimal getDelta();

    BigDecimal getIntrinsicValue();

    BigDecimal getExtrinsicValue();

    BigDecimal getFairValue();

    BigDecimal getOptionPrice();

    BigDecimal getOptionPricePerYear();

    BigDecimal getBreakeven();

    BigDecimal getTheta();

    BigDecimal getRho();

    BigDecimal getGamma();

    BigDecimal getLeverage();

    BigDecimal getVega();

    String getUnderlyingIsin();

    String getCertificateType();

    String getCertificateSubtype();

    String getMultiassetName();

    String getCertificateTypeDZBANK();

    BigDecimal getCap();

    Long getRiskclassAttrax();

    BigDecimal getCoupon();

    String getCouponType();

    String getBondType();

    String getBondRank();

    String getOptionType();

    String getOptionCategory();

    String getCurrency();

    String getRatingFitchLongTerm();

    DateTime getRatingFitchLongTermDate();

    String getRatingFitchLongTermAction();

    String getRatingFitchShortTerm();

    DateTime getRatingFitchShortTermDate();

    String getRatingFitchShortTermAction();

    String getRatingMoodysLongTerm();

    DateTime getRatingMoodysLongTermDate();

    String getRatingMoodysLongTermAction();

    String getRatingMoodysLongTermSource();

    String getRatingMoodysShortTerm();

    DateTime getRatingMoodysShortTermDate();

    String getRatingMoodysShortTermAction();

    String getRatingMoodysShortTermSource();

    BigDecimal getBasePointValue();

    BigDecimal getDuration();

    BigDecimal getConvexity();

    BigDecimal getModifiedDuration();

    BigDecimal getBrokenPeriodInterest();

    BigDecimal getInterestRateElasticity();

    String getProductnameIssuer();

    BigDecimal getRecommendation();

    String getIssuerCategory();

    DateTime getEdgRatingDate();

    Long getEdgScore1();

    Long getEdgScore2();

    Long getEdgScore3();

    Long getEdgScore4();

    Long getEdgScore5();

    Long getEdgTopClass();

    Long getEdgTopScore();

    String getBenchmarkName();

    String getExerciseType();

    String getType();

    String getTypeKey();

    String getSubtype();

    String getSubtypeKey();

    BigDecimal getThetaRelative();

    BigDecimal getTheta1w();

    BigDecimal getTheta1wRelative();

    BigDecimal getTheta1m();

    BigDecimal getTheta1mRelative();

    BigDecimal getDividend();

    BigDecimal getDividendYield();

    String getDividendCurrency();

    DateTime getExternalReferenceTimestamp();

    String getIssueCurrency();

    BigDecimal getTurnoverDay();

    BigDecimal getOngoingCharge();

    DateTime getOngoingChargeDate();

    BigDecimal getTickSize();

    BigDecimal getTickValue();

    String getTickCurrency();

    BigDecimal getContractValue();

    BigDecimal getContractValueCalculated();

    String getContractCurrency();

    BigDecimal getContractSize();

    Long getGenerationNumber();

    Long getVersionNumber();

    String getRatingSnPShortTerm();

    DateTime getRatingSnPShortTermDate();

    String getRatingSnPShortTermAction();

    String getRatingSnPLongTerm();

    DateTime getRatingSnPLongTermDate();

    String getRatingSnPLongTermAction();

    String getRatingSnPLongTermRegulatoryId();

    String getRatingSnPLongTermQualifier();

    String getRatingSnPLocalLongTerm();

    DateTime getRatingSnPLocalLongTermDate();

    String getRatingSnPLocalShortTerm();

    DateTime getRatingSnPLocalShortTermDate();

    String getZnsCategory();

    String getMaturity();

    String getDebtRanking();

    String getIssuerType();

    String getRestructuringRule();

    String getSource();

    Long getSrriValue();

    DateTime getSrriValueDate();

    Long getDiamondRating();

    DateTime getDiamondRatingDate();

    Long getFidaRating();

    String getSedexIssuerName();

    Long getSedexStrike();

    DateTime getSedexIssueDate();

    DateTime getSedexExpires();

    BigDecimal getSmallestTransferableUnit();

    Long getFwwRiskclass();

    String getLmeMetalCode();

    DateTime getLmeExpirationDate();

    String getWmInvestmentAssetPoolClass();

    Boolean isSpecialDismissal();

    String getGicsSector();

    String getGicsIndustryGroup();

    String getGicsIndustry();

    String getGicsSubIndustry();

    Boolean isLmeComposite();

    String getGicsSectorKey();

    String getGicsIndustryGroupKey();

    String getGicsIndustryKey();

    String getGicsSubIndustryKey();

    String getMerHandelsmonat();

    String getMerInstrumentenTyp();

    String getLei();

    BigDecimal getVolatility();

    String getRating();

    String getStandard();

    String getFidaPermissionType();

    Boolean isFlex();

    BigDecimal getMinimumInvestmentAmount();

    String getRatingSnPLongTermSource();

    String getRatingSnPShortTermSource();

    String getRatingSnPLocalLongTermSource();

    String getRatingSnPLocalShortTermSource();

    String getRatingSnPLocalLongTermAction();

    String getRatingSnPLocalShortTermAction();

}
