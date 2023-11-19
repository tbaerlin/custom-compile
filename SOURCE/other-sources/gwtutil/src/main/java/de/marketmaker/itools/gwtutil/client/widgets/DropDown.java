package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.PopupPositionCallback;

/**
 *
 * @author Felix Hoffmann
 *
 *  A DropDown field.
 *
 * <h3>CSS Style Rules</h3>
 * <ul>
 * <li>.mm-dropdown { primary style }</li>
 * <li>.mm-dropdown img { style of the button }</li>
 * <li>.mm-dropdown img.hover { dependent style added when the button is hovered }</li>
 * <li>.mm-dropdown input { style of the textbox }</li>
 * <li>.mm-dropdown.focus { dependent style added when the textbox has focus }</li>
 * <li>.mm-dropdown.focus img { dependent style of the button when the textbox has focus }</li>
 * <li>.mm-dropdown.focus img.hover { dependent style of the button when the textbox has focus and button is hovered}</li>
 * <li>.mm-dropdown.focus input { dependent style of the textbox when it has focus }</li>
 * <li>.mm-dropdown.disabled { dependent style added when DropDown is disabled }</li>
 * <li>.mm-dropdown.disabled img { dependent style of the button when DropDown is disabled }</li>
 * <li>.mm-dropdown.disabled img.hover { dependent style of the button when DropDown is disabled and button is hovered }</li>
 * <li>.mm-dropdown.disabled input { dependent style of the textbox when DropDown is disabled }</li>
 *
 * <li>.mm-dropdown-popup { style of the PopUp }</li>
 * <li>.mm-dropdown-popup .item { style of the Items within the PopUp }</li>
 * <li>.mm-dropdown-popup .item.hover { style of the currently hovered Item }</li>
 * </ul>
 *
 * <h3>Examples</h3>
 * <h4>Hovered button</h1>
 * <pre>{@code
 * <div class="mm-dropdown">
 *      <input type="text" class="gwt-TextBox gwt-TextBox-readonly"/>
 *      <img src="clear.cache.gif" class="gwt-Image hover" />
 * </div>}</pre>
 * <h4>Focused textbox</h1>
 * <pre>{@code
 * <div class="mm-dropdown focus">
 *      <input type="text" class="gwt-TextBox gwt-TextBox-readonly"/>
 *      <img src="clear.cache.gif" class="gwt-Image" />
 * </div>}</pre>
 * <h4>Disabled DropDown</h1>
 * <pre>{@code
 * <div class="mm-dropdown disabled">
 *      <input type="text" class="gwt-TextBox gwt-TextBox-readonly"/>
 *      <img src="clear.cache.gif" class="gwt-Image" />
 * </div>}</pre>
 */
@SuppressWarnings({"GWTStyleCheck", "UnusedDeclaration"})
public class DropDown<T> extends Composite implements HasValueChangeHandlers<T> {

    private final DropDownModel<T> model = new DropDownModel<T>();
    private final PopupPanel pnlPopup = new PopupPanel(true);
    private final Panel pnlPopupItems = new FlowPanel();
    private final TextBox text = new TextBox();
    private Label selectedHoverLabel = null;
    private int maxPopupHeight = 140;
    private final Image button = new Image("clear.cache.gif");
    private final Panel pnlDropDown;

    public DropDown() {
        this.pnlDropDown = new HorizontalPanel();
        this.pnlDropDown.setStyleName("mm-dropdown");
        this.pnlPopup.setGlassEnabled(true);
        this.pnlPopup.setStyleName("mm-dropdown-popup");

        this.button.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                button.addStyleName("hover");
            }
        });
        this.button.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                button.removeStyleName("hover");
            }
        });
        this.button.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showPopup();
            }
        });

        this.text.setReadOnly(true);
        this.text.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                pnlDropDown.addStyleName("focus");
            }
        });
        this.text.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                pnlDropDown.removeStyleName("focus");
