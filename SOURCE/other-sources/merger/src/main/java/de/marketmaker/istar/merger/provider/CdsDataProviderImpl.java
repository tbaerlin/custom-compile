/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import com.google.gson.Gson;
import de.marketmaker.istar.domain.data.CdsDataRecord;
import de.marketmaker.istar.domainimpl.data.CdsDataRecordImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class CdsDataProviderImpl implements CdsDataProvider {

    @Configuration
    static class AppConfig {
        @Bean
        public String bulkUri() {
            return UriComponentsBuilder.newInstance()
                    .scheme("http").host("te-mdp-ords02.df1.vwd-df.net").port(8080)
                    .path("/ords/dp/dm/saptrdata_batch/").build().toUriString();
        }

        @Bean
        public String uriTemplate() {
            return UriComponentsBuilder.newInstance()
                .scheme("http").host("te-mdp-ords02.df1.vwd-df.net").port(8080)
                .path("/ords/dp/dm/saptrdata/{symbol}").build().toUriString();
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public CdsDataProviderImpl cdsDataProvider(RestTemplate restTemplate, String uriTemplate, String bulkUri) {
            final CdsDataProviderImpl provider = new CdsDataProviderImpl();
            provider.setBulkUri(bulkUri);
            provider.setUriTemplate(uriTemplate);
            provider.setRestTemplate(restTemplate);
            return provider;
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(CdsDataProviderImpl.class);

    private String bulkUri;

    private String uriTemplate;

    private RestTemplate restTemplate;

    public void setBulkUri(String bulkUri) {
        this.bulkUri = bulkUri;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public CdsDataRecord getCdsDataRecord(String symbol) {
        final ResponseEntity<CdsDataOrdsRecord> responseEntity = this.restTemplate.getForEntity(this.uriTemplate, CdsDataOrdsRecord.class, symbol);
        final HttpStatus httpStatus = responseEntity.getStatusCode();

        if (!httpStatus.is2xxSuccessful()) {
            LOG.error("<getCdsDataRecord> http status: {} {}", httpStatus.value(), httpStatus.getReasonPhrase());
            return new CdsDataRecordImpl();
        }

        final List<CdsDataOrdsRowset> ordsRowsets = responseEntity.getBody().getRowset();
        if (ordsRowsets.isEmpty()) {
            LOG.warn("<getCdsDataRecord> no record for {}", symbol);
            return new CdsDataRecordImpl();
        }
        final CdsDataOrdsRowset ordsRowset = ordsRowsets.get(0);

        final CdsDataRecordImpl reportingRecord = new CdsDataRecordImpl();

        reportingRecord.setProductCategory(ordsRowset.getAdf790());
        reportingRecord.setProduktcharakteristika(ordsRowset.getAdf387());
        reportingRecord.setWpNameKurz(ordsRowset.getAdf49());


        return reportingRecord;
    }

    @Override
    public Map<String, CdsDataRecord> getCdsDataRecordBulk(List<String> symbols) {
        final ResponseEntity<CdsDataOrdsRecord[]> responseEntity = this.restTemplate.postForEntity(this.bulkUri, toRequestBody(symbols), CdsDataOrdsRecord[].class);
        final HttpStatus httpStatus = responseEntity.getStatusCode();
        Map<String, CdsDataRecord> records = new HashMap<>();

        if (!httpStatus.is2xxSuccessful()) {
            LOG.error("<getCdsDataRecord> http status: {} {}", httpStatus.value(), httpStatus.getReasonPhrase());
            return records;
        }

        final CdsDataOrdsRecord[] ordsRecords = responseEntity.getBody();
        for (CdsDataOrdsRecord ordsRecord : ordsRecords) {
            final List<CdsDataOrdsRowset> ordsRowsets = ordsRecord.getRowset();
            if (ordsRowsets.isEmpty()) {
                LOG.warn("<getCdsDataRecord> no record for {}", symbols);
            }

            for (CdsDataOrdsRowset ordsRowset : ordsRowsets) {
                final CdsDataRecordImpl reportingRecord = new CdsDataRecordImpl();

                reportingRecord.setProductCategory(ordsRowset.getAdf790());
                reportingRecord.setProduktcharakteristika(ordsRowset.getAdf387());
                reportingRecord.setWpNameKurz(ordsRowset.getAdf49());

                records.put(ordsRecord.getVwdKey(), reportingRecord);
            }
        }

        return records;
    }

    /**
     * JSON Object construction. Replaces old net.sf.json-lib:json-lib implementation.
     */
    protected String toRequestBody(List<String> symbols) {
        Gson gson = new Gson();
        return gson.toJson(Collections.singletonMap("vwdkeys", symbols.stream().map(s -> Collections.singletonMap("vwdkey", s)).collect(toList())));
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        CdsDataProviderImpl reportingProvider = ctx.getBean(CdsDataProviderImpl.class);

        System.out.println("============================================================================");
        System.out.println("Single symbol request:");
        System.out.println(reportingProvider.getCdsDataRecord("2840SNUSDCR.SPCDS.6M"));

        System.out.println("============================================================================");
        System.out.println("Bulk symbol request:");
        Map<String, CdsDataRecord> records = reportingProvider.getCdsDataRecordBulk(Arrays.asList("2840SNUSDCR.SPCDS.6M", "17272SNUSDXR.SPCDS.10Y"));
        for (Map.Entry<String, CdsDataRecord> entry : records.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
