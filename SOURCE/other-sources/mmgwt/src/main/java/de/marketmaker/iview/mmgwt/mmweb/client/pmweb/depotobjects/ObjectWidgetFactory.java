/*
 * ObjectWidgetFactory.java
 *
 * Created on 11.01.13 09:51
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.query.client.Function;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.LayoutNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWorkspaceCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWorkspaceHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.AbstractOwner;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Advisor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.DepotObject;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Investor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Person;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Prospect;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.ActivityInfoItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.AlertsResponse;
import de.marketmaker.iview.pmxml.GetWorkspaceResponse;
import de.marketmaker.iview.pmxml.MMAlert;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.WorksheetDefaultMode;
import de.marketmaker.iview.pmxml.WorkspaceDefaultSheetDesc;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.query.client.GQuery.$;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class ObjectWidgetFactory {
    public static final String STYLE_TYPE = "as-objectWidget-type"; // $NON-NLS$
    public static final String STYLE_HEAD = "as-objectWidget-head"; // $NON-NLS$]
    public static final String STYLE_HEAD_TOOLTIP = STYLE_HEAD + "-tooltip"; // $NON-NLS$
    public static final String STYLE_ALERT = "as-objectWidget-alert"; // $NON-NLS$

    private static final String COLON = ": ";  //$NON-NLS$
    private static final String BR = "<br/>"; //$NON-NLS$
    private static final String O_TEXT = "<div class=\"as-objectWidget-text\">"; //$NON-NLS$
    private static final String O_NUMBER = "<div class=\"as-objectWidget-number\">"; //$NON-NLS$
    private static final String O_CONTACT = "<div class=\"as-objectWidget-contact\">"; //$NON-NLS$
    private static final String O_TYPE = "<div class=\"" + STYLE_TYPE + "\">"; //$NON-NLS$
    private static final String C = "</div>"; //$NON-NLS$

    private static final Map<AlertNodeKey, LayoutNode> ALERT_NODES = new HashMap<>(); //cache for alert nodes

    private static SafeHtmlBuilder appendObjectIcon(SafeHtmlBuilder sb, String iconClass) {
        sb.appendHtmlConstant("<div class=\"as-objectIcon\">"); // $NON-NLS$
        sb.append(IconImage.get(iconClass + "-32").getSafeHtml()); // $NON-NLS$
        sb.appendHtmlConstant("</div>"); // $NON-NLS$
        return sb;
    }

    private static void appendNameOrDash(SafeHtmlBuilder shb, Address address) {
        if (address == null) {
            shb.appendEscaped("--");
            return;
        }
        boolean fieldFilled = append(shb, false, null, I18n.I.salutation(), address.getSalutation());
        fieldFilled = append(shb, fieldFilled, " ", I18n.I.lastName(), address.getLastname());
        append(shb, fieldFilled, ", ", I18n.I.firstName(), address.getFirstname());
    }

    private static void appendNameOrDash(SafeHtmlBuilder shb, Person person) {
        if (person == null) {
            shb.appendEscaped("--");
            return;
        }
        boolean fieldFilled = append(shb, false, null, I18n.I.salutation(), PmRenderers.GESCHLECHT_SALUTATION_RENDERER.render(person.getGender()));
        fieldFilled = append(shb, fieldFilled, " ", I18n.I.titleName(), person.getLastNamePrefix());
        fieldFilled = append(shb, fieldFilled, " ", I18n.I.lastName(), person.getLastName());
        append(shb, fieldFilled, ", ", I18n.I.firstName(), person.getFirstName());
    }

    private static boolean append(SafeHtmlBuilder shb, boolean fieldFilled, String divider, String qtipLabel, String value) {
        if (!StringUtil.hasText(value)) {
            return fieldFilled;
        }
        if (fieldFilled) {
            shb.appendEscaped(divider);
        }
        shb.appendHtmlConstant("<span " + Tooltip.ATT_QTIP_LABEL + "=\"" + qtipLabel + "\">");
        shb.appendEscaped(value);
        shb.appendHtmlConstant("</span>");
        return true;
    }

    private static SafeHtmlBuilder appendHead(SafeHtmlBuilder sb, ContextItem item) {
        final String renderedObjectName = item == null
                ? Renderer.STRING_DOUBLE_DASH.render(null)
                : Renderer.STRING_DOUBLE_DASH.render(item.getName());

        return sb.appendHtmlConstant("<div class=\"" + STYLE_HEAD + "\" ") // $NON-NLS$
                .appendHtmlConstant(Tooltip.ATT_COMPLETION).appendHtmlConstant("=\"auto\"") // $NON-NLS$
                .appendHtmlConstant(Tooltip.ATT_STYLE).appendHtmlConstant("=\"" + STYLE_HEAD_TOOLTIP + "\">") // $NON-NLS$
                .appendEscaped(renderedObjectName)
                .appendHtmlConstant("</div>");
    }

    private static void appendDateOfBirth(SafeHtmlBuilder sb, Address address) {
        if (address != null && StringUtil.hasText(address.getDateOfBirth())) {
            appendDivWithQtipLabel(sb, "as-objectWidget-dateOfBirth", I18n.I.birthday(), "*" + PmRenderers.DATE_STRING.render(address.getDateOfBirth())); // $NON-NLS$
        }
    }

    private static void appendAddress(SafeHtmlBuilder sb, Address address) {
        if (address == null) return;
        appendAddress(sb, address.getAddress(), address.getPostCode(), address.getCity(), address.getCountry());
    }

    private static void appendAddress(SafeHtmlBuilder sb, String address, String postCode, String city, String country) {
        sb.appendHtmlConstant("<div class=\"as-objectWidget-address\""); // $NON-NLS$
        appendQtipLabel(sb, I18n.I.address());
        sb.appendHtmlConstant(">"); // $NON-NLS$
        if (StringUtil.hasText(address)) {
            sb.appendEscaped(address);
            sb.appendHtmlConstant("<br/>"); // $NON-NLS$
        }
        if (StringUtil.hasText(postCode)) {
            sb.appendEscaped(postCode);
            sb.appendEscaped(" ");
        }
        if (StringUtil.hasText(city)) {
            sb.appendEscaped(city);
        }
        if (StringUtil.hasText(country)) {
            sb.appendHtmlConstant("<br/>").appendEscaped(country); // $NON-NLS$
        }
        sb.appendHtmlConstant("</div>"); // $NON-NLS$
    }

    private static void appendPhoneNumbers(SafeHtmlBuilder sb, Address address) {
        if (address == null) {
            return;
        }

        if (StringUtil.hasText(address.getPhonePrivate())) {
            appendSpanWithQtipLabel(sb, I18n.I.telephoneNumberPrivateAbbr(), address.getPhonePrivate());
        }
        else if (StringUtil.hasText(address.getPhoneBusiness())) {
            appendSpanWithQtipLabel(sb, I18n.I.telephoneNumberBusinessAbbr(), address.getPhoneBusiness());
        }
        else if (StringUtil.hasText(address.getPhoneMobile())) {
            appendSpanWithQtipLabel(sb, I18n.I.telephoneNumberMobileAbbr(), address.getPhoneMobile());
        }
    }

    private static SafeHtmlBuilder appendEmailAddress(SafeHtmlBuilder sb, Address address) {
        if (address == null) return sb;
        final String email = address.getEmail();
        if (!StringUtil.hasText(email)) return sb;

        sb.appendHtmlConstant("<br/><a href=\"mailto:").appendEscaped(email).appendHtmlConstant("\""); // $NON-NLS$
        appendQtipLabel(sb, I18n.I.email());
        sb.appendHtmlConstant(">").appendEscaped(email).appendHtmlConstant("</a>"); //$NON-NLS$
        return sb;
    }

    private static SafeHtmlBuilder appendBank(SafeHtmlBuilder sb, String bankName) {
        return sb.appendHtmlConstant("<div class=\"as-objectWidget-text\">") // $NON-NLS$
                .appendEscaped(I18n.I.bank())
                .appendEscaped(": ")
                .appendEscaped(Renderer.STRING_DOUBLE_DASH.render(bankName))
                .appendHtmlConstant("</div>"); // $NON-NLS$
    }

    private static SafeHtmlBuilder appendCreationDateWithAlertAndDataStatus(SafeHtmlBuilder sb, DepotObject object) {
        sb.appendHtmlConstant("<div class=\"as-objectWidget-status\">"); // $NON-NLS$
        sb.appendHtmlConstant("<div class=\"as-objectWidget-bottomBorder\"></div>"); // $NON-NLS$

        final String creationDateText = PmRenderers.DATE_TIME_STRING.render(object.getCreationDate());
        sb.appendHtmlConstant("<div class=\"as-objectWidget-creationDate\""); // $NON-NLS$
        if (!creationDateText.isEmpty()) {
            appendQtipLabel(sb, I18n.I.createdOn());
        }
        sb.appendHtmlConstant(">");
        sb.appendEscaped(creationDateText)
                .appendHtmlConstant("</div>"); // $NON-NLS$

        appendAlert(sb, object);
        appendDataStatus(sb, object);

        sb.appendHtmlConstant("</div>"); // $NON-NLS$

        return sb;
    }

    private static void appendSpanWithQtipLabel(SafeHtmlBuilder sb, String qtipLabelText, String contentText) {
        sb.appendHtmlConstant("<span ") // $NON-NLS$
                .appendHtmlConstant(Tooltip.ATT_QTIP_LABEL).appendHtmlConstant("=\"").appendEscaped(qtipLabelText).appendHtmlConstant("\">")
                .appendEscaped(contentText)
                .appendHtmlConstant("</span>");
    }

    private static void appendDivWithQtipLabel(SafeHtmlBuilder sb, String styleName, String qtipLabelText, String contentText) {
        sb.appendHtmlConstant("<div class=\"").appendHtmlConstant(styleName).appendHtmlConstant("\" ") // $NON-NLS$
                .appendHtmlConstant(Tooltip.ATT_QTIP_LABEL).appendHtmlConstant("=\"").appendEscaped(qtipLabelText).appendHtmlConstant("\">")
                .appendEscaped(contentText)
                .appendHtmlConstant("</div>");
    }

    private static void appendDivWithQtipLabel(SafeHtmlBuilder sb, String styleName, SafeHtml qtipLabelText, SafeHtml contentHtml) {
        sb.appendHtmlConstant("<div class=\"").appendHtmlConstant(styleName).appendHtmlConstant("\"");
        if (qtipLabelText != null) {
            appendQtipLabel(sb, qtipLabelText);
        }
        sb.appendHtmlConstant(">")
                .append(contentHtml)
                .appendHtmlConstant("</div>");
    }

    private static void appendDivWithQtipLabel(SafeHtmlBuilder sb, String styleName, String qtipLabelText, SafeHtml contentHtml) {
        appendDivWithQtipLabel(sb, styleName, new SafeHtmlBuilder().appendEscaped(qtipLabelText).toSafeHtml(), contentHtml);
    }

    private static void appendQtipLabel(SafeHtmlBuilder sb, String qtipLabelText) {
        sb.appendHtmlConstant(" ").appendHtmlConstant(Tooltip.ATT_QTIP_LABEL).appendHtmlConstant("=\"").appendEscaped(qtipLabelText).appendHtmlConstant("\"");
    }

    private static void appendQtipLabel(SafeHtmlBuilder sb, SafeHtml qtipLabelText) {
        sb.appendHtmlConstant(" ").appendHtmlConstant(Tooltip.ATT_QTIP_LABEL).appendHtmlConstant("=\"").append(qtipLabelText).appendHtmlConstant("\"");
    }

    @NonNLS
    private static void appendAlert(SafeHtmlBuilder sb, DepotObject object) {
        final AlertsResponse res = object.getAlertResponse();
        if (res == null) {
            return;
        }
        final List<MMAlert> alerts = res.getAlerts();
        if (alerts.isEmpty()) {
            return;
        }
        if (alerts.size() == 1) {
            appendDivWithQtipLabel(sb, STYLE_ALERT, createAlertMessage(alerts.get(0), object), IconImage.get("pm-alert").getSafeHtml());
        }
        else {
            appendDivWithQtipLabel(sb, STYLE_ALERT, createAlertsMessage(alerts, object), IconImage.get("pm-alert").getSafeHtml());
        }
    }

    private static SafeHtml createAlertsMessage(List<MMAlert> alerts, DepotObject shownObject) {

        //TODO: USE STREAM-API AS SOON AS POSSIBLE!!!

        final SafeHtmlBuilder shb = new SafeHtmlBuilder();
        //group by id
        final HashMap<String, List<MMAlert>> idMap = new HashMap<>();
        for (MMAlert alert : alerts) {
            final String idKey = alert.getShellMM().getId();
            if (!idMap.containsKey(idKey)) {
                idMap.put(idKey, new ArrayList<>());
            }
            idMap.get(idKey).add(alert);
        }
        //render alert of shownObject first
        if (idMap.containsKey(shownObject.getId())) {
            final List<MMAlert> mmAlerts = idMap.get(shownObject.getId());
            boolean hasText = false;
            for (MMAlert mmAlert : mmAlerts) {
                if (hasText) {
                    shb.appendHtmlConstant("<br/>");
                }
                shb.append(createAlertMessage(mmAlert, shownObject));
                hasText = true;
            }
        }
        idMap.remove(shownObject.getId());
        if (idMap.isEmpty()) {
            return shb.toSafeHtml();
        }

        //more alerts? order depotobjects by type and sort alerts by id
        final HashMap<String, List<MMAlert>> typeMap = new HashMap<>();
        for (List<MMAlert> mmAlerts : idMap.values()) {
            if (mmAlerts.isEmpty()) {
                continue;
            }
            final String typeKey = mmAlerts.get(0).getShellMM().getTyp().value();
            if (!typeMap.containsKey(typeKey)) {
                typeMap.put(typeKey, new ArrayList<>());
            }
            final ArrayList<MMAlert> sortedAlerts = new ArrayList<>(mmAlerts);
            Collections.sort(sortedAlerts, new Comparator<MMAlert>() {
                @Override
                public int compare(MMAlert o1, MMAlert o2) {
                    return o1.getShellMM().getId().compareTo(o2.getShellMM().getId());
                }
            });
            typeMap.get(typeKey).addAll(mmAlerts);
        }
        shb.appendHtmlConstant("<hr/>");
        shb.appendHtmlConstant("<ul class='as-tooltip-alerts-list'>");
        for (String type : typeMap.keySet()) {
            shb.appendHtmlConstant("<li>")
                    .appendEscaped(PmRenderers.SHELL_MM_TYPE_PLURAL.render(ShellMMType.fromValue(type)));
            final List<MMAlert> mmAlerts = typeMap.get(type);
            for (MMAlert mmAlert : mmAlerts) {
                shb.appendHtmlConstant("<p>")
                        .appendHtmlConstant("<i>")
                        .appendEscaped(mmAlert.getShellMM().getBezeichnung())
                        .appendHtmlConstant("</i>")
                        .appendEscaped(": ")
                        .appendEscaped(mmAlert.getMessage())
                        .appendHtmlConstant("</p>");
            }
            shb.appendHtmlConstant("</li>");
        }
        shb.appendHtmlConstant("</ul>");
        return shb.toSafeHtml();
    }


    private static SafeHtml createAlertMessage(MMAlert mmAlert, DepotObject shownObject) {
        final SafeHtmlBuilder shb = new SafeHtmlBuilder();
        if (!mmAlert.getShellMM().getId().equals(shownObject.getId())) {
            shb.appendEscaped(PmRenderers.SHELL_MM_TYPE.render(mmAlert.getShellMM().getTyp()))
                    .appendEscaped(" ").appendEscaped(mmAlert.getShellMM().getBezeichnung())
                    .appendEscapedLines(" : ");
        }
        shb.appendEscaped(mmAlert.getMessage());
        return shb.toSafeHtml();
    }


    private static SafeHtmlBuilder appendDataStatus(SafeHtmlBuilder sb, DepotObject object) {
        if (object.getDataStatusTotal() == null) {
            return sb;
        }

        final String date = PmRenderers.DATE_STRING.render(object.getDataStatusTotalDate());
        final String tooltip;

        final String imageName;
        switch (object.getDataStatusTotal()) {
            case VS_VALID:
                imageName = "pm-datastatus-correct"; //$NON-NLS$
                tooltip = StringUtil.hasText(date) ? I18n.I.dataStatusValidFrom(date) : I18n.I.dataStatusValid();
                break;
            case VS_INVALID:
                imageName = "pm-datastatus-incorrect"; //$NON-NLS$
                tooltip = StringUtil.hasText(date) ? I18n.I.dataStatusInvalidFrom(date) : I18n.I.dataStatusInvalid();
                break;
            case VS_NA:
            default:
                return sb;
        }

        appendDivWithQtipLabel(sb, "as-objectWidget-dataStatus", tooltip, IconImage.get(imageName).getSafeHtml()); // $NON-NLS$
        return sb;
    }

    private static void appendCapital(SafeHtmlBuilder sb, String capital, String currencyOfCapital) {
        sb.appendHtmlConstant("<div class=\"as-objectWidget-capital\">") // $NON-NLS$
                .appendEscaped(Renderer.PRICE23.render(capital));
        if (currencyOfCapital != null) {
            sb.appendEscaped(" ").appendEscaped(currencyOfCapital);
        }
        sb.appendHtmlConstant("</div>"); // $NON-NLS$
    }

    public static Widget createInvestorWidget(Investor i) {
        return createAbstractOwnerWidget(i, "pm-investor", i.getType(), I18n.I.investorNumber());  // $NON-NLS$
    }

    public static Widget createProspectWidget(Prospect p) {
        return createAbstractOwnerWidget(p, "pm-investor-prospect", I18n.I.prospect(), I18n.I.prospectNumberAbbr());  // $NON-NLS$
    }

    private static <T extends AbstractOwner> Widget createAbstractOwnerWidget(final T t, String iconClass, String type, String ownerNumberLabel) {
        final Address address = t.getAddress();

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendObjectIcon(sb, iconClass)
                .appendHtmlConstant(O_TYPE)
                .appendEscaped(type)
                .appendHtmlConstant(C);

        appendHead(sb, t);

        if (StringUtil.hasText(t.getNumber())) {
            appendDivWithQtipLabel(sb, "as-objectWidget-number", ownerNumberLabel, t.getNumber()); // $NON-NLS$
        }

        sb.appendHtmlConstant(O_TEXT);
        appendNameOrDash(sb, address);
        sb.appendHtmlConstant(C);

        appendDateOfBirth(sb, address);

        appendAddress(sb, address);

        sb.appendHtmlConstant(O_CONTACT);
        appendPhoneNumbers(sb, address);
        appendEmailAddress(sb, address);
        sb.appendHtmlConstant(C);

        appendCreationDateWithAlertAndDataStatus(sb, t);

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-objectWidget");

        addAlertClickHandler(t, html);
        return html;
    }

    private static <T extends DepotObject> void addAlertClickHandler(final T t, final HTML html) {
        final LayoutNode layoutNode = ALERT_NODES.get(AlertNodeKey.getFlyWeight(t.getShellMMType()));
        if(layoutNode != null) {
            doAddAlertClickHandler(t, html, layoutNode);
        }
        else {
            PmWorkspaceHandler.getInstance().getPmWorkspace(PrivacyMode.isActive(), t.getShellMMType(), new PmWorkspaceCallback() {
                @Override
                public void onWorkspaceAvailable(GetWorkspaceResponse response) {
                    final WorkspaceSheetDesc sheet = findAlertSheet(response, findAlertNode(response));
                    if (sheet == null) {
                        Firebug.error("no alert sheetdesc found in workspace for " + t.getShellMMType());
                        return;
                    }

                    final String nodeId = sheet.getNodeId();
                    if(StringUtil.hasText(nodeId)) {
                        final LayoutNode value = LayoutNode.create(nodeId, sheet.getLayoutGuid());
                        ALERT_NODES.put(new AlertNodeKey(t.getShellMMType()), value);
                        doAddAlertClickHandler(t, html, value);
                    }
                }
            });
        }
    }

    private static <T extends DepotObject> void doAddAlertClickHandler(final T t, final HTML html, final LayoutNode layoutNode) {
        html.addAttachHandler(new AttachEvent.Handler() {
            public void onAttachOrDetach(AttachEvent event) {
                $("." + STYLE_ALERT, html).on("click", new Function() { //traversal is cheap because of limitation to html widget
                    public void f() {
                        onAlertClick(t, layoutNode);
                    }
                }).addClass("mm-link");
            }
        });
    }

    private static void onAlertClick(final DepotObject object, final LayoutNode layoutNode) {
        HistoryToken.builder(ShellMMTypeUtil.getControllerId(object.getShellMMType()))
                .with(AbstractDepotObjectPortraitController.OBJECTID_KEY, object.getId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, layoutNode.toString())
                .build().fire();
    }

    private static WorkspaceSheetDesc findAlertSheet(GetWorkspaceResponse response, String alertNode) {
        if (alertNode == null) {
            return null;
        }
        final List<WorkspaceSheetDesc> sheets = response.getSheets();
        for (WorkspaceSheetDesc sheet : sheets) {
            final WorkspaceSheetDesc alertSheet = findAlertSheet(sheet, alertNode);
            if (alertSheet != null) {
                return alertSheet;
            }
        }
        return null;
    }

    private static WorkspaceSheetDesc findAlertSheet(WorkspaceSheetDesc sheet, String alertNode) {
        for (WorkspaceSheetDesc child : sheet.getSheets()) {
            if (child.getNodeId().equals(alertNode)) {
                return child;
            }
        }
        return null;
    }

    private static String findAlertNode(GetWorkspaceResponse response) {
        final List<WorkspaceDefaultSheetDesc> defaultSheets = response.getDefaultSheets();
        for (WorkspaceDefaultSheetDesc defaultSheet : defaultSheets) {
            if (defaultSheet.getDefaultMode() == WorksheetDefaultMode.WDM_ALERT) {
                return defaultSheet.getNodeId();
            }
        }
        Firebug.error("no default sheet for WDM_ALERT found");
        return null;
    }

    public static Widget createPortfolioWidget(Portfolio portfolio) {
        final Advisor advisor = portfolio.getPortfolioVersion() != null
                ? portfolio.getPortfolioVersion().getAdvisor()
                : null;
        final Address advisorAddress = advisor != null ? advisor.getAddress() : null;

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendObjectIcon(sb, "pm-investor-portfolio") // $NON-NLS$
                .appendHtmlConstant(O_TYPE)
                .appendEscaped(portfolio.getType())
                .appendHtmlConstant(C);

        appendHead(sb, portfolio);

        if (StringUtil.hasText(portfolio.getPortfolioNumber())) {
            appendDivWithQtipLabel(sb, "as-objectWidget-number", I18n.I.portfolioNumber(), portfolio.getPortfolioNumber()); // $NON-NLS$
        }

        sb.appendHtmlConstant(O_TEXT)
                .appendEscaped(I18n.I.pmInvestor())
                .appendEscaped(COLON)
                .appendEscaped(Renderer.STRING_DOUBLE_DASH.render(portfolio.getInvestorName()))
                .appendHtmlConstant(C)

                .appendHtmlConstant(O_TEXT)
                .appendEscaped(I18n.I.advisor())
                .appendEscaped(COLON);
        appendNameOrDash(sb, advisorAddress);
        sb.appendHtmlConstant(C);

        appendCreationDateWithAlertAndDataStatus(sb, portfolio);

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-objectWidget");

        addAlertClickHandler(portfolio, html);

        return html;
    }

    public static Widget createDepotWidget(Depot depot) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendObjectIcon(sb, "pm-investor-depot") // $NON-NLS$
                .appendHtmlConstant(O_TYPE)
                .appendEscaped(depot.getType())
                .appendHtmlConstant(C);

        appendHead(sb, depot);

        if (StringUtil.hasText(depot.getDepotNumber())) {
            appendDivWithQtipLabel(sb, "as-objectWidget-number", I18n.I.depotNumber(), depot.getDepotNumber()); // $NON-NLS$
        }

        sb.appendHtmlConstant(O_TEXT)
                .appendEscaped(I18n.I.pmInvestor())
                .appendEscaped(COLON)
                .appendEscaped(Renderer.STRING_DOUBLE_DASH.render(depot.getInvestorName()))
                .appendHtmlConstant(C);

        appendBank(sb, depot.getBankName());

        appendCreationDateWithAlertAndDataStatus(sb, depot);

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-objectWidget"); //$NON-NLS$

        addAlertClickHandler(depot, html);

        return html;
    }

    public static Widget createAccountWidget(Account account) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendObjectIcon(sb, "pm-investor-account") // $NON-NLS$
                .appendHtmlConstant(O_TYPE)
                .appendEscaped(account.getType())
                .appendHtmlConstant(C);

        appendHead(sb, account);

        if (StringUtil.hasText(account.getAccountNumber())) {
            appendDivWithQtipLabel(sb, "as-objectWidget-number", I18n.I.accountNumber(), account.getAccountNumber()); // $NON-NLS$
        }

        sb.appendHtmlConstant(O_TEXT)
                .appendEscaped(I18n.I.pmInvestor())
                .appendEscaped(COLON)
                .appendEscaped(Renderer.STRING_DOUBLE_DASH.render(account.getInvestorName()))
                .appendHtmlConstant(C);

        appendCapital(sb, account.getBalance(), account.getCurrency());

        appendBank(sb, account.getBank());

        sb.appendHtmlConstant(O_TEXT)
                .appendEscaped("IBAN") //$NON-NLS$
                .appendHtmlConstant(COLON)
                .appendEscaped(Renderer.STRING_DOUBLE_DASH.render(account.getIban()))
                .appendHtmlConstant(C);

        appendCreationDateWithAlertAndDataStatus(sb, account);

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-objectWidget"); //$NON-NLS$

        addAlertClickHandler(account, html);

        return html;
    }

    public static Widget createActivityInfoWidget(ActivityInstanceInfo aii) {
        assert aii.getMainInput() instanceof ShellMMInfo;

        final ShellMMInfo mainInput = (ShellMMInfo)aii.getMainInput();

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendObjectIcon(sb, "pm-activity") // $NON-NLS$
                .appendHtmlConstant(O_TYPE)
                .appendEscaped(I18n.I.activity())
                .appendHtmlConstant(C);
        appendHead(sb, new ActivityInfoItem(aii));
        final String lastAccessedText = PmRenderers.DATE_TIME_HHMM_STRING.render(aii.getLastSaved());
        sb.appendHtmlConstant(O_TEXT)
                .appendEscaped(I18n.I.lastAccess())
                .appendEscaped(":")
                .appendHtmlConstant(BR)
                .appendEscaped(lastAccessedText)
                .appendHtmlConstant(C);
        sb.appendHtmlConstant(O_TEXT)
                .appendEscaped(PmRenderers.SHELL_MM_TYPE.render(mainInput.getTyp()))
                .appendEscaped(":")
                .appendHtmlConstant(BR)
                .appendEscaped(mainInput.getBezeichnung())
                .appendHtmlConstant(C);

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-objectWidget");
        return html;
    }

    public static Widget createPersonWidget(Person person) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendObjectIcon(sb, "pm-investor-person") // $NON-NLS$
                .appendHtmlConstant(O_TYPE)
                .appendEscaped(person.getType())
                .appendHtmlConstant(C);

        appendHead(sb, person);

        if (StringUtil.hasText(person.getSerialNumber())) {
            sb.appendHtmlConstant(O_NUMBER)
                    .appendEscaped(person.getSerialNumber())
                    .appendHtmlConstant(C);
        }

        sb.appendHtmlConstant(O_TEXT);
        appendNameOrDash(sb, person);
        sb.appendHtmlConstant(C);

        String dateOfBirth = person.getDateOfBirth();
        if (StringUtil.hasText(dateOfBirth)) {
            appendDivWithQtipLabel(sb, "as-objectWidget-dateOfBirth", I18n.I.birthday(), "*" + PmRenderers.DATE_STRING.render(dateOfBirth)); // $NON-NLS$
        }

        appendAddress(sb, person.getAddress(), person.getPostCode(), person.getCity(), person.getCountry());

        if (Boolean.TRUE == person.getIncapacityToContract()) {
            sb.appendHtmlConstant(O_TEXT).appendEscaped(I18n.I.incapableOfContracting());
        }

        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("as-objectWidget");
        return html;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static class AlertNodeKey {
        static AlertNodeKey flyWeight = new AlertNodeKey(null);

        static AlertNodeKey getFlyWeight(ShellMMType shellMMType) {
            flyWeight.setShellMMType(shellMMType);
            return flyWeight;
        }

        private ShellMMType shellMMType;
        private boolean privacyMode;

        public AlertNodeKey(ShellMMType shellMMType) {
            this.shellMMType = shellMMType;
            this.privacyMode = PrivacyMode.isActive();
        }

        public ShellMMType getShellMMType() {
            return shellMMType;
        }

        public void setShellMMType(ShellMMType shellMMType) {
            this.shellMMType = shellMMType;
        }

        public boolean isPrivacyMode() {
            return privacyMode;
        }

        public void setPrivacyMode(boolean privacyMode) {
            this.privacyMode = privacyMode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AlertNodeKey)) return false;

            AlertNodeKey that = (AlertNodeKey) o;

            if (privacyMode != that.privacyMode) return false;
            return shellMMType == that.shellMMType;

        }

        @Override
        public int hashCode() {
            int result = shellMMType.hashCode();
            result = 31 * result + (privacyMode ? 1 : 0);
            return result;
        }
    }
}