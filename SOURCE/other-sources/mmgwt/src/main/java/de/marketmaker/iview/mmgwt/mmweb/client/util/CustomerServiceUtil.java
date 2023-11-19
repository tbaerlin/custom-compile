package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData.VwdCustomerServiceContact;

public class CustomerServiceUtil {

    private CustomerServiceUtil() {
        //do not instantiate
    }

    public static void customerServiceAddressRowsBuilder(StringBuilder sb, VwdCustomerServiceContact c) {
        sb.append("<tr><td>").append(I18n.I.telephoneNumberAbbrShort()).append("</td><td>").append(c.getPhone()).append("</td></tr>"); // $NON-NLS$
        if (StringUtil.hasText(c.getFax())) {
            sb.append("<tr><td>").append(I18n.I.telefaxNumberAbbrShort()).append("</td><td>").append(c.getFax()).append("</td></tr>"); // $NON-NLS$
        }
        sb.append("<tr><td colspan=\"2\"><a href=\"mailto:").append(c.getEmail()).append("\">").append(c.getEmail()).append("</td></tr>") // $NON-NLS$
            .append("<tr><td colspan=\"2\">&nbsp;</td></tr>");// $NON-NLS$
    }
}
