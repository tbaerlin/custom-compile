package de.marketmaker.istar.merger.provider.pages;

import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ReutersPageProvider extends AbstractGisPageProvider {
    public ReutersPageProvider() {
        super("http://finance.gis-online.de/gisweb/sims");
    }

    protected String getPageInternal(String xsession, String pagenumber) throws Exception {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(this.uri)
                .queryParam("xsession", xsession)
                .queryParam("id", pagenumber);

        return this.restTemplate.getForObject(b.build().toUri(), String.class);
    }

    protected String convertPage(String page) {
        final int start = page.indexOf("<PRE>");
        final int end = page.lastIndexOf("</PRE>");
        return start > 0 && end > 0 ? page.substring(start, end + "</PRE>".length()) : page;
    }

    public static void main(String[] args) throws Exception {
        RestTemplateFactory rtf = new RestTemplateFactory();
        final ReutersPageProvider provider = new ReutersPageProvider();
        provider.setRestTemplate(rtf.getObject());

        final String s = provider.getPage("RSF.PAGE.DEMARKETS/SB_EMMA1=DZFT.GENO");
        System.out.println(s);
        rtf.destroy();
    }
}