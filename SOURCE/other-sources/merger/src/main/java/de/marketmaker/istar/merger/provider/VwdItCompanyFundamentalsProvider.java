/*
 * CompanyFundamentalsProvider.java
 *
 * Created on 09.08.2006 07:56:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.BasicBalanceFigures;
import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domain.data.ConvensysRawdata;
import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.data.IpoData;
import de.marketmaker.istar.domain.data.ProfitAndLoss;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.vwditcompanydata")
public interface VwdItCompanyFundamentalsProvider {

    /**
     * Returns a string representation of content that has been generated based on convensys data
     * (e.g., an html fragment)
     * @param isin key
     * @param contentKey identifies
     * @return content as string or null if not available
     */
    String getConvensysContent(String isin, String contentKey);

    /**
     * Returns additional items of information for the given isin.
     * Intended to be used in conjunction with #getConvensysContent.
     * @param isin key
     * @return a map - must never be null!
     * @throws UnsupportedOperationException if a provider does not implement this method.
     */
    Map<String,Object> getAdditionalInformation(String isin);

    ConvensysRawdata getPortraitData(String isin);

    byte[] getPortraitPdf(String isin);

    byte[] getOrganisationStructurePdf(String isin);
}
