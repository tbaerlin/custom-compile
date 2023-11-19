/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.domain.data.NullRegulatoryReportingRecord;
import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import de.marketmaker.istar.domainimpl.data.RegulatoryReportingRecordImpl;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implements {@link RegulatoryReportingProvider} based on REST calls into ORDS service.
 * <p>
 * Following metrics are relevant for performance monitoring:
 *   <dl>
 *     <dt>ords[dm.mifid]</dt><dd>endpointDmMifid</dd>
 *     <dt>ords[euwax.date]</dt><dd>endpointEuwaxDate</dd>
 *   </dl>
 *   If too slow, switch to MDP export and provider approach.
 * </p>
 */
public class RegulatoryReportingProviderImpl implements RegulatoryReportingProvider {

  @Configuration
  static class AppConfig {

    @Bean
    public String endpointDmMifid() {
      return UriComponentsBuilder.newInstance()
          .scheme("https").host("ords-test.internal.infrontservices.com")
          .path("/ords/dp/dm-mifid/adffields_ext/{instrumentId}").build().toUriString();
    }

    @Bean
    public String endpointEuwaxDate() {
      return UriComponentsBuilder.newInstance()
          .scheme("https").host("ords-test.internal.infrontservices.com")
          .path("/ords/dp/dm/euwax/{isin}").build().toUriString();
    }

    @Bean
    public RestTemplateFactory restTemplate() {
      final RestTemplateFactory factory = new RestTemplateFactory();
      factory.setTrustSelfSignedCert(true);
      factory.setVerifySSLHostname(false);
      return factory;
    }

    @Bean
    public RegulatoryReportingProviderImpl regulatoryReportingProvider(RestTemplate restTemplate) {
      final RegulatoryReportingProviderImpl provider = new RegulatoryReportingProviderImpl();
      provider.setEndpointDmMifid(endpointDmMifid());
      provider.setEndpointEuwaxDate(endpointEuwaxDate());
      provider.setRestTemplate(restTemplate);
      provider.setMeterRegistry(new SimpleMeterRegistry());
      return provider;
    }
  }

  private static final Logger log = LoggerFactory.getLogger(RegulatoryReportingProviderImpl.class);

  private String endpointDmMifid;

  private String endpointEuwaxDate;

  private RestTemplate restTemplate;

  private MeterRegistry meterRegistry;

  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void setEndpointDmMifid(String endpointDmMifid) {
    this.endpointDmMifid = endpointDmMifid;
  }

