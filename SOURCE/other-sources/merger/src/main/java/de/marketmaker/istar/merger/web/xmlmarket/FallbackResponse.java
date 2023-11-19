package de.marketmaker.istar.merger.web.xmlmarket;


import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class FallbackResponse implements Response {

    @Override
    public void render(PrintWriter writer) {
        final HashMap<String, String> arbitrageMap = new HashMap<>();
        arbitrageMap.put("US89628E1047", "STK");
        arbitrageMap.put("HSH3GP", "BND");
        arbitrageMap.put("EB0CEC", "CER");
        arbitrageMap.put("A0B5UZ", "FND");
        arbitrageMap.put("BP1JQE", "WNT");
        arbitrageMap.put("DE0006056112", "GNS");

        final StringBuilder sb = new StringBuilder();
        final String line1 = "<a href=\"http://xml.market-maker.de/data/arbitrage.xml?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&wkn={0}\">{1}: {0}</a>";
        final String line2 = "<a href=\"/dmxml-1/xmlmarket/arbitrage.xml?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&wkn={0}\">{1}: {0}</a>";

        sb.append("<!DOCTYPE html><html><head><title>testlinks xmlmarket</title></head><body>");

        sb.append("<hr>");
        sb.append("<h3>diff links</h3>");
        sb.append(getDiffLinks());
        sb.append("<hr>");

        sb.append("<h3>arbitrage</h3>");
        sb.append("<table>");
        sb.append("<tr><td><b>old</b></td><td><b>new</b></td></tr>");
        for (Map.Entry<String, String> entry : arbitrageMap.entrySet()) {
            sb.append("<tr><td>");
            sb.append(MessageFormat.format(line1, entry.getKey(), entry.getValue()));
            sb.append("&nbsp;</td>");
            sb.append("<td>&nbsp;");
            sb.append(MessageFormat.format(line2, entry.getKey(), entry.getValue()));
            sb.append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h3>details</h3>");

        sb.append("<h3>intraday</h3>");

        sb.append("<h3>priceSearch</h3>");

        sb.append("<h3>search</h3>");

        sb.append("<hr>");
        sb.append(getStaticClientLinks());
        sb.append("<hr>");

        sb.append("<hr>");

        sb.append("</body></html>");

        writer.print(sb.toString());
    }

    private String getDiffLinks() {
        final StringBuilder sb = new StringBuilder();
        MessageFormat f1,f2;
        String[] iids;

        sb.append("<h1>diffs</h1>");

        // arbitrage
        sb.append("<ul>");
        f1 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&wkn={0}&ep=arbitrage\">{0}</a>");

        sb.append("<li>arbitrage: " + f1.format(new Object[] {"US89628E1047"}) + "</li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"HSH3GP"}) + " (time) </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"EB0CEC"}) + " (supplement) </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"A1CV78"}) + " </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"A0B5UZ"}) + " </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"BP1JQE"}) + " </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"DE0006056112"}) + " </li>");

        sb.append("<li>arbitrage: " + f1.format(new Object[] {"DE0005933931"}) + " (second symbol)</li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"AT0000936513"}) + " (currencyCode order)</li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"AT0000A00Y78"}) + " </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"AU000000DYE9"}) + " </li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"CH0184305016"}) + " </li>");

        sb.append("<li>arbitrage: " + f1.format(new Object[] {"DE0005316962"}) + " (second symbol)</li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"DE0005508105"}) + " (can't find WKN)</li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"DE0005933931"}) + " (secondSymbol, currencyCode order)</li>");
        sb.append("<li>arbitrage: " + f1.format(new Object[] {"514000"}) + " </li>");

        sb.append("<li>arbitrage: " + f1.format(new Object[] {"DE0006026701"}) + " </a></li>");
        sb.append("</ul>");


        // search
        sb.append("<ul>");
        f1 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&ep=search&searchFor={0}\">{0}</a></a>");
        f2 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&ep=search&searchFor={0}&wpart={1}\">{0}, wpart:{1}</a></a>");

        sb.append("<li>search: " + f1.format(new Object[] {"Bank"}) + " </li>");
        sb.append("<li>search: " + f2.format(new Object[] {"Bank", "aktie"}) + "</li>");
        sb.append("<li>search: " + f1.format(new Object[] {"Qatar"}) + "</li>");
        sb.append("<li>search: " + f1.format(new Object[] {"Google"}) + "</li>");
        sb.append("<li>search: " + f1.format(new Object[] {"Daimler"}) + "</li>");
        sb.append("<li>search: " + f1.format(new Object[] {"Benz"}) + "</li>");
        sb.append("</ul>");


        // intraday
        sb.append("<ul>");
        f1 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&ep=intraday&wkn={0}&platz=MCH&numResults=5\">{0}</a>");
        f2 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt&ep=intraday&wkn={0}&platz={1}&numResults=5\">{0}, platz:{1}</a>");

        sb.append("<li>intraday Aktie/STK: " + f1.format(new Object[] {"DE0005557508"}) + " (estimatedDividend, dividend)</a></li>");
        sb.append("<li>intraday Rente/BND: " + f1.format(new Object[] {"IE00B60Z6194"}) + " (defaultExchange, nominal, premium, interest)</a></li>");
        sb.append("<li>intraday Fonds/Optionsschein/CER: " + f1.format(new Object[] {"EB0CEC"}) + " (typeUnderlying, ) </a></li>");
        sb.append("<li>intraday Fonds/Optionsschein/FND: " + f1.format(new Object[] {"A1CV78"}) + " </a></li>");
        sb.append("<li>intraday Optionsschein/WNT: " + f1.format(new Object[] {"BP1JQE"}) + " </a></li>");
        sb.append("<li>intraday Genussschein/GNS: " + f1.format(new Object[] {"DE0006056112"}) + " </a></li>");

        iids = new String[] {"US89628E1047", "HSH3GP", "514000", "DE0005933931", "DE0005508105", "DE0005316962", "CH0184305016", "AU000000DYE9"};
        for (String s : iids) {
            sb.append("<li> intraday: " + f1.format(new Object[] {s}) + " </a></li>");
        }
        sb.append("</ul>");

        // pricesearch
        sb.append("<ul>");
        f1 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=2179620d6d76db77e5c27d3aebbe68dd1bba5f2b0e44e7029b919afcb12680d3&uname=aab-nt&ep=pricesearch&searchFor={0}&wmtype=Anleihe%3BAktie%3BZertifikat%3BGenussschein%3BOptionsschein%3BFonds&startAt=0\">{0}</a></a>");

        iids = new String[] {
                "US89628E1047", "HSH3GP", "514000", "DE0005933931", "DE0005508105",
                "DE0005316962", "CH0184305016", "AU000000DYE9",
                // testingbugfixes:
                "DE000LS9AAJ6",
                "a0mnbv",
                "114160",
                "A1AM06",
        };
        for (String s : iids) {
            sb.append("<li> pricesearch: " + f1.format(new Object[] {s}) + " </a></li>");
        }
        sb.append("</ul>");


        // details
        sb.append("<ul>");
        f1 = new MessageFormat("<a href=\"/dmxml-1/xmlmarket/diff.html?ukey=2179620d6d76db77e5c27d3aebbe68dd1bba5f2b0e44e7029b919afcb12680d3&uname=aab-nt&ep=details&wkn={0}&platz=MCH\">{0}</a></a>");

        iids = new String[] {
                "DE0005557508", "IE00B60Z6194", "EB0CEC", "BP1JQE", "DE0006056112",
                "US89628E1047", "HSH3GP", "514000", "DE0005933931", "DE0005508105",
                "DE0005316962", "AU000000DYE9",
        // testing bugfixes:
                "A0JC8V",
                "UX128X",
                "UX1Y53",
                "UX12RS",
                "DT3TWS",
                "A0V9X3",
                "DE000LS9AAJ6",
                "DE000BP87TR2",
                "789125",
                "LU0157922724",
                "AGP8696W1045",
                "114160",
                "840400",
                "847100",
                "114160",
                "A0Q8T6"
        };
        for (String s : iids) {
            sb.append("<li> details: " + f1.format(new Object[] {s}) + " </a></li>");
        }

        sb.append("<li> details FND/!$data.Platz.startsWith(\"FONDS\"): " + f1.format(new Object[] {"A1CV78"}) + " </a></li>");
        sb.append("<li> details FND/$data.Platz.startsWith(\"FONDS\"): " + f1.format(new Object[] {"CH0184305016"}) + " </a></li>");

        sb.append("</ul>");

        return sb.toString();
    }


    private String getStaticClientLinks() {
        final StringBuilder sb = new StringBuilder();

        sb.append("<h1>the clients</h1>");

        String authParams1 = "ukey=3134994f33abc83c2df5ba4af1edf05bd5c6edb2a91968dcaadb4e92ffaf07b5&uname=1822_nt";
        String authParams2 = "ukey=eabb2b3cb46f290605075655cb9dc54551e976b4fc0d5b6f93b537864a8bbd4f&uname=1822_rt";
        sb.append("<h2>1822</h2>");
        sb.append("<ul>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/arbitrage.xml?" + authParams1 + "&wkn=US89628E1047\">http://xml.market-maker.de/data/arbitrage.xml</a></li>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/search.xml?" + authParams1 + "&searchFor=DE0005232805\">http://xml.market-maker.de/data/search.xml</a></li>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/intraday.xml?" + authParams2 + "&wkn=IE00B60Z6194&platz=MCH&numResults=5\">http://xml.market-maker.de/data/intraday.xml:IE00B60Z6194 (1822_rt)</a></li>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/intraday.xml?" + authParams2 + "&wkn=DE0005557508&platz=MCH&numResults=5\">http://xml.market-maker.de/data/intraday.xml:DE0005557508 (1822_rt)</a></li>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/intraday.xml?" + authParams1 + "&wkn=DE0005557508&platz=MCH&numResults=5\">http://xml.market-maker.de/data/intraday.xml:DE0005557508 (1822_nt)</a></li>");
        sb.append("</ul>");
        sb.append("<ul>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/arbitrage.xml?" + authParams1 + "&wkn=US89628E1047\">/dmxml-1/xmlmarket/arbitrage.xml</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/search.xml?" + authParams1 + "&searchFor=DE0005232805\">/dmxml-1/xmlmarket/search.xml</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/intraday.xml?" + authParams2 + "&wkn=IE00B60Z6194&platz=MCH&numResults=5\">/dmxml-1/xmlmarket/intraday.xml:IE00B60Z6194 (1822_rt)</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/intraday.xml?" + authParams2 + "&wkn=DE0005557508&platz=MCH&numResults=5\">/dmxml-1/xmlmarket/intraday.xml:DE0005557508 (1822_rt)</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/intraday.xml?" + authParams1 + "&wkn=DE0005557508&platz=MCH&numResults=5\">/dmxml-1/xmlmarket/intraday.xml:DE0005557508 (1822_nt)</a></li>");
        sb.append("</ul>");

        String authParams3 = "ukey=2179620d6d76db77e5c27d3aebbe68dd1bba5f2b0e44e7029b919afcb12680d3&uname=aab-nt";
        sb.append("<h2>aab</h2>");
        sb.append("<ul>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/pricesearch.xml?" + authParams3 + "&searchFor=Ua3e1j&wmtype=Anleihe%3BAktie%3BZertifikat%3BGenussschein%3BOptionsschein%3BFonds&startAt=0\">http://xml.market-maker.de/data/pricesearch.xml searchFor=Ua3e1j</a></li>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/pricesearch.xml?" + authParams3 + "&searchFor=Bank&wmtype=Anleihe%3BAktie%3BZertifikat%3BGenussschein%3BOptionsschein%3BFonds&startAt=0\">http://xml.market-maker.de/data/pricesearch.xml searchFor=Bank</a></li>");
        sb.append("<li><a href=\"http://xml.market-maker.de/data/details.xml?" + authParams3 + "&wkn=LU0055114457&platz=FONDS.USD\">http://xml.market-maker.de/data/details.xml</a></li>");
        sb.append("</ul>");
        sb.append("<ul>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/pricesearch.xml?" + authParams3 + "&searchFor=Ua3e1j&wmtype=Anleihe%3BAktie%3BZertifikat%3BGenussschein%3BOptionsschein%3BFonds&startAt=0\">aab-nt: /dmxml-1/xmlmarket/pricesearch.xml</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/details.xml?" + authParams3 + "&wkn=LU0055114457&platz=FONDS.USD\">aab-nt: /dmxml-1/xmlmarket/details.xml</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/pricesearch.xml?" + authParams3 + "&searchFor=Ua3e1j&wmtype=Anleihe%3BAktie%3BZertifikat%3BGenussschein%3BOptionsschein%3BFonds&startAt=0\">aab-nt: /dmxml-1/xmlmarket/pricesearch.xml searchFor=Ua3e1j</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/pricesearch.xml?" + authParams3 + "&searchFor=Bank&wmtype=Anleihe%3BAktie%3BZertifikat%3BGenussschein%3BOptionsschein%3BFonds&startAt=0\">aab-nt: /dmxml-1/xmlmarket/pricesearch.xml searchFor=Bank</a></li>");
        sb.append("<li><a href=\"/dmxml-1/xmlmarket/details.xml?" + authParams3 + "&wkn=LU0055114457&platz=FONDS.USD\">aab-nt: /dmxml-1/xmlmarket/details.xml</a></li>");
        sb.append("</ul>");

        return sb.toString();
    }
}
