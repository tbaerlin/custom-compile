package de.marketmaker.istar.merger.provider.pages;

import java.util.Properties;

/**
 * @author umaurer
 */
abstract class PdlRenderer {

    protected String linkText;

    protected PdlRenderer(Properties p) {
        this.linkText = p.getProperty("linkText", "ShowPage?pageid=");
    }

    abstract String render(PdlPage pp);

    protected int getPreSpaceCount(PdlObject po) {
        if (po.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_CENTER)) {
            return (po.getDisplayWidth() - po.getContent().length()) / 2;
        }
        if (po.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_RIGHT)) {
            return (po.getDisplayWidth() - po.getContent().length());
        }
        return 0;
    }

    protected int getPostSpaceCount(PdlObject po) {
        if (po.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_CENTER)) {
            return po.getDisplayWidth() - getPreSpaceCount(po) - po.getContent().length();
        }
        if (!po.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_RIGHT)) {
            return (po.getDisplayWidth() - po.getContent().length());
        }
        return 0;
    }

    protected void addSpace(StringBuilder sb, int spaceCount) {
        for (int i = 0; i < spaceCount; i++) {
            sb.append(' ');
        }
    }

    protected void appendEncoded(StringBuilder sb, String content) {
        for (int i = 0, n = content.length(); i < n; i++) {
            final char c = content.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
    }
}
