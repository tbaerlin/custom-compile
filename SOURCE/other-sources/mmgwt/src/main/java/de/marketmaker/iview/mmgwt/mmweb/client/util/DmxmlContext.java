/*
 * DmxmlContext.java
 *
 * Created on 28.03.2008 10:36:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.Parameter;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.dmxml.RequestedBlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents both a dmxml request and the resulting response by encapsulating the interaction
 * with both {@link de.marketmaker.iview.dmxml.RequestType} and
 * {@link de.marketmaker.iview.dmxml.ResponseType} objects.<p>
 * Each context contains a number of blocks that correspond to the blocks in a RequestType.
 * Blocks can be created by calling {@link #addBlock(String)} and manipulated in
 * a number of ways (i.e., adding, changing, and removing parameters)<p>
 * Once the blocks are ready to be requested, a client calls
 * {@link #issueRequest(com.google.gwt.user.client.rpc.AsyncCallback)}. On receiving the response,
 * the result will be inspected and the contained sub-results will be associated with the
 * respective blocks. When the supplied AsyncCallback is informed about success or failure of the
 * request, it can retrieve the results right from the blocks and does not have to inspect
 * the ResponseType object itself.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DmxmlContext implements AsyncCallback<ResponseType> {
    /**
     * Separator for correlation ids
     */
    public static final String CID_SEPARATOR = ","; // $NON-NLS-0$
    public static final String REQUEST_KEY = "request"; // $NON-NLS$

    public class Block<V extends BlockType> implements BlockMemento {
        // incremented with each new result received for this block, used to detect whether
        // the result changed.

        private int changeNo = 0;

        private Block dependsOn;

        private boolean enabled = true;

        private boolean refreshOnRequest = true;

        private ErrorType error;

        private double expires = getCurrentTime();

        private final String id;

        private final String key;

        private HashMap<String, String[]> parameters = new HashMap<>();

        private HashMap<String, Parameter> complexParameters = new HashMap<>();

        private V result;

        private BlockState state = BlockState.NOT_REQUESTED;

        private BlockMemento memento = null;

        private Block(String key, String id) {
            this.key = key;
            this.id = id;
        }

        public void disableRefreshOnRequest() {
            this.refreshOnRequest = false;
        }

        /**
         * Use this method if this block is only checked by a single client
         * @return true iff block changed
         */
        public boolean blockChanged() {
            if (this.memento == null) {
                this.memento = createMemento();
                return isEnabled();
            }
            return this.memento.blockChanged();
        }

        /**
         * use this method to create BlockMementos for multiple objects that need to check the
         * block individually for modifications.
         * @return new memento
         */
        public BlockMemento createMemento() {
            return new BlockMemento() {
                private int myChangeNo = Block.this.changeNo;

                public boolean blockChanged() {
                    if (this.myChangeNo == Block.this.changeNo) {
                        return false;
                    }
                    this.myChangeNo = Block.this.changeNo;
                    return true;
                }
            };
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void disable() {
            setEnabled(false);
        }

        public void enable() {
            setEnabled(true);
        }

        public ErrorType getError() {
            return (this.state == BlockState.ERROR) ? this.error : null;
        }

        public String getKey() {
            return key;
        }

        public String getId() {
            return id;
        }

        public String getParameter(String key) {
            final String[] values = this.parameters.get(key);
            return (values != null && values.length > 0) ? values[0] : null;
        }

        public int getParameterAsInt(String key) {
            final String s = getParameter(key);
            return (s != null) ? Integer.parseInt(s) : -1;
        }

        public String[] getParameters(String key) {
            return this.parameters.get(key);
        }

        public V getResult() {
            return (this.enabled && this.state == BlockState.OK) ? this.result : null;
        }

        public BlockState getState() {
            return this.state;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isPending() {
            return this.state == BlockState.PENDING;
        }

        public boolean isNotRequested() {
            return this.state == BlockState.NOT_REQUESTED;
        }

        public boolean isResponseOk() {
            return this.state == BlockState.OK;
        }

        public boolean isToBeRequested() {
            if (!this.enabled) {
                return false;
            }
            if (this.dependsOn != null && this.dependsOn.isToBeRequested()) {
                return true;
            }
            if (DmxmlContext.refreshRequested && this.refreshOnRequest) {
                return true;
            }
            if (isResponseOk() && this.expires > getCurrentTime()) {
                return false;
            }
            return true;
        }

        public void removeAllParameters() {
            if (!this.parameters.isEmpty()) {
                setToBeRequested();
            }
            this.parameters.clear();
            this.complexParameters.clear();
        }

        public void removeParameter(String key) {
            if (this.parameters.remove(key) != null || this.complexParameters.remove(key) != null) {
                setToBeRequested();
            }
        }

        public void setDependsOn(Block other) {
            this.dependsOn = other;
            do {
                assert other != this : "cyclic dependency"; // $NON-NLS-0$
            } while ((other = other.dependsOn) != null);
        }

        public void addParametersFrom(Block<V> block) {
            this.parameters.putAll(block.parameters);
            this.complexParameters.putAll(block.complexParameters);
            setToBeRequested();
        }

        public boolean setParameter(String key, Boolean value) {
            return setParameter(key, value.toString());
        }

        public boolean setParameter(String key, Number value) {
            return setParameter(key, value.toString());
        }

        public boolean setParameter(String key, String value) {
            final String old = getParameter(key);
            if (value == null) {
                if (old == null) {
                    return false;
                }
                setToBeRequested();
                this.parameters.remove(key);
                return true;
            }
            if (value.equals(old)) {
                return false;
            }
            setParameters(key, new String[]{value});
            return true;
        }

        public void setParameters(String key, String[] values) {
            final String[] old = getParameters(key);
            if (values == null) {
                if (old == null) {
                    return;
                }
                setToBeRequested();
                this.parameters.remove(key);
                return;
            }
            this.parameters.put(key, values);
            if (old == null || !equals(old, values)) {
                setToBeRequested();
            }
        }

        public void setParameter(Parameter parameter) {
            if (!StringUtil.hasText(parameter.getKey())) {
                parameter.setKey(REQUEST_KEY);
            }
            this.complexParameters.put(parameter.getKey(), parameter);
            setToBeRequested();
        }

        public Parameter getComplexParameter(String key) {
            return this.complexParameters.get(key);
        }

        @SuppressWarnings("unchecked")
        public <T extends Parameter> T getComplexParameter(String key, Class<T> type) {
            final Parameter parameter = this.complexParameters.get(key);
            if (parameter.getClass() == type) {
                return (T) parameter;
            } else {
                throw new IllegalArgumentException("type doesn't match! parameter: " + // $NON-NLS$
                        parameter.getClass() + " / type: " + type); // $NON-NLS$
            }
        }

        private boolean equals(String[] old, String[] values) {
            if (old.length != values.length) {
                return false;
            }
            for (int i = 0; i < old.length; i++) {
                if (!old[i].equals(values[i])) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            final StringBuffer sb = new StringBuffer(100);
            sb.append("Block[").append(this.id).append(", state=").append(this.state.name()); // $NON-NLS-0$ $NON-NLS-1$
            sb.append(", ").append(this.key).append(", params={"); // $NON-NLS-0$ $NON-NLS-1$
            boolean first = true;
            for (String s : parameters.keySet()) {
                if (!first) sb.append(", "); // $NON-NLS-0$
                first = false;
                sb.append(s).append("="); // $NON-NLS-0$
                final String[] values = parameters.get(s);
                if (values.length == 1) {
                    sb.append(values[0]);
                }
                else {
                    sb.append("["); // $NON-NLS-0$
                    for (int i = 0; i < values.length; i++) {
                        if (i > 0) sb.append(", "); // $NON-NLS-0$
                        sb.append(values[i]);
                    }
                    sb.append("]"); // $NON-NLS-0$
                }
            }
            sb.append("}"); // $NON-NLS-0$
            if (!this.complexParameters.isEmpty()) {
                sb.append(", complexParams={"); // $NON-NLS-0$
                for (String s : complexParameters.keySet()) {
                    if (!first) sb.append(", "); // $NON-NLS-0$
                    first = false;
                    sb.append(s).append("=<complex parameter>"); // $NON-NLS-0$
                }
                sb.append("}"); // $NON-NLS-0$
            }
            sb.append("]"); // $NON-NLS-0$
            return sb.toString();
        }

        /**
         * Makes sure this block will be requested next time the context issues a request.
         * Automatically triggered by changing parameters, but may also be called to force
         * a request even if nothing changed.
         */
        public void setToBeRequested() {
            this.state = BlockState.NOT_REQUESTED;
        }

        private void addParametersTo(RequestedBlockType result) {
            for (String key : this.parameters.keySet()) {
                final String[] values = this.parameters.get(key);
                for (String value : values) {
                    result.getParameter().add(createParameter(key, value));
                }
            }
            for (String key : this.complexParameters.keySet()) {
                final Parameter complexParameter = this.complexParameters.get(key);
                result.getParameter().add(complexParameter);
            }
        }

        private Parameter createParameter(String key, String value) {
            final Parameter result = new Parameter();
            result.setKey(key);
            result.setValue(value);
            return result;
        }

        private double parseTtl(String ttl) {
            if (ttl.startsWith("PT")) { // $NON-NLS-0$
                final int amount = Integer.parseInt(ttl.substring(2, ttl.length() - 1));
                if (ttl.endsWith("S")) { // $NON-NLS-0$
                    return 1000 * amount;
                }
                if (ttl.endsWith("M")) { // $NON-NLS-0$
                    return 60 * 1000 * amount;
                }
                if (ttl.endsWith("H")) { // $NON-NLS-0$
                    return 60 * 60 * 1000 * amount;
                }
            }
            else if (ttl.startsWith("P")) { // $NON-NLS-0$
                return 8 * 60 * 60 * 1000; // 8h maximum
            }
            return 60 * 1000; // default: 1 min
        }

        private void setError(ErrorType result) {
            this.error = result;
            this.state = BlockState.ERROR;
            this.expires = 0;
            this.changeNo++;
        }

        public void setError() {
            setError(null);
        }

        private void setResult(V result) {
            this.result = result;
            this.state = BlockState.OK;
            this.expires = getCurrentTime() + parseTtl(result.getTtl());
            this.changeNo++;
        }

        private double getCurrentTime() {
            return GWT.isClient() ? Duration.currentTimeMillis() : System.currentTimeMillis();
        }

        private void setPending() {
            this.state = BlockState.PENDING;
            this.error = null;
            this.result = null;
        }

        private RequestedBlockType toRequestedBlockType() {
            final RequestedBlockType result = new RequestedBlockType();
            result.setKey(this.key);
            result.setId(this.id);
            if (this.dependsOn != null) {
                result.setDependsOnId(this.dependsOn.id);
            }
            addParametersTo(result);
            return result;
        }

        /**
         * Issues a request with just this block
         * @param delegate callback for success or failure
         */
        public void issueRequest(final AsyncCallback<ResponseType> delegate) {
            if (!this.isToBeRequested()) {
                delegate.onSuccess(null);
                return;
            }
            setPending();
            final RequestType request = new RequestType();
            request.getBlock().add(toRequestedBlockType());
            DmxmlContext.this.issueRequest(delegate, request);
        }

        /**
         * Returns true if the other block represents the same request as this one.
         * @param other to be compared
         * @return true iff other represents same request
         */
        private boolean isSameRequestAs(Block other) {
            if (!this.key.equals(other.key)) {
                return false;
            }
            if (this.dependsOn != other.dependsOn) {
                return false;
            }
            if (this.parameters.size() != other.parameters.size()) {
                return false;
            }
            for (Map.Entry<String, String[]> entry : this.parameters.entrySet()) {
                final String[] params2 = other.getParameters(entry.getKey());
                if (!Arrays.equals(entry.getValue(), params2)) {
                    return false;
                }
            }
            if (!this.complexParameters.isEmpty()) {
                // TODO: introduce equality check for complex parameter
                return false;
            }
            return true;
        }
    }

    /**
     * Allows to check whether a block's result changed since the Memento was created or
     * its {@link de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext.BlockMemento#blockChanged()}
     * method was called last time.
     */
    public interface BlockMemento {
        /**
         * Returns true iff the block's result (or error) changed after this method has been called
         * for the last time (or after this object has been created)
         * @return whether the block changed
         */
        boolean blockChanged();
    }

    private enum BlockState {
        NOT_REQUESTED, // parameters changed, not requested
        PENDING,       // requested, no result yet
        OK,            // result available for current parameters
        ERROR          // current parameters yielded an error
    }

    private static int currentInstanceId = 0;

    public static boolean refreshRequested = false;


    private final ArrayList<Block> blocks = new ArrayList<>();

    private int currentBlockId = 0;

    private final int instanceId = ++currentInstanceId;

    private boolean cancellable = true;

    public DmxmlContext withBlocks(ArrayList<? extends Block> blocks) {
        this.blocks.clear();
        this.blocks.addAll(blocks);
        return this;
    }

    public <V extends BlockType> Block<V> addBlock(String key) {
        return addBlock(key, getBlockId());
    }

    public <V extends BlockType> Block<V> addBlock(String key, String id) {
        assert key != null;
        assert id != null;
        final Block<V> result = new Block<>(key, id);
        this.blocks.add(result);
        return result;
    }

    public <V extends BlockType> Block<V> addBlock(String key, Map<String, String> parameters) {
        final Block<V> block = addBlock(key, getBlockId());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            block.setParameter(entry.getKey(), entry.getValue());
        }
        return block;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public void addParameter(String key, String value) {
        addParameters(key, new String[]{value});
    }

    public void addParameters(String key, String[] value) {
        for (Block block : this.blocks) {
            block.setParameters(key, value);
        }
    }

    public Block getBlock(String id) {
        final int p = getBlockPosition(id);
        return p != -1 ? this.blocks.get(p) : null;
    }

    /**
     * Calls {@link #toRequest()} and issues that request immediately. On receiving the response,
     * at first this objects AsyncCallback methods will be invoked (to assign the results to the
     * respective blocks) and then the ones of the specified delegate.
     * @param delegate will be informed about success or failure
     */
    public void issueRequest(final AsyncCallback<ResponseType> delegate) {
        final RequestType request = toRequest();
        if (request.getBlock().isEmpty()) {
            onEmptyRequest();
            delegate.onSuccess(null);
            // empty event needed to update push state if push is active
            EventBusRegistry.get().fireEvent(new RequestCompletedEvent());
            return;
        }
        issueRequest(delegate, request);
    }

    private void onEmptyRequest() {
        LogWindow.add(this.instanceId + ":" + this.currentBlockId + " - nothing to send", false); // $NON-NLS-0$ $NON-NLS-1$
        if (DebugUtil.DEBUG) {
            for (Block block : blocks) {
                LogWindow.add(block.toString(), false);
            }
        }
    }

    private void issueRequest(final AsyncCallback<ResponseType> delegate, RequestType request) {
        request.setLocale(I18n.I.localeForRequests());
        MmwebServiceAsyncProxy.INSTANCE.getData(request, new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable throwable) {
                DmxmlContext.this.onFailure(throwable);
                delegate.onFailure(throwable);
            }

            public void onSuccess(ResponseType responseType) {
                DmxmlContext.this.onSuccess(responseType);
                delegate.onSuccess(responseType);
            }
        }, this.cancellable);
    }

    /**
     * Acknowledges the fact that an error occurred in all blocks
     * @param throwable not used
     */
    public void onFailure(Throwable throwable) {
        Firebug.error("Request failed", throwable); // $NON-NLS-0$
        for (Block block : this.blocks) {
            if (block.isPending()) {
                block.setError(null);
            }
        }
    }

    /**
     * Handles the request by looking at the results per block and setting the result for the
     * corresponding {@link de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext.Block}s
     * @param responseType as obtained from server
     */
    public void onSuccess(ResponseType responseType) {
        final List<BlockOrError> boes = responseType.getData().getBlockOrError();
        for (BlockOrError boe : boes) {
            if (boe instanceof BlockType) {
                assignResult((BlockType) boe);
            }
            else {
                assignError((ErrorType) boe);
            }
        }
    }

    private void assignError(ErrorType error) {
        if (error.getCorrelationId().indexOf(CID_SEPARATOR) <= 0) {
            assignError(error, error.getCorrelationId());
            return;
        }
        final String[] cids = error.getCorrelationId().split(CID_SEPARATOR);
        for (String cid : cids) {
            assignError(error, cid);
        }
    }

    private void assignError(ErrorType error, String cid) {
        final Block dmxmlBlock = getBlock(cid);
        if (dmxmlBlock != null) {
            dmxmlBlock.setError(error);
        }
    }

    private void assignResult(BlockType block) {
        if (block.getCorrelationId().indexOf(CID_SEPARATOR) <= 0) {
            assignResult(block, block.getCorrelationId());
            return;
        }
        final String[] cids = block.getCorrelationId().split(CID_SEPARATOR);
        for (String cid : cids) {
            assignResult(block, cid);
        }
    }

    private void assignResult(BlockType block, String cid) {
        final Block dmxmlBlock = getBlock(cid);
        if (dmxmlBlock != null) {
            //noinspection unchecked
            dmxmlBlock.setResult(block);
        }
    }

    /**
     * Removes the given block from this context
     * @param block to be removed
     * @return true iff removed
     */
    public boolean removeBlock(Block block) {
        return this.blocks.remove(block);
    }

    /**
     * Removes the block with the given id
     * @param id identifies block
     * @return removed block or null if no such block exists
     */
    public Block removeBlock(String id) {
        final int p = getBlockPosition(id);
        if (p != -1) {
            return this.blocks.remove(p);
        }
        return null;
    }

    /**
     * Creates a RequestType object with all the blocks defined for this object that are to be
     * requested (and sets the state of those blocks to {@link BlockState#PENDING}).
     * Will not set authentication information on the result.
     * @return request
     */
    private RequestType toRequest() {
        final RequestType result = new RequestType();

        final ArrayList<Block> blocksToBe = getBlocksToBeRequested();
        final String[] cids = getCorrelationIDs(blocksToBe);

        for (int i = 0; i < blocksToBe.size(); i++) {
            final Block block = blocksToBe.get(i);
            block.setPending();
            if (cids[i] != null) {
                final RequestedBlockType blockRequest = block.toRequestedBlockType();
                blockRequest.setId(cids[i]);
                result.getBlock().add(blockRequest);
            }
        }

        return result;
    }

    /**
     * Returns an array of correlation-IDs to be used with the block at the corresponding
     * position in the blocksToBe list. For all sets of blocks in blocksToBe that represent
     * identical requests, only the correlation-ID of the block with the lowest index will not be
     * null (it will contain the ids of all blocks separated by CID_SEPARATOR). Thus, a null value
     * in the result array indicates that the corresponding blocks does not need to be requested,
     * as the result will be the same as for another block.
     * @param blocksToBe return ids for these blocks
     * @return correlation ids
     */
    private String[] getCorrelationIDs(ArrayList<Block> blocksToBe) {
        final String[] result = new String[blocksToBe.size()];
        NEXT_BLOCK:
        for (int i = 0; i < blocksToBe.size(); i++) {
            final Block block = blocksToBe.get(i);
            for (int j = 0; j < i; j++) {
                final Block block0 = blocksToBe.get(j);
                if (block.isSameRequestAs(block0)) {
                    result[j] = result[j] + CID_SEPARATOR + block.getId();
                    continue NEXT_BLOCK;
                }
            }
            result[i] = block.getId();
        }
        return result;
    }

    private ArrayList<Block> getBlocksToBeRequested() {
        final ArrayList<Block> result = new ArrayList<>(this.blocks.size());
        for (Block block : this.blocks) {
            if (block.isToBeRequested() && !result.contains(block)) {
                result.add(block);
                Block d = block.dependsOn;
                while (d != null && !result.contains(d)) {
                    result.add(d);
                    d = d.dependsOn;
                }
            }
        }
        return result;
    }

    /**
     * Returns a unique block id. No block in this object nor any block from another DmxmlContext
     * will use the same id.
     * @return unique block id
     */
    private String getBlockId() {
        return this.instanceId + ":" + ++this.currentBlockId; // $NON-NLS-0$
    }

    private int getBlockPosition(String id) {
        for (int i = 0; i < this.blocks.size(); i++) {
            final Block block = this.blocks.get(i);
            if (block.id.equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
