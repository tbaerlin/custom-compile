package de.marketmaker.itools.gwtutil.client.widgets.tree;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTree;

/**
 * @author Ulrich Maurer
 *         Date: 26.02.13
 */
public class SimpleCellTreeResources implements CellTree.Resources {
    private CellTree.Style style = new CellTree.Style() {
        @Override
        public String cellTreeEmptyMessage() {
            return "cellTreeEmptyMessage";
        }

        @Override
        public String cellTreeItem() {
            return "cellTreeItem";
        }

        @Override
        public String cellTreeItemImage() {
            return "cellTreeItemImage";
        }

        @Override
        public String cellTreeItemImageValue() {
            return "cellTreeItemImageValue";
        }

        @Override
        public String cellTreeItemValue() {
            return "cellTreeItemValue";
        }

        @Override
        public String cellTreeKeyboardSelectedItem() {
            return "cellTreeKeyboardSelectedItem";
        }

        @Override
        public String cellTreeOpenItem() {
            return "cellTreeOpenItem";
        }

        @Override
        public String cellTreeSelectedItem() {
            return "cellTreeSelectedItem";
        }

        @Override
        public String cellTreeShowMoreButton() {
            return "cellTreeShowMoreButton";
        }

        @Override
        public String cellTreeTopItem() {
            return "cellTreeTopItem";
        }

        @Override
        public String cellTreeTopItemImage() {
            return "cellTreeTopItemImage";
        }

        @Override
        public String cellTreeTopItemImageValue() {
            return "cellTreeTopItemImageValue";
        }

        @Override
        public String cellTreeWidget() {
            return widgetStyleName;
        }

        @Override
        public boolean ensureInjected() {
            return false;
        }

        @Override
        public String getText() {
            return "getText";
        }

        @Override
        public String getName() {
            return "getName";
        }
    };

    private String widgetStyleName;
    private final ImageResource closedItem;
    private final ImageResource openItem;
    private final ImageResource loading;
    private final ImageResource selectedBackground;


    public SimpleCellTreeResources() {
        this("cellTree",
                new ImageResourcePrototype("closedItem", UriUtils.unsafeCastFromUntrustedString("svg/mm/gwtutil/tree/closedItem.svg"), 0, 0, 15, 15, false, false),
                new ImageResourcePrototype("openItem", UriUtils.unsafeCastFromUntrustedString("svg/mm/gwtutil/tree/openItem.svg"), 0, 0, 15, 15, false, false),
                new ImageResourcePrototype("loading", UriUtils.unsafeCastFromUntrustedString("svg/mm/gwtutil/tree/loading.svg"), 0, 0, 15, 15, true, false),
                new ImageResourcePrototype("selectedBackground", UriUtils.unsafeCastFromUntrustedString("svg/mm/gwtutil/tree/selectedBackground.svg"), 0, 0, 26, 26, false, false)
        );
    }

    public SimpleCellTreeResources(ImageResource closedItem, ImageResource openItem, ImageResource loading, ImageResource selectedBackground) {
        this("cellTree", closedItem, openItem, loading, selectedBackground);
    }

    public SimpleCellTreeResources(String widgetStyleName, ImageResource closedItem, ImageResource openItem, ImageResource loading, ImageResource selectedBackground) {
        this.widgetStyleName = widgetStyleName;
        this.closedItem = closedItem;
        this.openItem = openItem;
        this.loading = loading;
        this.selectedBackground = selectedBackground;
    }

    public SimpleCellTreeResources withWidgetStyleName(String styleName) {
        this.widgetStyleName = "cellTree " + styleName;
        return this;
    }

    @Override
    public ImageResource cellTreeClosedItem() {
        return this.closedItem;
    }

    @Override
    public ImageResource cellTreeLoading() {
        return this.loading;
    }

    @Override
    public ImageResource cellTreeOpenItem() {
        return this.openItem;
    }

    @Override
    public ImageResource cellTreeSelectedBackground() {
        return this.selectedBackground;
    }

    @Override
    public CellTree.Style cellTreeStyle() {
        return this.style;
    }
}
