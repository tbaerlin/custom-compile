/*
 * Renderer.java
 *
 * Created on 05.06.2008 16:56:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.DzPibDownloadLinkRenderer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Renderer<T> {
    LargeNumberRenderer.LargeNumberLabels LARGE_NUMBER_LABELS =
        new LargeNumberRenderer.LargeNumberLabels(
            I18n.I.millionAbbr(),
            I18n.I.billionAbbr(),
            I18n.I.trillionAbbr(),
            I18n.I.quadrillionAbbr()
        );
    PriceStringRenderer PRICE = new PriceStringRenderer();
    PriceWithSupplementRenderer PRICE_WITH_SUPPLEMENT = new PriceWithSupplementRenderer();
    PriceStringRenderer PRICE23 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_2_3, "--"); // $NON-NLS-0$
    PriceStringRenderer PRICE25 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_2_5, "--"); // $NON-NLS-0$
    PriceWithCurrencyRenderer PRICE23_WITH_CURRENCY = new PriceWithCurrencyRenderer(PRICE23);
    PriceStringRenderer PRICE2 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_2, "--"); // $NON-NLS-0$
    PriceStringRenderer PRICE3 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_3, "--"); // $NON-NLS-0$
    PriceStringRenderer PRICE_0MAX5 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_0_5, "--"); // $NON-NLS-0$
    PriceStringRenderer PRICE_MAX2 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_0_2, "--"); // $NON-NLS-0$
    PriceStringRenderer PRICE_MAX1 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_0_1, "--"); // $NON-NLS-0$
    PercentRenderer PERCENT = new PercentRenderer();
    PercentRenderer PERCENT_NO_SHIFT = new PercentRenderer(StringBasedNumberFormat.ROUND_2, "--", true, 0);
    PercentRenderer PERCENT23 = new PercentRenderer(StringBasedNumberFormat.ROUND_2_3, "--", true); // $NON-NLS-0$
    PercentRenderer PERCENT_INT = new PercentRenderer(StringBasedNumberFormat.ROUND_0_2, "--", true); // $NON-NLS-0$
    @SuppressWarnings("unused")
    PriceStringRenderer LARGE_PRICE = new PriceStringRenderer(StringBasedNumberFormat.ROUND_0, "--"); // $NON-NLS-0$
    PriceStringRenderer LARGE_PRICE_MAX2 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_2, "--"); // $NON-NLS-0$
    PriceStringRenderer LARGE_PRICE_0MAX5 = new PriceStringRenderer(StringBasedNumberFormat.ROUND_0_5, "--"); // $NON-NLS-0$
    ChangeRenderer CHANGE_PRICE = new ChangeRenderer(new PriceStringRenderer());
    ChangeRenderer CHANGE_LARGE_PRICE_MAX2 = new ChangeRenderer(LARGE_PRICE_MAX2);
    ChangeRenderer CHANGE_PERCENT = new ChangeRenderer(new PercentRenderer());
    LargeNumberRenderer LARGE_NUMBER = new LargeNumberRenderer();
    LargeNumberRenderer VOLUME = LARGE_NUMBER;
    LargeNumberRenderer TURNOVER = LARGE_NUMBER;
    LargeLongRenderer VOLUME_LONG = new LargeLongRenderer(LARGE_NUMBER);
    SupplementRenderer SUPPLEMENT = new SupplementRenderer();
    SellHoldBuyRenderer SELL_HOLD_BUY = new SellHoldBuyRenderer();
    RecommendationRenderer RSC_RECOMMENDATION = RecommendationRenderer.createRscRecommendationRenderer();
    TrendBarRenderer TRENDBAR = new TrendBarRenderer();
    ImageRenderer IMAGE = new ImageRenderer();
    ExtendBarRenderer EXTEND_BAR_LEFT = new ExtendBarRenderer(ExtendBarRenderer.STYLE_LEFT);
    ExtendBarRenderer EXTEND_BAR_RIGHT = new ExtendBarRenderer(ExtendBarRenderer.STYLE_RIGHT);
    CertificateCategoryRenderer CERTIFICATE_CATEGORY = new CertificateCategoryRenderer();
    CertLeverageTypeRenderer CERT_LEVERAGE_TYPE = new CertLeverageTypeRenderer();
    WarrantTypeRenderer WARRANT_TYPE = new WarrantTypeRenderer();
    @SuppressWarnings("unused")
    BviFundTypeRenderer BVI_FUNDTYPE = new BviFundTypeRenderer();
    TooltipRenderer TOOLTIP = new TooltipRenderer();
    @SuppressWarnings("unused")
    DzPibDownloadLinkRenderer DZ_PIB_DOWNLOAD_LINK_RENDERER = new DzPibDownloadLinkRenderer();
    StringRenderer STRING_DOUBLE_DASH = new StringRenderer("--"); // $NON-NLS$
    BooleanYesNoRenderer BOOLEAN_YES_NO_RENDERER = new BooleanYesNoRenderer();

    String render(T t);
}
