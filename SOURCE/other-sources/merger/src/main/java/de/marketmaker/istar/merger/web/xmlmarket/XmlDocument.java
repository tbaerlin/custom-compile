package de.marketmaker.istar.merger.web.xmlmarket;

import java.io.PrintWriter;
import java.util.ArrayList;

class XmlDocument {

    protected final ArrayList<XmlNode> children = new ArrayList<>();


    public String render() {
        StringBuilder stringBuilder = new StringBuilder();
        render(stringBuilder, "");
        return stringBuilder.toString();
    }

    public void render(PrintWriter writer) {
        StringBuilder stringBuilder = new StringBuilder();
        render(stringBuilder, "");
        writer.print(stringBuilder.toString());
    }

    protected void render(StringBuilder sb, String inset) {
        for (XmlNode xmlOutput : children) {
            xmlOutput.render(sb, inset);
        }
    }

    public XmlDocument child(XmlNode xmlNode) {
        children.add(xmlNode);
        return this;
    }


    public static class XmlDeclaration extends XmlNode {
        public static final XmlDeclaration ISO_8859_2 = new XmlDeclaration("<?xml version=\"1.0\" encoding=\"iso-8859-2\"?>");
        public static final XmlDeclaration ISO_8859_1 = new XmlDeclaration("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");

        private final String string;

        public XmlDeclaration(String string) {
            super(""); // no tag name, just content
            this.string = string;
        }

        @Override
        public void render(StringBuilder sb, String inset) {
            sb.append(inset);
            sb.append(string);
            sb.append("\n");
        }
    }

}
