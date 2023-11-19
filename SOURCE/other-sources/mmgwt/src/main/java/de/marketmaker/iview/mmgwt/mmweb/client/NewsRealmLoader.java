package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;
import de.marketmaker.iview.dmxml.NWSSearchMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;


/**
 * helper for implementing a callback for NWS_SearchMetadata
 */
public abstract class NewsRealmLoader extends ResponseTypeCallback implements Command {

    private DmxmlContext.Block<NWSSearchMetadata> block;

    private final DmxmlContext context = new DmxmlContext();

    private final String blockName = FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() ? "NWS_FinderMetadata" : "NWS_SearchMetadata"; // $NON-NLS$

    @Override
    public void execute() {
        this.block = this.context.addBlock(this.blockName);
        this.context.setCancellable(false);
        this.context.issueRequest(this);
    }

    @Override
    protected void onResult() {
        if (this.throwable == null) {
            handleResult(block.getResult());
        }
        else {
            DebugUtil.logToServer("NewsRealmLoader failed", this.throwable); // $NON-NLS-0$
        }
    }

    public abstract void handleResult(NWSSearchMetadata result);

}
