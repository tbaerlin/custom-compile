package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.extjs.gxt.ui.client.widget.Component;

/**
 * Marker interface to specify the implementing view as advisory solution module.
 * advisory solution modules are handled specially in DemoAsMainView.
 * @author umaurer
 */
public interface AsModule {
    Component getAsModuleComponent();
}
