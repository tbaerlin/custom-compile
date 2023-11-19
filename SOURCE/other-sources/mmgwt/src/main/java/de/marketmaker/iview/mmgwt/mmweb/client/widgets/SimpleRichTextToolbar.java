package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

/**
 * @author Ulrich Maurer
 *         Date: 15.05.12
 */

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;

/**
 * @author Ulrich Maurer
 *         Date: 15.05.12
 */
public class SimpleRichTextToolbar extends Composite {
    private final RichTextArea.Formatter formatter;
    private final ImageButton btnBold;
    private final ImageButton btnItalic;
    private final ImageButton btnUnderline;
    private final ImageButton btnStrikethrough;
    private final ColorChooser colorChooser;

    public SimpleRichTextToolbar(RichTextArea richTextArea) {
        richTextArea.addMouseUpHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                updateStatus();
            }
        });
        richTextArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateStatus();
            }
        });
        this.formatter = richTextArea.getFormatter();

        btnBold = GuiUtil.createImageButton("mm-richtext-bold", null, "mm-richtext-italic", "Fett"); // TODO: // $NON-NLS$
        btnBold.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        btnBold.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.toggleBold();
            }
        });

        btnItalic = GuiUtil.createImageButton("mm-richtext-italic", null, null, "Kursiv"); // TODO: // $NON-NLS$
        btnItalic.getElement().getStyle().setFontStyle(Style.FontStyle.ITALIC);
        btnItalic.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.toggleItalic();
            }
        });

        btnUnderline = GuiUtil.createImageButton("mm-richtext-underline", null, null, "Unterstrichen"); // TODO: // $NON-NLS$
        btnUnderline.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
        btnUnderline.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.toggleUnderline();
            }
        });

        btnStrikethrough = GuiUtil.createImageButton("mm-richtext-strikethrough", null, null, "Durchgestrichen"); // TODO: // $NON-NLS$
        btnStrikethrough.getElement().getStyle().setTextDecoration(Style.TextDecoration.LINE_THROUGH);
        btnStrikethrough.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.toggleStrikethrough();
            }
        });

        final ImageButton btnUl = GuiUtil.createImageButton("mm-richtext-unordered", null, null, "Punktaufzählung"); // TODO: // $NON-NLS$
        btnUl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.insertUnorderedList();
            }
        });

        final ImageButton btnNl = GuiUtil.createImageButton("mm-richtext-ordered", null, null, "Nummerierte Aufzählung"); // TODO: // $NON-NLS$
        btnNl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.insertOrderedList();
            }
        });

        final ImageButton btnLeft = GuiUtil.createImageButton("mm-richtext-left", null, null, "Ausrichtung: links"); // TODO: // $NON-NLS$
        btnLeft.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.setJustification(RichTextArea.Justification.LEFT);
            }
        });

        final ImageButton btnCenter = GuiUtil.createImageButton("mm-richtext-center", null, null, "Ausrichtung: zentriert"); // TODO: // $NON-NLS$
        btnCenter.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.setJustification(RichTextArea.Justification.CENTER);
            }
        });

        final ImageButton btnRight = GuiUtil.createImageButton("mm-richtext-right", null, null, "Ausrichtung: rechts"); // TODO: // $NON-NLS$
        btnRight.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.setJustification(RichTextArea.Justification.RIGHT);
            }
        });

        colorChooser = new ColorChooser();

        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
        table.setCellPadding(3);
        int col = -1;
        table.setWidget(0, ++col, btnBold);
        table.setWidget(0, ++col, btnItalic);
        table.setWidget(0, ++col, btnUnderline);
        table.setWidget(0, ++col, btnStrikethrough);
        table.setHTML(0, ++col, "&nbsp;"); // $NON-NLS$
        cellFormatter.setWidth(0, col, "20"); // $NON-NLS$
        table.setWidget(0, ++col, colorChooser);
        table.setHTML(0, ++col, "&nbsp;"); // $NON-NLS$
        cellFormatter.setWidth(0, col, "20"); // $NON-NLS$
        table.setWidget(0, ++col, btnUl);
        table.setWidget(0, ++col, btnNl);
        table.setHTML(0, ++col, "&nbsp;"); // $NON-NLS$
        cellFormatter.setWidth(0, col, "20"); // $NON-NLS$
        table.setWidget(0, ++col, btnLeft);
        table.setWidget(0, ++col, btnCenter);
        table.setWidget(0, ++col, btnRight);
        initWidget(table);

        updateStatus();
    }

    private void updateStatus() {
        this.btnBold.setActive(this.formatter.isBold());
        this.btnItalic.setActive(this.formatter.isItalic());
        this.btnUnderline.setActive(this.formatter.isUnderlined());
        this.btnStrikethrough.setActive(this.formatter.isStrikethrough());
        this.colorChooser.setColor(this.formatter.getForeColor());
    }

    class ColorChooser extends Composite {
        private final HTML html = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"); // $NON-NLS$
        private PopupPanel popupPanel = null;

        ColorChooser() {
            final Style style = html.getElement().getStyle();
            style.setBackgroundColor("black"); // $NON-NLS$
            style.setCursor(Style.Cursor.POINTER);
            html.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (popupPanel == null) {
                        popupPanel = new PopupPanel(true, true);
                        popupPanel.setStyleName("mm-popupPanel");

                        final FlowPanel panel = new FlowPanel();
                        panel.add(createColor("black")); // $NON-NLS$
                        panel.add(createColor("red")); // $NON-NLS$
                        panel.add(createColor("orange")); // $NON-NLS$
                        panel.add(createColor("yellow")); // $NON-NLS$
                        panel.add(createColor("green")); // $NON-NLS$
                        panel.add(createColor("blue")); // $NON-NLS$
                        panel.add(createColor("purple")); // $NON-NLS$

                        popupPanel.setWidget(panel);
                    }
                    popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                        @Override
                        public void setPosition(int offsetWidth, int offsetHeight) {
                            popupPanel.setPopupPosition(html.getAbsoluteLeft(), html.getAbsoluteTop() + html.getOffsetHeight());
                        }
                    });
                }
            });
            initWidget(this.html);
        }

        public void setColor(String color) {
            html.getElement().getStyle().setBackgroundColor(color == null || color.isEmpty() ? "black" : color); // $NON-NLS$
            closePopup();
        }

        public void closePopup() {
            if (this.popupPanel == null) {
                return;
            }
            this.popupPanel.hide();
        }
    }

    private Widget createColor(final String color) {
        final HTML html = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"); // $NON-NLS$
        final Style style = html.getElement().getStyle();
        style.setBackgroundColor(color);
        style.setMargin(2, Style.Unit.PX);
        style.setCursor(Style.Cursor.POINTER);
        html.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formatter.setForeColor(color);
                colorChooser.setColor(color);
            }
        });
        return html;
    }
}
