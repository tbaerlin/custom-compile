package de.marketmaker.istar.merger.web.xmlmarket;


import de.marketmaker.istar.common.util.XmlUtil;
import org.joda.time.DateTime;

class ErrorDocument extends XmlDocument {

    ErrorDocument(String errorCode, String errorName, String errorMessage, String errorInfo, StackTraceElement[] errorTrace) {
        child(XmlDeclaration.ISO_8859_2);
        child(new XmlNode("errorpage")
                .child(new XmlNode("generated").text(new DateTime().toString("dd.MM.yyyy HH:mm:ss")))
                .child(new XmlNode("errorCode").text(errorCode))
                .child(new XmlNode("errorName").cdata(errorName))
                .child(new XmlNode("errorMessage").cdata(errorMessage))
                .child(new XmlNode("errorInfo").cdata(errorInfo))
                .child(new XmlNode("errorTrace").cdata(format(errorTrace)))
        );
    }


    private String format(StackTraceElement[] errorTrace) {
        if (errorTrace == null || errorTrace.length == 0) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement elem : errorTrace) {
                sb.append(elem);
                sb.append("\n");
            }
            return sb.toString();
        }
    }

}