  public void setEndpointEuwaxDate(String endpointEuwaxDate) {
    this.endpointEuwaxDate = endpointEuwaxDate;
  }

  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public RegulatoryReportingRecord getPriceRegulatoryReportingRecord(
      RegulatoryReportingRequest reportingRequest) {
    final long iid = reportingRequest.getInstrumentId();
    final ResponseEntity<RegulatoryReportingOrdsRecord> responseEntity =
        this.meterRegistry.timer("REST", "endpoint", "ords.dm.mifid")
            .record(() -> this.restTemplate.getForEntity(
                this.endpointDmMifid, RegulatoryReportingOrdsRecord.class, iid));
    final HttpStatus httpStatus = responseEntity.getStatusCode();

    if (!httpStatus.is2xxSuccessful()) {
      log.error("<getPriceRegulatoryReportingRecord> {},{},{}",
          this.endpointDmMifid, iid, httpStatus.value());
      return NullRegulatoryReportingRecord.INSTANCE;
    }

    final List<RegulatoryReportingOrdsRowset> ordsRowsets = responseEntity.getBody().getRowset();
    if (ordsRowsets.isEmpty()) {
      log.warn("<getPriceRegulatoryReportingRecord> no record for {}", iid);
      return NullRegulatoryReportingRecord.INSTANCE;
    }
    final RegulatoryReportingOrdsRowset ordsRowset = ordsRowsets.get(0);

    final RegulatoryReportingRecordImpl reportingRecord = new RegulatoryReportingRecordImpl();
    final Locale locale = RequestContextHolder.getRequestContext().getLocale();

    // 1. MiFID II

    reportingRecord.setMifidCapitalClass(ordsRowset.getAdf2870(locale));
    reportingRecord.setMifidAssetClass(ordsRowset.getAdf2871(locale));
    reportingRecord.setMifidAssetClassAddition(ordsRowset.getAdf2872(locale));
    reportingRecord.setMifidClFiRts28(ordsRowset.getAdf2873(locale));
    reportingRecord.setMifidLeverageProductIdentifier(ordsRowset.getAdf2874(locale));
    reportingRecord.setMifidTickSize(ordsRowset.getAdf2875(locale));
    reportingRecord.setMifidProductCategory(ordsRowset.getAdf2876(locale));
    reportingRecord.setMifidProductApprovalProcessIdentifier(ordsRowset.getAdf2877(locale));

    // 2a. Target market

    reportingRecord.setTmSourceOfTargetMarketData(ordsRowset.getAdf2849(locale));
    reportingRecord.setTmCustomerCategory(ordsRowset.getAdf2850(locale));
    reportingRecord.setTmInvestmentObjectives(ordsRowset.getAdf2851(locale));
    reportingRecord.setTmInvestmentHorizon(ordsRowset.getAdf2852(locale));
    reportingRecord.setTmRiskIndicator(ordsRowset.getAdf2853(locale));
    reportingRecord.setTmCalculationMethodOfRiskIndicator(ordsRowset.getAdf2854(locale));

    reportingRecord.setTmRiskProfile(ordsRowset.getAdf2855(locale));
    reportingRecord.setTmFinancialLossBearingCapacity(ordsRowset.getAdf2856(locale));
    reportingRecord.setTmKnowledgeAndExperience(ordsRowset.getAdf2857(locale));
    reportingRecord.setTmDistributionStrategy(ordsRowset.getAdf2858(locale));
    reportingRecord.setTmSpecialRequirements(ordsRowset.getAdf2859(locale));
    reportingRecord.setTmAddendumToTheSpecialRequirements(ordsRowset.getAdf2860());

    // 2b. Neg. Target market

    reportingRecord.setNtmCustomerCategory(ordsRowset.getAdf2861(locale));
    reportingRecord.setNtmInvestmentObjectives(ordsRowset.getAdf2862(locale));
    reportingRecord.setNtmInvestmentHorizon(ordsRowset.getAdf2863(locale));
    reportingRecord.setNtmRiskIndicator(ordsRowset.getAdf2864(locale));
    reportingRecord.setNtmRiskAndYieldProfile(ordsRowset.getAdf2865(locale));
    reportingRecord.setNtmFinancialLossBearingCapacity(ordsRowset.getAdf2866(locale));
    reportingRecord.setNtmKnowledgeAndExperience(ordsRowset.getAdf2867(locale));
    reportingRecord.setNtmDistributionStrategy(ordsRowset.getAdf2868(locale));
    reportingRecord.setNtmSpecialRequirements(ordsRowset.getAdf2869(locale));

    // 3a. Cost Funds

    reportingRecord.setCfPerformanceFeeIdentifier(ordsRowset.getAdf2878(locale));
    reportingRecord.setCfSwingPricingIdentifier(ordsRowset.getAdf2879(locale));
    reportingRecord.setCfRunningFundCostsEst(BigDecimal.valueOf(ordsRowset.getAdf2880()));
    reportingRecord.setCfRunningFundCostsEstDate(ordsRowset.getAdf2881());
    reportingRecord.setCfTransactionCostsFundsEst(BigDecimal.valueOf(ordsRowset.getAdf2882()));
    reportingRecord.setCfTransactionCostsFundsEstDate(ordsRowset.getAdf2883());
    reportingRecord.setCfEventRelatedCostsFundsEst(BigDecimal.valueOf(ordsRowset.getAdf2884()));
    reportingRecord.setCfEventRelatedCostsFundsEstDate(ordsRowset.getAdf2885());
    reportingRecord.setCfActualRedemptionCostsFund(BigDecimal.valueOf(ordsRowset.getAdf2886()));
    reportingRecord.setCfActualRedemptionCostsFundDate(ordsRowset.getAdf2887());
    reportingRecord.setCfMinimumBackEndLoad(BigDecimal.valueOf(ordsRowset.getAdf2888()));
    reportingRecord.setCfMinimumBackEndLoadCurrency(ordsRowset.getAdf2889(locale));
    reportingRecord.setCfMinimumBackEndLoadPercentSign(ordsRowset.getAdf2890(locale));
    reportingRecord.setCfMinimumBackEndLoadReferenceValue(ordsRowset.getAdf2891(locale));
    reportingRecord.setCfMaximumBackEndLoad(BigDecimal.valueOf(ordsRowset.getAdf2892()));
    reportingRecord.setCfMaximumBackEndLoadCurrency(ordsRowset.getAdf2893(locale));
    reportingRecord.setCfMaximumBackEndLoadPercentSign(ordsRowset.getAdf2894(locale));
    reportingRecord.setCfMaximumBackEndLoadReferenceValue(ordsRowset.getAdf2895(locale));
    reportingRecord.setCfTotalFundCostsDateFrom(ordsRowset.getAdf2896());
    reportingRecord.setCfTotalFundCostsDateTo(ordsRowset.getAdf2897());
    reportingRecord.setCfTotalFundCostsTransaction(BigDecimal.valueOf(ordsRowset.getAdf2898()));
    reportingRecord.setCfTotalFundCostsRunning(BigDecimal.valueOf(ordsRowset.getAdf2899()));
    reportingRecord.setCfTotalFundCostsEventRelated(BigDecimal.valueOf(ordsRowset.getAdf2900()));
    reportingRecord.setNtmCustomerCategory(String.valueOf(ordsRowset.getAdf2901(locale)));

    // 3b. Cost Structured Products

    reportingRecord.setCspFairValueInstrument(ordsRowset.getAdf2902(locale));
    reportingRecord.setCspInstrumentWithRunningCosts(ordsRowset.getAdf2903(locale));
    reportingRecord.setCspEstRunningCostsPrFv(BigDecimal.valueOf(ordsRowset.getAdf2904()));
    reportingRecord.setCspEstRunningCostsPrFvCurrency(ordsRowset.getAdf2905(locale));
    reportingRecord.setCspEstRunningCostsPrFvPercentSign(ordsRowset.getAdf2906(locale));
    reportingRecord.setCspEstRunningCostsPrFvDate(ordsRowset.getAdf2907());
    reportingRecord.setCspEntryCostsPrFv(ordsRowset.getAdf2908());
    reportingRecord.setCspEntryCostsPrFvCurrency(ordsRowset.getAdf2909(locale));
    reportingRecord.setCspEntryCostsPrFvPercentSign(ordsRowset.getAdf2910(locale));
    reportingRecord.setCspEntryCostsPrFvTime(ordsRowset.getAdf2911());
    reportingRecord.setCspEntryCostsPrFvCorrectionId(ordsRowset.getAdf2912(locale));
    reportingRecord.setCspExitCostsPrFv(BigDecimal.valueOf(ordsRowset.getAdf2913()));
    reportingRecord.setCspExitCostsPrFvCurrency(ordsRowset.getAdf2914(locale));
    reportingRecord.setCspExitCostsPrFvPercentSign(ordsRowset.getAdf2915(locale));
    reportingRecord.setCspExitCostsPrFvTime(ordsRowset.getAdf2916());
    reportingRecord.setCspExitCostsPrFvCorrectionId(ordsRowset.getAdf2917(locale));
    reportingRecord.setCspRunningIncrementalCostsPrFv(BigDecimal.valueOf(ordsRowset.getAdf2918()));
    reportingRecord.setCspRunningIncrementalCostsPrFvCurrency(ordsRowset.getAdf2919(locale));
    reportingRecord.setCspRunningIncrementalCostsPrFvPercentSign(ordsRowset.getAdf2920(locale));
    reportingRecord.setCspRunningIncrementalCostsPrFvDate(ordsRowset.getAdf2921());
    reportingRecord.setCspRunningIncrementalCostsPrFvCorrectionId(ordsRowset.getAdf2922(locale));

    // Priips Kennzeichnung

    reportingRecord.setPriipsID(ordsRowset.getAdf2923Raw());
    reportingRecord.setPriipsText(ordsRowset.getAdf2923(locale));

    return reportingRecord;
  }