//                pnlPopup.hide();
            }
        });
        this.text.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showPopup();
            }
        });
        this.text.addKeyPressHandler(new KeyPressHandler(){
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
                    selectNextItem();
                }
                else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
                    selectPreviousItem();
                }
            }
        });

        pnlDropDown.add(this.text);
        pnlDropDown.add(this.button);

        final ScrollPanel pnlScroll = new ScrollPanel(this.pnlPopupItems);
        pnlScroll.setHeight("auto");

        this.pnlPopup.setWidget(pnlScroll);
        PopupPanelFix.addFrameDummy(this.pnlPopup);

        initWidget(pnlDropDown);
    }

    private void scalePopup() {
        final int offsetHeight = this.pnlPopupItems.getOffsetHeight();
        if (offsetHeight > this.maxPopupHeight) {
            this.pnlPopup.setHeight(this.maxPopupHeight + "px");
        }
        else {
            this.pnlPopup.setHeight("auto");
        }

        final int formItemWidth = this.text.getOffsetWidth() + this.button.getOffsetWidth();
        this.pnlPopup.setWidth(formItemWidth - 2 + "px");
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> tValueChangeHandler) {
        return addHandler(tValueChangeHandler, ValueChangeEvent.getType());
    }

    private void showPopup() {
        if (!text.isEnabled()) {
            return;
        }
        text.setFocus(true);

        final PopupPositionCallback ppc = new PopupPositionCallback(this.text, pnlPopup);
        pnlPopup.setPopupPositionAndShow(
                new PopupPanel.PositionCallback() {
                    public void setPosition(int offsetWidth, int offsetHeight) {
                        scalePopup();
                        ppc.setPosition(pnlPopup.getOffsetWidth(), pnlPopup.getOffsetHeight());
                    }
                }
        );
    }

    class PopupLabel extends Label {
        public PopupLabel(final DropDownModel<T>.Item<T> item) {
            super(item.getDisplay());
            this.setStyleName("item");
            addMouseOverHandler(new MouseOverHandler() {
                public void onMouseOver(MouseOverEvent event) {
                    setHoverStyle(PopupLabel.this);
                }
            });
            addMouseOutHandler(new MouseOutHandler() {
                public void onMouseOut(MouseOutEvent event) {
                    setHoverStyle(null);
                }
            });
            addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    valueClicked(item);
                }
            });
        }
    }

    private void valueClicked(DropDownModel<T>.Item<T> item) {
        DropDownModel<T>.Item<T> oldItem = model.getSelectedItem();
        model.setSelectedItem(item);
        this.text.setText(item.getDisplay());
        this.pnlPopup.hide();

        ValueChangeEvent.fireIfNotEqual(this, oldItem == null ? null : oldItem.getValue(), item.getValue());
    }

    private void setHoverStyle(Label label) {
        if (this.selectedHoverLabel != null) {
            this.selectedHoverLabel.removeStyleName("hover");
        }
        this.selectedHoverLabel = label;
        if (label != null) {
            label.addStyleName("hover");
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getMaxPopupHeight() {
        return maxPopupHeight;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setMaxPopupHeight(int maxPopupHeight) {
        this.maxPopupHeight = maxPopupHeight;
    }

    public T getValue() {
        return model.getSelectedItem().getValue();
    }

    public String getDisplay() {
        return model.getSelectedItem().getDisplay();
    }

    public int getItemCount() {
        return model.getItems().size();
    }

    public String getItemText(int index) {
        return model.getItem(index).getDisplay();
    }

    public String getName() {
        return this.text.getName();
    }

    public int getSelectedIndex() {
        return model.getItems().indexOf(model.getSelectedItem());
    }

    public int getItemIndex(T value) {
        return this.model.getItemIndex(value);
    }

    public void remove(T value) {
        this.remove(getItemIndex(value));
    }

    public void remove(DropDownModel<T>.Item<T> item) {
        this.remove(model.getItems().indexOf(item));
    }

    public void remove(int index) {
        final boolean selected = index == this.getSelectedIndex();
        model.getItems().remove(index);
        this.pnlPopupItems.getElement().getChild(index).removeFromParent();
        if (selected) {
            if (model.getItems().size() > index) {
                this.setSelectedIndex(index);
            }
            else {
                this.setSelectedIndex(index - 1);
            }
        }
    }

    public void clear() {
        model.getItems().clear();
        this.pnlPopupItems.clear();
    }

    public DropDownModel<T>.Item<T> add(T value, String display) {
        final DropDownModel<T>.Item<T> it = model.createItem(value, display);
        model.getItems().add(it);
        final Label l = new PopupLabel(it);
        this.pnlPopupItems.add(l);
        return it;
    }

    public DropDownModel<T>.Item<T> add(DropDownModel<T>.Item<T> item) {
        model.getItems().add(item);
        final Label l = new PopupLabel(item);
        this.pnlPopupItems.add(l);
        return item;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean isItemSelected(int index) {
        return index == model.getItems().indexOf(model.getSelectedItem());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setItemText(int index, String display) {
        model.getItem(index).setDisplay(display);
    }

    public void setName(String name) {
        this.text.setName(name);
    }

    public void setEnabled(boolean enabled) {
        this.text.setEnabled(enabled);
        if (enabled) {
            this.removeStyleName("disabled");
        }
        else {
            this.addStyleName("disabled");
        }
    }

    public boolean isEnabled() {
        return this.text.isEnabled();
    }

    public boolean setSelected(T t) {
        final int idx = getItemIndex(t);
        if (idx >= 0) {
            setSelectedIndex(idx);
            return true;
        }
        return false;
    }

    public void setSelectedIndex(int index) {
        final DropDownModel<T>.Item<T> it = model.getItems().get(index);
        model.setSelectedItem(it);
        this.text.setText(it.getDisplay());
    }

    public void setValue(int index, T t) {
        final DropDownModel<T>.Item<T> it = model.getItems().get(index);
        it.setValue(t);
    }

    private void selectNextItem() {
        int nextIndex = getSelectedIndex() + 1;
        if (nextIndex < model.getItems().size()) {
            setSelectedIndex(nextIndex);
        }
    }

    private void selectPreviousItem() {
        int nextIndex = getSelectedIndex() - 1;
        if (nextIndex >= 0) {
            setSelectedIndex(nextIndex);
        }
    }
}
