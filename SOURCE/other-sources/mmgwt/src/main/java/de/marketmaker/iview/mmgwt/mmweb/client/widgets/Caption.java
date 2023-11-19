/*
 * Caption.java
 *
 * Created on 25.11.2015 10:45
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * A lazily vwd styled form caption.
 * @author mdick
 */
public class Caption extends Widget implements HasText {
    private Element captionElement;

    private String text;

    private InfoIcon infoIcon;

    public Caption() {
        super();
        final Element div = Document.get().createDivElement();
        setElement(div);
        this.captionElement = div;
        this.captionElement.setClassName(Styles.get().caption());
    }

    public Caption(String text) {
        this();
        setText(text);
    }

    public Caption withInfoIcon(InfoIcon infoIcon) {
        if(infoIcon == null) {
            return this;
        }
        initInfoIcon(infoIcon);
        return this;
    }

    /** Rearranges the internal structure to hold the given info icon.
     * from:
     * <ul>
     *     <li>div:caption
     *     <ul><li>text</li></ul>
     *     </li>
     * </ul>
     *
     * to:
     * <ul>
     *     <li>div:caption-panel
     *     <ul>
     *         <li>div:infoIcon</li>
     *         <li>div:caption
     *         <ul><li>text</li></ul>
     *         </li>
     *     </ul>
     *     </li>
     *     <li>div:caption-panel</li>
     * </ul>
     */
    private void initInfoIcon(InfoIcon infoIcon) {
        assert(this.infoIcon == null);

        this.infoIcon = infoIcon;
        getElement().setInnerHTML(null);
        getElement().setClassName(Styles.get().captionPanel());
        getElement().insertFirst(this.infoIcon.getElement());

        this.captionElement = Document.get().createDivElement();
        this.captionElement.setClassName(Styles.get().caption());
        getElement().appendChild(this.captionElement);
        doSetText();
    }

    @Override
    public void setText(String text) {
        this.text = text;
        doSetText();
    }

    private void doSetText() {
        this.captionElement.setInnerHTML(SafeHtmlUtils.htmlEscape(this.text));
    }

    @Override
    public String getText() {
        return this.text;
    }
}
