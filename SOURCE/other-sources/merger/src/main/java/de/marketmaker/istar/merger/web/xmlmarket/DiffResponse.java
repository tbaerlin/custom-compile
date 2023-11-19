package de.marketmaker.istar.merger.web.xmlmarket;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 */
public class DiffResponse implements Response {
    private final DiffMatchPatch tool = new DiffMatchPatch();
    private final String left;
    private final String right;


    public DiffResponse(String left, String right) {
        this.left = left.replaceAll("\\n\\s+", "\n");
        this.right = right.replaceAll("\\n\\s+", "\n");
    }

    @Override
    public void render(PrintWriter writer) {
        final StringBuilder sb = new StringBuilder();

        LinkedList<DiffMatchPatch.Diff> diffs = tool.diff_lineMode(left, right);
        filterEmptyLines(diffs);

        int deletes = 0;
        int inserts = 0;
        int maxPatch = 0;
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation == DiffMatchPatch.Operation.DELETE) {
                deletes++;
                maxPatch = Math.max(diff.text.length(), maxPatch);
            }
            if (diff.operation == DiffMatchPatch.Operation.INSERT) {
                inserts++;
                maxPatch = Math.max(diff.text.length(), maxPatch);
            }
        }

        sb.append("<!DOCTYPE html><html><head><title>testlinks xmlmarket</title></head><body>");

        sb.append("<h2>");
        sb.append("changes: ");
        sb.append(deletes + inserts);
        sb.append(" (del: ");
        sb.append(deletes);
        sb.append(", ");
        sb.append("ins: ");
        sb.append(inserts);
        sb.append(") maxPatchSize: ");
        sb.append(maxPatch);
        sb.append("</h2>");

        sb.append("<table>");


        System.err.println("changes: " + (deletes + inserts)
                + " del: " + deletes
                + " ins: " + inserts
                + " maxPatchSize: " + maxPatch
         );

        /*
        sb.append("<tr><td>");
        sb.append(htmlEscape(left));
        sb.append("</td><td>");
        sb.append(htmlEscape(right));
        sb.append("</td></tr>");
        */

        sb.append("<tr><td colspan='2'>");
        sb.append(diffPrettyHtml(diffs));
        sb.append("</td></tr>");

        sb.append("</table>");

        sb.append("</body></html>");
        writer.print(sb.toString());
    }

    private void filterEmptyLines(LinkedList<DiffMatchPatch.Diff> diffs) {
        Iterator<DiffMatchPatch.Diff> iter = diffs.iterator();
        while (iter.hasNext()) {
            DiffMatchPatch.Diff diff = iter.next();
            if (diff.text.trim().length() == 0) {
                iter.remove();
            }
        }
    }

    public String diffPrettyHtml(LinkedList<DiffMatchPatch.Diff> diffs) {
        final StringBuilder html = new StringBuilder();
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.text.trim().length() == 0) {
                continue; // skip blank lines
            }
            switch (diff.operation) {
                case INSERT:
                    html.append("<ins style=\"background:#a6ffa6;\">")
                            .append(htmlEscape(diff.text))
                            .append("</ins>");
                    break;
                case DELETE:
                    html.append("<del style=\"background:#ffa6a6;\">")
                            .append(htmlEscape(diff.text))
                            .append("</del>");
                    break;
                case EQUAL:
                    html.append("<span>")
                            .append(htmlEscape(diff.text))
                            .append("</span>");
                    break;
            }
        }
        return html.toString();
    }

    private String htmlEscape(final String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "&para;<br/>")
                .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
                .replace(" ", "&nbsp;");
    }

}
