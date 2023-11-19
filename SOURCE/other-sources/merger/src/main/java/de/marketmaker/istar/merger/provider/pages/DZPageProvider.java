package de.marketmaker.istar.merger.provider.pages;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DZPageProvider extends AbstractGisPageProvider {
    private static final Matcher SORT_MATCHER = Pattern.compile("javascript:setNewUrl\\('/gisweb/vwd.do.*ql=(.*)&style=.*&B:(.*)'\\)").matcher("");

    private static final String SORT_PATTERN = "javascript:setNewUrl('/gisweb";

    private static final Matcher JS_LINK_MATCHER = Pattern.compile("<a href=\"javascript:window.opener.parent.location.href = '.*'; self.close\\(\\);\">([^<]*)</a>").matcher("");

    private static final String JS_LINK_PATTERN = "<a href=\"javascript:window.opener.parent.location.href = ";

    private static final String PATTERN = "href=\"/gisweb/";

    private static final String ISO_8859_1 = "ISO-8859-1";

    private String defaultUri;

    public DZPageProvider() {
        super("http://finance.gis-online.de/gisweb/");
        this.defaultUri = this.uri + "vwd.do";
    }

    protected String getPageInternal(String xsession, String pagenumber) throws Exception {
        if (pagenumber.toUpperCase().startsWith("X")) {
            return "Seite nicht freigeschaltet.";
        }

        final String id = pagenumber.toUpperCase();

        UriComponentsBuilder b;
        if (id.contains("%23") || id.contains("#")) {
            b = UriComponentsBuilder.fromHttpUrl(this.defaultUri);
            if (id.contains("&B:")) {
                final String[] tokens = id.split("&B:");
                b.queryParam("ql", URLDecoder.decode(tokens[0], ISO_8859_1));

                final String[] keyValue = tokens[1].split("=");
                b.queryParam("B:" + keyValue[0], keyValue[1]);
            }
            else {
                b.queryParam("ql", URLDecoder.decode(id, ISO_8859_1));
            }
            b.queryParam("style", "vrbapo");
        }
        else {
            b = UriComponentsBuilder.fromHttpUrl(this.uri + "vwd_" + id + ".html");
        }
        b.queryParam("xsession", xsession);
        URI uri = b.build().toUri();

        final String result = getForObjectIgnoreNotFound(uri);
        if (result != null) {
            return result;
        }

        return getWithQuickcode(xsession, pagenumber);
    }

    private String getForObjectIgnoreNotFound(URI uri) {
        try {
            return this.restTemplate.getForObject(uri, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    private String getWithQuickcode(String xsession, String pagenumber) throws Exception {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(this.defaultUri)
                .queryParam("xsession", xsession)
                .queryParam("style", "vrbapo")
                .queryParam("quickcode", pagenumber);

        return this.restTemplate.getForObject(b.build().toUri(), String.class);
    }

    protected String convertPage(String page) throws UnsupportedEncodingException {
        final StringBuilder sb = new StringBuilder(page.length());

        final Scanner s = new Scanner(page);
        while (s.hasNextLine()) {
            int index;

            final String line = replaceServer(s.nextLine());

            if (line.trim().length() == 0) {
                continue;
            }
            if (line.contains("collection.js")) {
                continue;
            }
            if (line.contains("<link rel=stylesheet")) {
                continue;
            }

            index = line.indexOf(JS_LINK_PATTERN);
            if (index >= 0) {
                if (JS_LINK_MATCHER.reset(line).find()) {
                    sb.append(JS_LINK_MATCHER.replaceAll("$1"));
                    sb.append("\n");
                    continue;
                }
            }

            index = line.indexOf(PATTERN);
            if (index < 0) {
                final int sortIndex = line.indexOf(SORT_PATTERN);
                if (sortIndex >= 0) {
                    final String prefix = line.substring(0, sortIndex);
                    final int hrefEndPos = line.indexOf("\"", sortIndex + 2);
                    final String href = line.substring(sortIndex, hrefEndPos);
                    final String suffix = line.substring(hrefEndPos);
                    final String decodedHref = URLDecoder.decode(href, ISO_8859_1);
                    if (SORT_MATCHER.reset(decodedHref).find()) {
                        sb.append(prefix).append("#P_D/")
                                .append(SORT_MATCHER.group(1)).append("&B:").append(SORT_MATCHER.group(2))
                                .append(suffix);
                        continue;
                    }
                }

                sb.append(line).append("\n");
                continue;
            }

            final String pageid = line.substring(index + PATTERN.length());
            final String rest;
            if (pageid.startsWith("vwd_")) {
                final int end = pageid.indexOf(".html");
                rest = pageid.substring(4, end) + pageid.substring(end + 5);
            }
            else {
                final int indexSlash = pageid.indexOf("\"");
                final int indexAnd = pageid.indexOf("&");
                rest = pageid.substring(pageid.indexOf("=") + 1,
                        indexAnd < 0 || indexAnd > indexSlash ? indexSlash : indexAnd)
                        + pageid.substring(indexSlash);
            }

            sb.append(line.substring(0, index)).append("href=\"#P_D/").append(rest).append("\n");
        }
        s.close();

        return sb.toString();
    }

    private String replaceServer(String line) {
        if (line.contains("/~server/gisweb/style")) {
            return line.replace("/~server/gisweb/", "");
        }
        if (line.contains("/~server/gisweb/image")) {
            return line.replace("/~server/gisweb/", "http://finance.gis-online.de/~server/gisweb/");
        }
        if (line.contains("href=\"/vrbp/")) {
            return line.replace("href=\"/vrbp/", "href=\"https://www.vr-bankenportal.de/vrbp/");
        }
        return line;
    }

    public static void main(String[] args) throws Exception {
        RestTemplateFactory rtf = new RestTemplateFactory();
        rtf.setDefaultSoTimeout(15000);
        final DZPageProvider provider = new DZPageProvider();
        provider.setRestTemplate(rtf.getObject());

//        final String s = provider.convertPage(FileCopyUtils.copyToString(new FileReader("d:/temp/dz541.html")));
        final String s = provider.getPage("D340");
//        final String s = provider.getPage("RSF.LINK.L0%23JUMBO%3DDZFT.GENO");
//        final String s = provider.getPage("RSF.LINK.L0%23EURIBOR%3DDZFT.GNOFM");
        System.out.println(s);

        rtf.destroy();
    }
}