  @Override
  public Optional<EuwaxDates> getEuwaxDates(String isin) {
    final ParameterizedTypeReference<OrdsData<Map<String, EuwaxDates>>> typeRef =
        new ParameterizedTypeReference<OrdsData<Map<String, EuwaxDates>>>() {
        };
    final ResponseEntity<OrdsData<Map<String, EuwaxDates>>> respEntity =
        this.meterRegistry.timer("REST", "endpoint", "ords.euwax.date")
            .record(() -> this.restTemplate.exchange(
                this.endpointEuwaxDate, HttpMethod.GET, HttpEntity.EMPTY, typeRef, isin));

    final HttpStatus httpStatus = respEntity.getStatusCode();
    if (!httpStatus.is2xxSuccessful()) {
      log.error("<getEuwaxDates> {},{},{}", this.endpointEuwaxDate, isin, httpStatus.value());
      return Optional.empty();
    }

    return Optional.ofNullable(respEntity.getBody())
        .map(OrdsData::get_data)
        .filter(m -> !m.isEmpty())
        .map(m -> m.get(0))
        .map(m -> m.get(isin));
  }

  public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    RegulatoryReportingProviderImpl reportingProvider =
        ctx.getBean(RegulatoryReportingProviderImpl.class);

    RequestContext context = new RequestContext(null, MarketStrategy.STANDARD);
    RequestContextHolder.setRequestContext(context);

    // 20664, 100344
    System.out.println(reportingProvider.getPriceRegulatoryReportingRecord(
        new RegulatoryReportingRequest(317585169)));
    reportingProvider.getEuwaxDates("DE000DGE3ML9").ifPresent(System.out::println);
  }
}
