/*
 * LogWindow.java
 *
 * Created on 09.10.2008 11:21:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.Parameter;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.dmxml.RequestedBlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.pmxml.MMTalkRequest;
import de.marketmaker.iview.pmxml.QueryMMTalk;
import de.marketmaker.iview.pmxml.QueryStandard;
import de.marketmaker.iview.pmxml.TableTreeElement;
import de.marketmaker.iview.pmxml.TableTreeFormula;
import de.marketmaker.iview.pmxml.TableTreeTable;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
@NonNLS
@SuppressWarnings({"GWTStyleCheck"})
public class LogWindow {
    private static final String STYLE_ENTRY = "logentry";

    private static ActiveWindow window = null;

    private static boolean modeXml = true;

    private static boolean withResponse = false;

    private static final HashMap<Class, ParameterCallback> MAP_PARAM_CALLBACKS = new HashMap<>();

    static {
        addCallback(Parameter.class, new ParameterCallback<Parameter>() {
            @Override
            public void addTable(StringBuilder sb, Parameter param) {
                sb.append(param.getValue());
            }

            @Override
            public void addXml(StringBuilder sb, Parameter param) {
                sb.append("    <parameter key=\"").append(param.getKey()).append("\" value=\"")
                        .append(param.getValue().replace("&", "&amp;").replace("<", "&lt;"))
                        .append("\"/>\n");
            }
        });

        addCallback(MMTalkRequest.class, new ParameterCallback<MMTalkRequest>() {
            @Override
            public void addTable(StringBuilder sb, MMTalkRequest param) {
                sb.append("__ complex MMTalkRequest __");
            }

            @Override
            public void addXml(StringBuilder sb, MMTalkRequest param) {
                renderMMTalkRequest(sb, param);
            }
        });
    }

    public interface ParameterCallback<T extends Parameter> {
        void addTable(StringBuilder sb, T t);

        void addXml(StringBuilder sb, T t);
    }

    public static <P extends Parameter> void addCallback(Class clazz,
            ParameterCallback<P> parameterCallback) {
        MAP_PARAM_CALLBACKS.put(clazz, parameterCallback);
    }

    private static native Document createWindow() /*-{
        var win = $wnd.open("", "logwindow", "dependent=yes,width=800,height=800,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no");
        console.log('new window was created' + win.location);
        var doc = win.document;
        doc.open();
        doc.write("<html><body onunload=\"\"></body></html>");
        doc.close();
        win.onunload = function (evt) {
            @de.marketmaker.iview.mmgwt.mmweb.client.util.LogWindow::onUnload()();
        };
        doc.onkeydown = function (e) {
            var event = window.event || e;
            if (event.keyCode == 116) {
                @de.marketmaker.iview.mmgwt.mmweb.client.util.LogWindow::clear()();
                event.preventDefault();
            }
        }
        return doc;
    }-*/;

    public static void onUnload() {
        window = null;
    }

    public static void show() {
        if (window == null) {
            window = new ActiveWindow(createWindow());
            window.loadStylesheet("logwindow.css");
            window.setTitle("mm[web] log");
        }
        else {
            clear();
        }
    }

    public static void clear() {
        window.clear();
    }

    public static void prepareRequest(MmwebRequest mmwebRequest) {
        mmwebRequest.setWithXmlRequest(hasComplexParameter(mmwebRequest) && (window != null || SessionData.INSTANCE.isUserPropertyTrue("fbRequest")));
        mmwebRequest.setWithXmlResponse((window != null && withResponse) || SessionData.INSTANCE.isUserPropertyTrue("fbResponse"));
    }

    private static boolean hasComplexParameter(MmwebRequest mmwebRequest) {
        final List<RequestedBlockType> listBlocks = mmwebRequest.getDmxmlRequest().getBlock();
        for (RequestedBlockType block : listBlocks) {
            final List<Parameter> listParameters = block.getParameter();
            for (Parameter parameter : listParameters) {
                if (!parameter.getClass().getName().matches(".*\\.Parameter$")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void add(String text, boolean asHtml) {
        if (window == null) {
            return;
        }
        window.add(text, asHtml, STYLE_ENTRY);
    }

    public static void add(MmwebRequest mmwebRequest) {
        if (window == null) {
            return;
        }
        if (!mmwebRequest.isWithXmlRequest()) {
            add(mmwebRequest.getDmxmlRequest());
        }
    }

    public static void add(RequestType request) {
        if (window == null) {
            return;
        }
        if (modeXml) {
            addXml(request);
        }
        else {
            addTable(request);
        }
    }

    private static void addTable(RequestType request) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<table style=\"width: 100%;\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.appendHtmlConstant("<colgroup><col width=\"100\"><col width=\"100\"></colgroup>");
        sb.appendHtmlConstant("<tbody>");
        sb.appendHtmlConstant("<tr><td class=\"request\" colSpan=\"4\">Request</td></tr>");
        for (RequestedBlockType block : request.getBlock()) {
            sb.appendHtmlConstant("<tr><td></td><td class=\"block\" colSpan=\"3\"><b>").appendEscaped(block.getKey()).appendHtmlConstant("</b> (id=").appendEscaped(block.getId()).appendHtmlConstant(")</td></tr>");
            for (Parameter param : block.getParameter()) {
                sb.appendHtmlConstant("<tr><td>&nbsp;</td><td>&nbsp;</td><td class=\"key\">").appendEscaped(param.getKey()).appendHtmlConstant("</td><td class=\"value\">");
                if (param.getClass() == Parameter.class) {
                    sb.appendEscaped(param.getValue());
                }
                else {
                    // TODO: add specific renderer for complex parameter types like MMTalkRequest

                    sb.appendEscaped("_complex parameter: ").appendEscaped(param.getClass().getName()).appendHtmlConstant("_");
                }
                sb.appendHtmlConstant("</td></tr>");
            }
        }
        sb.appendHtmlConstant("</tbody></table>");
        window.add(sb.toSafeHtml().asString(), true, STYLE_ENTRY);
    }

    private static void addXml(RequestType request) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<pre>");
        sb.appendEscaped(toXml(request));
        sb.appendHtmlConstant("</pre>");
        window.add(sb.toSafeHtml().asString(), true, STYLE_ENTRY);
    }

    public static String toXml(RequestType request) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<request>\n");
        sb.append("  <authentication>").append(SessionData.INSTANCE.getUser().getVwdId()).append("</authentication>\n");
        sb.append("  <authenticationType>vwddz</authenticationType>\n");
        sb.append("  <locale>").append(I18n.I.localeForRequests()).append("</locale>\n");
        for (RequestedBlockType block : request.getBlock()) {
            sb.append("  <block key=\"").append(block.getKey()).append("\" id=\"").append(block.getId()).append("\">\n");
            for (Parameter param : block.getParameter()) {
                final ParameterCallback callback = MAP_PARAM_CALLBACKS.get(param.getClass());
                if (callback != null) {
                    //noinspection unchecked
                    callback.addXml(sb, param);
                }
                else {
                    sb.append("    <parameter key=\"").append(param.getKey()).append("\">\n");
                    sb.append("      __ unhandled parameter type: ").append(param.getClass().getName()).append(" __\n");
                    sb.append("    </parameter>\n");
                }
            }
            sb.append("  </block>\n");
        }
        sb.append("</request>");
        return sb.toString();
    }


    public static void add(Throwable caught) {
        add(caught.getMessage(), false);
    }

    public static void add(ResponseType response) {
        if (window == null) {
            return;
        }
        if (response == null) {
            add("<div class=\"error\">Internal Server Error</div>", true);
            return;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width: 100%;\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.append("<colgroup><col width=\"100\"><col width=\"100\"></colgroup>");
        sb.append("<tbody>");
        sb.append("<tr><td class=\"response\" colSpan=\"3\">Response</td></tr>");
        for (BlockOrError boe : response.getData().getBlockOrError()) {
            if (boe instanceof BlockType) {
                append(sb, (BlockType) boe);
            }
            else {
                append(sb, (ErrorType) boe);
            }
        }
        sb.append("</tbody></table>");
        window.add(sb.toString(), true, STYLE_ENTRY);
    }

    private static void append(StringBuilder sb, BlockType block) {
        sb.append("<tr><td></td><td class=\"block\" colSpan=\"2\"><b>");
        sb.append(block.getKey());
        sb.append("</b> (id=");
        sb.append(block.getCorrelationId());
        sb.append("; ttl=");
        sb.append(block.getTtl());
        sb.append(")</td></tr>");
        sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td><td class=\"content\">");
        sb.append(block.toString()).append("</td></tr>");
    }


    private static void append(StringBuilder sb, ErrorType error) {
        sb.append("<tr><td></td><td class=\"error\" colSpan=\"2\"><b>");
        sb.append(error.getKey());
        sb.append("</b> (id=");
        sb.append(error.getCorrelationId());
        sb.append(")</td></tr>");
        sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td><td class=\"content\">");
        sb.append(error.getCode()).append("</td></tr>");
        sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td><td class=\"content\">");
        sb.append(error.getDescription()).append("</td></tr>");
    }


    public static void showUserData() {
        show();
        addUserData();
    }

    public static void addUserData() {
        if (window == null) {
            return;
        }
        final User user = SessionData.INSTANCE.getUser();

        final StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width: 100%;\" cellpadding=\"0\" cellspacing=\"0\">");
        sb.append("<colgroup><col width=\"100\"><col width=\"100\"></colgroup>");
        sb.append("<tbody>");

        sb.append("<tr><td colSpan=\"3\">").append(user.getLogin()).append("</td></tr>");
        sb.append("<tr><td></td><td>Name</td><td>").append(user.getFirstName()).append(" ").append(user.getLastName()).append("</td></tr>");
        sb.append("<tr><td></td><td>vwdId</td><td>").append(user.getVwdId()).append("</td></tr>");
        sb.append("<tr><td></td><td>client</td><td>").append(user.getClient()).append("</td></tr>");
        sb.append("<tr><td colSpan=\"3\">AppProfile</td></tr>");
        final AppProfile appProfile = user.getAppProfile();
        sb.append("<tr><td></td><td>ProduktId</td><td>").append(appProfile.getProduktId()).append("</td></tr>");
        append(sb, "functions", sort(appProfile.getFunctions()));
        append(sb, "products", sort(appProfile.getProducts()));
        append(sb, "news", sort(appProfile.getNews()));
        append(sb, "pages", sort(appProfile.getPages()));
        sb.append("<tr><td colSpan=\"3\">AppConfig</td></tr>");
        final AppConfig appConfig = user.getAppConfig();
        sb.append("<tr><td></td><td colSpan=\"2\">");
        appConfig.appendHtmlTable(sb);
        sb.append("</td></tr>");

        sb.append("</tbody></table>");
        window.add(sb.toString(), true, STYLE_ENTRY);
    }

    private static void append(StringBuilder sb, String name, Collection<String> selectors) {
        sb.append("<tr><td></td><td colSpan=\"2\">").append(name).append("</td></tr>");
        for (String s : selectors) {
            sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td><td>").append(s).append("&nbsp;&nbsp;").append(toEntitlement(s)).append("</td></tr>");
        }
    }

    public static ArrayList<String> sort(Collection<String> selectors) {
        final ArrayList<String> list = new ArrayList<>(selectors);
        Collections.sort(list, new Comparator<String>() {
            public int compare(String s1, String s2) {
                try {
                    return Integer.parseInt(s1) - Integer.parseInt(s2);
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2);
                }
            }
        });
        return list;
    }

    public static String toEntitlement(String sv) {
        final int value;
        try {
            value = Integer.parseInt(sv);
        } catch (NumberFormatException e) {
            return "";
        }
        if (value < 1 || value > 1560) {
            return "";
        }
        final int mod = (value % 26);
        final int n = (mod == 0) ? (value / 26) : (value / 26 + 1);
        final String zeroPrefix = n < 10 ? "0" : "";
        final char c = (mod == 0) ? 'Z' : (char) ('A' + mod - 1);
        return zeroPrefix + n + c;
    }

    public static void renderMMTalkRequest(StringBuilder sb, MMTalkRequest r) {
        indent(sb, 1).append("<parameter key=\"request\" xsi:type=\"MMTalkRequest\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        renderMMTalkRequestQuery(sb, r, 2);
        renderMMTalkRequestRootnode(sb, r, 2);
        indent(sb, 1).append("<intraday>").append(r.isIntraday()).append("</intraday>\n");
        indent(sb, 1).append("<language>").append(r.getLanguage()).append("</language>\n");
        indent(sb, 1).append("<name>").append(r.getName()).append("</name>\n");
        indent(sb, 1).append("</parameter>\n");
    }

    public static void renderMMTalkRequestQuery(StringBuilder sb, MMTalkRequest r,
            final int depth) {
        if (r.getQuery().getClass() == QueryMMTalk.class) {
            final QueryMMTalk q = (QueryMMTalk) r.getQuery();

            indent(sb, depth).append("<query xsi:type=\"QueryMMTalk\">\n");
            indent(sb, depth + 1).append("<MMTalkFormula>").append(q.getMMTalkFormula()).append("</MMTalkFormula>\n");
            indent(sb, depth).append("</query>\n");
        }
        else if (r.getQuery().getClass() == QueryStandard.class) {
            final QueryStandard q = (QueryStandard) r.getQuery();

            indent(sb, depth).append("<query xsi:type=\"QueryStandard\">\n");
            indent(sb, depth + 1).append("<inputObjectType>").append(q.getDataItemType()).append("</inputObjectType>\n");
            indent(sb, depth).append("</query>\n");
        }

    }

    public static void renderMMTalkRequestRootnode(StringBuilder sb, MMTalkRequest r,
            final int depth) {
        indent(sb, depth).append("<rootnode>\n");
        renderMMTalkNodeTable(sb, r.getRootnode(), depth + 1);
        indent(sb, depth).append("</rootnode>\n");
    }

    public static void renderMMTalkNodeTable(StringBuilder sb, TableTreeTable tableNode,
            final int depth) {
        if (tableNode.getFormula() != null) {
            indent(sb, depth).append("<Formula>").append(tableNode.getFormula()).append("</Formula>\n");
        }
        else {
            indent(sb, depth).append("<Formula xsi:nil=\"true\"/>\n");
        }

        if (tableNode.getFilterFormula() != null) {
            indent(sb, depth).append("<FilterFormula>").append(tableNode.getFilterFormula()).append("</FilterFormula>\n");
        }
        else {
            indent(sb, depth).append("<FilterFormula xsi:nil=\"true\"/>\n");
        }

        for (TableTreeElement node : tableNode.getColumns()) {
            if (node.getClass() == TableTreeFormula.class) {
                indent(sb, depth).append("<Columns xsi:type=\"TableTreeFormula\">\n");
                renderMMTalkNodeFormula(sb, (TableTreeFormula) node, depth + 1);
                indent(sb, depth).append("</Columns>\n");
            }
            else if (node.getClass() == TableTreeTable.class) {
                indent(sb, depth).append("<Columns xsi:type=\"TableTreeTable\">\n");
                renderMMTalkNodeTable(sb, (TableTreeTable) node, depth + 1);
                indent(sb, depth).append("</Columns>\n");
            }
            else {
                /* should never happen */
                indent(sb, depth).append("UNEXPECTED COMPLEX TYPE").append(node.getClass().getName());
            }
        }
    }

    public static void renderMMTalkNodeFormula(StringBuilder sb, TableTreeFormula formulaNode,
            final int depth) {
        indent(sb, depth).append("<Formula>");
        sb.append(formulaNode.getFormula().trim());
        sb.append("</Formula>\n");
    }

    public static StringBuilder indent(StringBuilder sb, final int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb;
    }

    public static void flipMode() {
        modeXml = !modeXml;
        Firebug.log("log window mode: xml = " + modeXml);
    }

    public static void setWithResponse(boolean b) {
        withResponse = b;
    }

    public static boolean isWithResponse() {
        return withResponse;
    }

    public static void addResponse(String text) {
        if (!withResponse) {
            return;
        }
        addPre(text, STYLE_ENTRY + " response");
    }

    public static void addRequest(String text) {
        addPre(text, STYLE_ENTRY);
    }

    public static void addPre(String text) {
        addPre(text, STYLE_ENTRY);
    }

    private static void addPre(String text, String style) {
        if (window == null || text == null) {
            return;
        }
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<pre>").appendEscaped(text).appendHtmlConstant("</pre>");
        window.add(sb.toSafeHtml().asString(), true, style);
    }
}
