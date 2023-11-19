package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.CollapsePanel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

/**
 * @author Ulrich Maurer
 *         Date: 24.11.11
 */
public class StyledBorderLayout extends BorderLayout {
    private final Map<Style.LayoutRegion, String> mapStyles = new HashMap<Style.LayoutRegion, String>();

    public StyledBorderLayout() {
        addStyleName(Style.LayoutRegion.WEST,  "mm-collapsed-west"); // $NON-NLS$
        addStyleName(Style.LayoutRegion.EAST,  "mm-collapsed-east"); // $NON-NLS$
        addStyleName(Style.LayoutRegion.NORTH,  "mm-collapsed-north"); // $NON-NLS$
        addStyleName(Style.LayoutRegion.SOUTH,  "mm-collapsed-south"); // $NON-NLS$
    }

    public void addStyleName(Style.LayoutRegion region, String styleName) {
        final String previousStyle = this.mapStyles.get(region);
        this.mapStyles.put(region, previousStyle == null ? styleName : (previousStyle + " " + styleName));
    }

    @Override
    protected CollapsePanel createCollapsePanel(ContentPanel panel, BorderLayoutData data) {
        final CollapsePanel collapsePanel = super.createCollapsePanel(panel, data);
        final String styleName = this.mapStyles.get(data.getRegion());
        if (styleName != null) {
            collapsePanel.addStyleName(styleName);
        }
        return collapsePanel;
    }
}
