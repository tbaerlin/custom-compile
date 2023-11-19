/*
 * SnippetInitializerCommand.java
 *
 * Created on 06.07.2016 09:05
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.mmgwt.mmweb.client.customer.apo.ApoFundPerformanceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.olb.OlbFundSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz.WGZCertificateListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz.WGZCertificateOverviewSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.economic.CountryListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.economic.EconomicChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.economic.TypeListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.BestToolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.BndListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CalendarSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CapitalMarketFavoritesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CerListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartRatioUniverseSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CompactQuoteSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurablePriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CrossrateSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CurrencyCalculatorSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzNewsRelatedOffersSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzPageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FinderGroupsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FlipChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.GisStaticDataSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ImageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.JsonListsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.JsonMultiListDetailsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.JsonTreeSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MarketOverviewSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MostActiveWNTSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MultiFinderGroupsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MultiPriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NationalEconomyMatrixSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NationalEconomyTableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewCERSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsSearchParamsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PdfOptionSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PerformanceCalculatorSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PerformanceCalculatorToolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortfolioSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticSymbolsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StkListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TopFlopSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TopRatingSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.UnderlyingListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.VwdPageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.WatchlistSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.YieldsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis.AnalystListSnippet;

/**
 * @author mdick
 */
public class SnippetInitializerCommand implements Command {
    @Override
    public void execute() {
        SimpleControllerInitializer.initSnippetClasses();

        SnippetClass.addClass(new AnalystListSnippet.Class());
        SnippetClass.addClass(new ApoFundPerformanceSnippet.Class());
        SnippetClass.addClass(new BestToolSnippet.Class());
        SnippetClass.addClass(new CalendarSnippet.Class());
        SnippetClass.addClass(new CapitalMarketFavoritesSnippet.Class());
        SnippetClass.addClass(new DzNewsRelatedOffersSnippet.Class());
        SnippetClass.addClass(new DzPageSnippet.Class());
        SnippetClass.addClass(new GisStaticDataSnippet.Class());
        SnippetClass.addClass(new CerListSnippet.Class());
        SnippetClass.addClass(new ChartListSnippet.Class());
        SnippetClass.addClass(new ChartRatioUniverseSnippet.Class());
        SnippetClass.addClass(new CompactQuoteSnippet.Class());
        SnippetClass.addClass(new ConfigurablePriceListSnippet.Class());
        SnippetClass.addClass(new CountryListSnippet.Class());
        SnippetClass.addClass(new CrossrateSnippet.Class());
        SnippetClass.addClass(new CurrencyCalculatorSnippet.Class());
        SnippetClass.addClass(new EconomicChartSnippet.Class());
        SnippetClass.addClass(new FinderGroupsSnippet.Class());
        SnippetClass.addClass(new FlipChartSnippet.Class());
        SnippetClass.addClass(new ImageSnippet.Class());
        SnippetClass.addClass(new JsonListsSnippet.Class());
        SnippetClass.addClass(new JsonMultiListDetailsSnippet.Class());
        SnippetClass.addClass(new JsonTreeSnippet.Class());
        SnippetClass.addClass(new MarketOverviewSnippet.Class());
        SnippetClass.addClass(new MultiFinderGroupsSnippet.Class());
        SnippetClass.addClass(new MostActiveWNTSnippet.Class());
        SnippetClass.addClass(new MultiPriceListSnippet.Class());
        SnippetClass.addClass(new NationalEconomyMatrixSnippet.Class());
        SnippetClass.addClass(new NationalEconomyTableSnippet.Class());
        SnippetClass.addClass(new NewCERSnippet.Class());
        SnippetClass.addClass(new NewsSearchParamsSnippet.Class());
        SnippetClass.addClass(new OlbFundSnippet.Class());
        SnippetClass.addClass(new PdfOptionSnippet.Class());
        SnippetClass.addClass(new PerformanceCalculatorSnippet.Class());
        SnippetClass.addClass(new PerformanceCalculatorToolSnippet.Class());
        SnippetClass.addClass(new PortfolioSnippet.Class());
        SnippetClass.addClass(new PriceListSnippet.Class());
        SnippetClass.addClass(new StaticSymbolsSnippet.Class());
        SnippetClass.addClass(new TopFlopSnippet.Class());
        SnippetClass.addClass(new TopRatingSnippet.Class());
        SnippetClass.addClass(new TypeListSnippet.Class());
        SnippetClass.addClass(new UnderlyingListSnippet.Class());
        SnippetClass.addClass(new VwdPageSnippet.Class());
        SnippetClass.addClass(new WatchlistSnippet.Class());
        SnippetClass.addClass(new WGZCertificateListSnippet.Class());
        SnippetClass.addClass(new WGZCertificateOverviewSnippet.Class());
        SnippetClass.addClass(new YieldsSnippet.Class());
        SnippetClass.addClass(new StkListSnippet.Class());
        SnippetClass.addClass(new BndListSnippet.Class());
    }
}
