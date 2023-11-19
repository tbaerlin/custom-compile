package de.marketmaker.iview.mmgwt.mmweb.client.util.bulk;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class BulkRequestBlock extends AbstractBulkRequest<Object> {
    private final DmxmlContext context;
    private final List<DmxmlContext.Block> listBlocks = new ArrayList<DmxmlContext.Block>();

    public static BulkRequestBlock forDmxml() {
        return new BulkRequestBlock(new DmxmlContext());
    }

    public static BulkRequestBlock forPmxml() {
        return new BulkRequestBlock(new DmxmlContext());
    }

    private BulkRequestBlock(DmxmlContext context) {
        this.context = context;
        this.context.setCancellable(false);
    }

    public <V extends BlockType> DmxmlContext.Block<V> addBlock(String key) {
        final DmxmlContext.Block<V> block = this.context.addBlock(key);
        this.listBlocks.add(block);
        return block;
    }

    public void sendRequest(final BulkRequestHandler handler) {
        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
                setState(State.RESULT_ERROR);
                _onError(handler, "BulkRequestBlock returned with error", caught); // $NON-NLS$
            }

            public void onSuccess(ResponseType result) {
                try {
                    checkAllBlocksAreOk();
                    setState(State.RESULT_OK);
                    _onSuccess(handler);
                }
                catch (Exception e) {
                    setState(State.RESULT_ERROR);
                    _onError(handler, e.getMessage(), e);
                }

            }
        });
        setState(State.REQUESTED);
    }

    private void checkAllBlocksAreOk() {
        final StringBuilder sb = new StringBuilder();
        for (DmxmlContext.Block block : this.listBlocks) {
            if (!block.isResponseOk()) {
                if (sb.length() != 0) {
                    sb.append(',');
                }
                sb.append(block.getKey());
            }
        }
        if (sb.length() > 0) {
            throw new RuntimeException("The following blocks failed: " + sb.toString()); // $NON-NLS$
        }
    }

    /**
     * Always returns null, since request can contain multiple blocks and therefor multiple results.
     * @return Always null.
     */
    public Object getResult() {
        return null;
    }

}
