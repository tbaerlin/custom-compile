package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Locale;

import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;

/**
 * This class allows the frontend to display XML Requests and Results.
 *
 * @author Ulrich Maurer
 *         Date: 18.01.13
 */
public class XmlDebugHelper {
    public static void handle(HttpServletRequest servletRequest, MmwebRequest request, MmwebResponse response, MoleculeRequest moleculeRequest, String rawResponse) {
        if (request.isWithXmlRequest() && moleculeRequest != null) {
            response.setXmlRequest(createXmlRequest(servletRequest, moleculeRequest));
        }
        if (request.isWithXmlResponse()) {
            response.setXmlResponse(rawResponse);
        }
    }

    private static String createXmlRequest(HttpServletRequest servletRequest, MoleculeRequest mr) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("  <request>\n")
                .append("  <authenticationType>").append(getAuthParam(mr.getAuthenticationType(), servletRequest, ProfileResolver.AUTHENTICATION_TYPE_KEY)).append("</authenticationType>\n")
                .append("  <authentication>").append(getAuthParam(mr.getAuthentication(), servletRequest, ProfileResolver.AUTHENTICATION_KEY)).append("</authentication>\n")
        ;

        final List<Locale> locales = mr.getLocales();
        if (locales != null && !locales.isEmpty()) {
            sb.append("  <locale>");
            String komma = "";
            for (Locale locale : locales) {
                sb.append(komma).append(locale.toString());
                komma = ",";
            }
            sb.append("</locale>\n");
        }

        final String key = mr.getKey();
        if (key != null) {
            sb.append("  <key>").append(key).append("</key>\n");
        }

        for (MoleculeRequest.AtomRequest ar : mr.getAtomRequests()) {
            sb.append("  <block key=\"").append(ar.getName()).append("\"");
            if (ar.getId() != null) {
                sb.append(" id=\"").append(ar.getId()).append("\"");
            }
            sb.append(">\n");
            for (MoleculeRequest.ParameterInfo pi : ar.getParameterInfos()) {
                if (pi.isComplexValue()) {
                    sb.append("    ").append(pi.getValue()).append('\n');

                }
                else {
                    sb.append("    <parameter key=\"").append(pi.getKey()).append("\" value=\"").append(pi.getValue()).append("\">\n");
                }
            }
            sb.append("  </block>\n");
        }

        sb.append("</request>");

        return sb.toString();
    }

    private static String getAuthParam(String value, HttpServletRequest servletRequest, String paramName) {
        if (value != null) {
            return value;
        }
        return HttpRequestUtil.getValue(servletRequest, paramName);

    }
}
