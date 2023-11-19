package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DocumentMetadata;

/**
 * Created on 24.04.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DmsLinkRenderer extends TableCellRenderers.MaxLengthStringRenderer {

    private final DmsPresenter dms;

    public DmsLinkRenderer(DmsPresenter dms) {
        super(70, null);
        this.dms = dms;
    }

    @Override
    public void render(Object data, StringBuffer sb, Context context) {
        if (!(data instanceof DocumentMetadata)) {
            sb.append("wrong type"); // $NON-NLS$
            return;
        }
        final DocumentMetadata dm = (DocumentMetadata) data;
        final LinkContext<DocumentMetadata> lc = new LinkContext<>(new LinkListener<DocumentMetadata>() {
            @Override
            public void onClick(LinkContext<DocumentMetadata> context, Element e) {
                dms.download(dm);
            }
        }, dm);

        final StringBuffer nameBuffer = new StringBuffer();
        final String documentName = ((DocumentMetadata) data).getDocumentName();


        super.render(StringUtil.hasText(documentName)
                ? documentName
                : new SafeHtmlBuilder().appendEscaped(I18n.I.unknownDocument()).toSafeHtml().asString()
                , nameBuffer, context);
        context.appendLink(lc, nameBuffer.toString(), null, sb);
    }
}