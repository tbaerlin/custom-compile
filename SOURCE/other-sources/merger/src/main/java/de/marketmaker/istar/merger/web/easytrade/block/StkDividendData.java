/*
 * StkDividendData.java
 *
 * Created on 21.10.2014 12:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.DividendData;
import de.marketmaker.istar.domainimpl.data.DividendDataImpl;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author jkirchg
 */
public class StkDividendData extends StkConvensysRawdata {

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Map<String, Object> model = new HashMap<>();

        // Retrieve convensys xml data
        final TimeTaker tt = new TimeTaker();
        final ModelAndView convensysResponse = super.doHandle(request, response, o, errors);
        this.logger.debug("<doHandle> received convensys response after " + tt);

        if (isValidResponse(convensysResponse)) {
            model.put("quote", convensysResponse.getModel().get("quote"));
            final String xml = (String) convensysResponse.getModel().get("rawdata");
            final Document doc;
            try {
                tt.reset().start();
                doc = parseXML(xml);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<doHandle> parsing of convensys response took " + tt);
                }
            } catch (Exception e) {
                this.logger.error("<doHandle> received invalid convensys xml", e);
                return new ModelAndView("stkdividenddata", model);
            }

            tt.reset().start();
            final Map<Integer, BigDecimal> dividendsPerShare =
                    extractDividendDataAllYearTypesAccumulated(doc, "//companyprofile/pershare/dividendpershare/year", "amount");
            final Map<Integer, BigDecimal> dividendPayments =
                    extractDividendDataAllYearTypesAccumulated(doc, "//companyprofile/pershare/dividendpayment/year", "amount");
            final Map<Integer, BigDecimal> netIncomes =
                    extractDividendDataAllYearTypesAccumulated(doc, "//companyprofile/incomestatement/netincome/year", "amount");
            final Map<Integer, BigDecimal> earningsPerShare =
                    extractDividendDataAllYearTypesAccumulated(doc, "//companyprofile/pershare/earningpershare/basiceps/year", "amount");
            final Map<Integer, BigDecimal> quotesEndFiscalYear =
                    extractDividendDataAllYearTypesAccumulated(doc, "//companyprofile/stockinformations/quoteendfiscalyear/year", "qt");

