package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitBNDController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitCERController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitCURController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitFNDController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitFUTController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitGNSController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitINDController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitMERController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitMKController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitOPTController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitPEController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitSTKController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitWNTController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitZNSController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AlertSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AlertsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AnalyserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ArbitrageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CerAnalogInstrumentsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartcenterSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DependentValuesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DividendChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DividendListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DividendOverviewSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.EdgRatingSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.EdgRatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FavouritesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.HighchartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ListDetailsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsrelatedQuotesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.OHLCVSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.OrderbookSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PdfSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceEarningsChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.RatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.RegulatoryReportingSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.RegulatoryReportingSnippet.Class;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SimpleHtmlSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCERSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticTextSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StructDataSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StructPieSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolLinkSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TimesAndSalesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TopProductsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis.AnalysisDetailsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis.AnalysisListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.bond.BNDRatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate.BasketSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate.CerAnnotationSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.certificate.CerUnderlyingSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.docman.DocmanArchiveSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.docman.DocmanCreateSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.FNDYieldsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.FeesAndEarningsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.FndRatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.FundStrategySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.FundprospectKWTSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.LastUpdateSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.ReportsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.SellHoldBuyCoeffGraphSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.SellHoldBuyGraphSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.StaticDataSTKSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.convensys.ConvensysPortraitSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates.*;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.ilsole24ore.IlSole24OrePortraitSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener.ScreenerAlternativesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener.ScreenerAnalysisSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener.ScreenerRiskSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener.ScreenerStaticDataSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.warrant.MiniWntRatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.warrant.WntRatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Ulrich Maurer
 *         Date: 29.10.12
 */
public class SimpleControllerInitializer {
    public static void initSnippetClasses() {
        SnippetClass.addClass(new AlertSnippet.Class());
        if(SessionData.isAsDesign()) {
            SnippetClass.addClass(new AlertsSnippet.Class());
            SnippetClass.addClass(new FavouritesSnippet.Class());
        }
        SnippetClass.addClass(new AnalyserSnippet.Class());
        SnippetClass.addClass(new AnalysisDetailsSnippet.Class());
        SnippetClass.addClass(new AnalysisListSnippet.Class());
        SnippetClass.addClass(new ArbitrageSnippet.Class());
        SnippetClass.addClass(new BasketSnippet.Class());
        SnippetClass.addClass(new BNDRatiosSnippet.Class());
        SnippetClass.addClass(new CerAnalogInstrumentsSnippet.Class());
        SnippetClass.addClass(new CerAnnotationSnippet.Class());
        SnippetClass.addClass(new CerUnderlyingSnippet.Class());
        SnippetClass.addClass(new ChartcenterSnippet.Class());
        SnippetClass.addClass(new ConvensysPortraitSnippet.Class());
        SnippetClass.addClass(new DependentValuesSnippet.Class());
        SnippetClass.addClass(new DividendChartSnippet.Class());
        SnippetClass.addClass(new DividendListSnippet.Class());
        SnippetClass.addClass(new DividendOverviewSnippet.Class());
        SnippetClass.addClass(new DocmanArchiveSnippet.Class());
        SnippetClass.addClass(new DocmanCreateSnippet.Class());
        SnippetClass.addClass(new EdgRatingSnippet.Class());
        SnippetClass.addClass(new EdgRatiosSnippet.Class());
        SnippetClass.addClass(new FeesAndEarningsSnippet.Class());
        SnippetClass.addClass(new FndRatiosSnippet.Class());
        SnippetClass.addClass(new FNDYieldsSnippet.Class());
        SnippetClass.addClass(new FundprospectKWTSnippet.Class());
        SnippetClass.addClass(new FundStrategySnippet.Class());
        SnippetClass.addClass(new HighchartSnippet.Class());
        SnippetClass.addClass(new IlSole24OrePortraitSnippet.Class());
        SnippetClass.addClass(new LastUpdateSnippet.Class());
        SnippetClass.addClass(new ListDetailsSnippet.Class());
        SnippetClass.addClass(new MiniWntRatiosSnippet.Class());
        SnippetClass.addClass(new NewsEntrySnippet.Class());
        SnippetClass.addClass(new NewsHeadlinesSnippet.Class());
        SnippetClass.addClass(new NewsrelatedQuotesSnippet.Class());
        SnippetClass.addClass(new OHLCVSnippet.Class());
        SnippetClass.addClass(new OrderbookSnippet.Class());
        SnippetClass.addClass(new PdfSnippet.Class());
        SnippetClass.addClass(new PortraitChartSnippet.Class());
        SnippetClass.addClass(new PriceEarningsChartSnippet.Class());
        SnippetClass.addClass(new PriceSnippet.Class());
        SnippetClass.addClass(new PriceTeaserSnippet.Class());
        SnippetClass.addClass(new RatiosSnippet.Class());
        SnippetClass.addClass(new RegulatoryReportingSnippet.Class());
        SnippetClass.addClass(new ReportsSnippet.Class());
        SnippetClass.addClass(new ScreenerStaticDataSnippet.Class());
        SnippetClass.addClass(new ScreenerAlternativesSnippet.Class());
        SnippetClass.addClass(new ScreenerAnalysisSnippet.Class());
        SnippetClass.addClass(new ScreenerRiskSnippet.Class());
        SnippetClass.addClass(new SellHoldBuyCoeffGraphSnippet.Class());
        SnippetClass.addClass(new SellHoldBuyGraphSnippet.Class());
        SnippetClass.addClass(new SimpleHtmlSnippet.Class());
        SnippetClass.addClass(new StaticDataSnippet.Class());
        SnippetClass.addClass(new StaticTextSnippet.Class());
        SnippetClass.addClass(new StaticDataCERSnippet.Class());
        SnippetClass.addClass(new StaticDataSTKSnippet.Class());
        SnippetClass.addClass(new STKEstimatesBrokerSnippet.Class());
        SnippetClass.addClass(new STKEstimatesGraphSnippet.Class());
        SnippetClass.addClass(new STKEstimatesHistorySnippet.Class());
        SnippetClass.addClass(new STKEstimatesRatiosSnippet.Class());
        SnippetClass.addClass(new STKEstimatesRatiosTrendSnippet.Class());
        SnippetClass.addClass(new STKEstimatesTrendSnippet.Class());
        SnippetClass.addClass(new StructDataSnippet.Class());
        SnippetClass.addClass(new StructPieSnippet.Class());
        SnippetClass.addClass(new SymbolLinkSnippet.Class());
        SnippetClass.addClass(new TimesAndSalesSnippet.Class());
        SnippetClass.addClass(new TopProductsSnippet.Class());
        SnippetClass.addClass(new WntRatiosSnippet.Class());
    }

    public static void initControllers(AbstractMainController controller) {
        final ContentContainer cc = controller.getView();
        controller.addControllerCheckJson(false, "P_BND", new PortraitBNDController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_CER", new PortraitCERController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_FND", new PortraitFNDController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_FUT", new PortraitFUTController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_MER", new PortraitMERController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_MK", new PortraitMKController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_ZNS", new PortraitZNSController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_GNS", new PortraitGNSController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_OPT", new PortraitOPTController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_PE", new PortraitPEController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_STK", new PortraitSTKController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_UND", new PortraitFUTController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_WNT", new PortraitWNTController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_CUR", new PortraitCURController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "P_IND", new PortraitINDController(cc)); // $NON-NLS$

        controller.addControllerCheckJson(false, "N_D", new NewsDetailController(cc)); // $NON-NLS$
        controller.addControllerCheckJson(false, "M_S", new PriceSearchController(cc)); // $NON-NLS$
    }

}
