/*
 * Market.java
 *
 * Created on 11.06.13 09:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;

/**
 * @author Markus
 */
public class Market {
    private String name;
    private String mic;
    private String countryName;
    private String countryCurrencyIso;

    public static MmTalkWrapper<Market> createWrapper(String formula) {
        final MmTalkWrapper<Market> cols = MmTalkWrapper.create(formula, Market.class);
        cols.appendColumnMapper(new MmTalkColumnMapper<Market>("Name") { // $NON-NLS$
            @Override
            public void setValue(Market a, MM item) {
                a.name = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Market>("MIC") { // $NON-NLS$
            @Override
            public void setValue(Market a, MM item) {
                a.mic = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Market>("Land.Name") { // $NON-NLS$
            @Override
            public void setValue(Market a, MM item) {
                a.countryName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Market>("Land.Währung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Market a, MM item) {
                a.countryCurrencyIso = MmTalkHelper.asString(item);
            }
        });
        return cols;
    }

    public String getName() {
        return name;
    }

    public String getMic() {
        return mic;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Market)) return false;

        final Market market = (Market) o;

        if (countryCurrencyIso != null ? !countryCurrencyIso.equals(market.countryCurrencyIso) : market.countryCurrencyIso != null)
            return false;
        if (countryName != null ? !countryName.equals(market.countryName) : market.countryName != null) return false;
        if (mic != null ? !mic.equals(market.mic) : market.mic != null) return false;
        if (name != null ? !name.equals(market.name) : market.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (mic != null ? mic.hashCode() : 0);
        result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
        result = 31 * result + (countryCurrencyIso != null ? countryCurrencyIso.hashCode() : 0);
        return result;
    }
}