            model.putAll(buildModel(dividendsPerShare, dividendPayments, netIncomes, earningsPerShare, quotesEndFiscalYear));
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<doHandle> building of model took " + tt);
            }
        }

        return new ModelAndView("stkdividenddata", model);
    }

    /**
     * Validate convensys response
     */
    private boolean isValidResponse(ModelAndView convensysResponse) {
        return convensysResponse != null
                && convensysResponse.getModel() != null
                && convensysResponse.getModel().get("rawdata") != null
                && convensysResponse.getModel().get("rawdata") instanceof String;
    }

    /**
     * Parse convensys xml
     */
    private Document parseXML(
            String xml) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final InputSource inputSource = new InputSource(new StringReader(xml));
        return builder.parse(inputSource);
    }

    /**
     * Extract dividend data from parsed convensys xml for regular and shortened fiscal year (summed up)     */
    Map<Integer, BigDecimal> extractDividendDataAllYearTypesAccumulated(Document doc, String expressionString,
            String valueAttributeName) {
        Map<Integer, BigDecimal> result =
                extractDividendData(doc, expressionString, "yr", valueAttributeName);
        Map<Integer, BigDecimal> resultShortenedFiscalYear =
                extractDividendData(doc, expressionString, "yrn", valueAttributeName);
        merge(result, resultShortenedFiscalYear);
        return result;
    }

    private void merge(Map<Integer, BigDecimal> fiscalYear, Map<Integer, BigDecimal> shortenedFiscalYear) {
        if (fiscalYear.isEmpty()) {
            fiscalYear.putAll(shortenedFiscalYear);
            return;
        }
        if (shortenedFiscalYear.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, BigDecimal> entry : shortenedFiscalYear.entrySet()) {
            Integer year = entry.getKey();
            BigDecimal fiscalYearValue = fiscalYear.get(year);
            BigDecimal shortenedFiscalYearValue = entry.getValue();
            if (fiscalYearValue == null) {
                fiscalYearValue = shortenedFiscalYearValue;
            } else {
                fiscalYearValue = fiscalYearValue.add(shortenedFiscalYearValue);
            }
            fiscalYear.put(year, fiscalYearValue);
        }
    }

    /**
     * Extract dividend data from parsed convensys xml
     */
    private Map<Integer, BigDecimal> extractDividendData(Document doc, String expressionString,
            String yearAttributeName, String valueAttributeName) {
        final Map<Integer, BigDecimal> result = new TreeMap<>();
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expression = xpath.compile(expressionString + "[@" + yearAttributeName + " and @" + valueAttributeName + "]");
            final NodeList nodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                final String year = node.getAttributes().getNamedItem(yearAttributeName).getNodeValue().trim();
                final String value = node.getAttributes().getNamedItem(valueAttributeName).getNodeValue().trim();
                result.put(Integer.parseInt(year), value.isEmpty() ? null : new BigDecimal(value));
            }
        } catch (Exception e) {
            this.logger.error("<extractDividendData> an error occurred while extracting data from " +
                    "convensys xml via xpath expression " + expressionString, e);
        }
        return result;
    }

    /**
     * Build the model to be passed into the response
     */
    private Map<String, Object> buildModel(Map<Integer, BigDecimal> dividendsPerShare,
            Map<Integer, BigDecimal> dividendPayments,
            Map<Integer, BigDecimal> netIncomes,
            Map<Integer, BigDecimal> earningsPerShare,
            Map<Integer, BigDecimal> quotesEndFiscalYear) {
        final Map<String, Object> model = new HashMap<>();
        final List<DividendData> data =
                dividendsPerShare.keySet()
                        .stream()
                        .map(year -> {
                            BigDecimal dividendPerShare = dividendsPerShare.get(year);
                            BigDecimal dividendPayment = dividendPayments.get(year);
                            return new DividendDataImpl(
                                    year,
                                    dividendPayment,
                                    getDividendPayoutRatio(netIncomes.get(year), dividendPayment),
                                    getDividendYield(dividendPerShare, quotesEndFiscalYear.get(year)),
                                    getDividendCoverage(earningsPerShare.get(year), dividendPerShare),
                                    dividendPerShare,
                                    getDividendPerShareGrowth5y(dividendPerShare, dividendsPerShare.get(year - 5)));
                        })
                        .collect(Collectors.toList());

        model.put("elements", data);
        return model;
    }

    private BigDecimal getDividendPayoutRatio(BigDecimal netIncome, BigDecimal dividendPayment) {
        if (netIncome != null && dividendPayment != null && netIncome.compareTo(BigDecimal.ZERO) != 0) {
            return dividendPayment.divide(netIncome, MC);
        }
        return null;
    }

    private BigDecimal getDividendYield(BigDecimal dividendPerShare,
            BigDecimal quoteEndFiscalYear) {
        if (dividendPerShare != null && quoteEndFiscalYear != null && quoteEndFiscalYear.compareTo(BigDecimal.ZERO) != 0) {
            return dividendPerShare.divide(quoteEndFiscalYear, MC);
        }
        return null;
    }

    private BigDecimal getDividendCoverage(BigDecimal earningsPerShare,
            BigDecimal dividendPerShare) {
        if (earningsPerShare != null && dividendPerShare != null && dividendPerShare.compareTo(BigDecimal.ZERO) != 0) {
            return earningsPerShare.divide(dividendPerShare, MC);
        }
        return null;
    }

    private BigDecimal getDividendPerShareGrowth5y(BigDecimal dividendPerShare,
            BigDecimal dividendPerShare5yAgo) {
        if (dividendPerShare != null && dividendPerShare5yAgo != null && dividendPerShare5yAgo.compareTo(BigDecimal.ZERO) != 0) {
            return dividendPerShare
                    .subtract(dividendPerShare5yAgo)
                    .divide(dividendPerShare5yAgo, MC);
        }
        return null;
    }
}
