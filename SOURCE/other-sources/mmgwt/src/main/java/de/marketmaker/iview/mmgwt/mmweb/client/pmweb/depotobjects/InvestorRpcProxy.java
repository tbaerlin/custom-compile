package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.extjs.gxt.ui.client.data.RpcProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkRequest;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.QueryMMTalk;
import de.marketmaker.iview.pmxml.TableTreeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author umaurer
 */
public class InvestorRpcProxy extends RpcProxy<List<InvestorItem>> {
    private final DmxmlContext context = new DmxmlContext();
    private QueryMMTalk mmTalkQuery = null;
    private DmxmlContext.Block<MMTalkResponse> block;
    private static InvestorRpcProxy instance = null;

    private InvestorRpcProxy() {
        this.context.setCancellable(false);
    }

    public static InvestorRpcProxy getInstance() {
        if (instance == null) {
            instance = new InvestorRpcProxy();
        }
        return instance;
    }

    @Override
    protected void load(Object loadConfig, final AsyncCallback<List<InvestorItem>> callback) {
        if (loadConfig == null) {
            final InvestorItem rootGroup;
            final String pmRootName = SessionData.INSTANCE.getUserProperty("pmRootName"); // $NON-NLS$
            if (pmRootName != null) {
                final String pmRootId = SessionData.INSTANCE.getUserProperty("pmRootId"); // $NON-NLS$
                rootGroup = new InvestorItem(pmRootId == null ? "12345" : pmRootId, InvestorItem.Type.Gruppe, pmRootName, "PUBLIC", InvestorItem.HasChildren.YES, false, true); // $NON-NLS$
            }
            else {
                rootGroup = InvestorItem.ROOT_GROUP;
            }

            rootGroup.setHasChildren(InvestorItem.HasChildren.YES);
            final ArrayList<InvestorItem> list = new ArrayList<InvestorItem>(1);
            list.add(rootGroup);
            callback.onSuccess(list);
        }
        else {
            final InvestorItem parent = (InvestorItem) loadConfig;
            setQueryFormula(parent);
            this.context.issueRequest(new AsyncCallback<ResponseType>(){
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                public void onSuccess(ResponseType result) {
                    final BlockOrError boe = result.getData().getBlockOrError().get(0);
                    if (boe instanceof ErrorType) {
                        final ErrorType error = (ErrorType) boe;
                        Firebug.log("InvestorRpcProxy got Error '" + error.getCode() + "': " + error.getDescription());
                        //noinspection ThrowableInstanceNeverThrown
                        callback.onFailure(new RuntimeException(error.getDescription()));
                    }
                    else {
                        final List<InvestorItem> listChildren = addHolders(parent, (MMTable) ((MMTalkResponse) boe).getData());
                        callback.onSuccess(listChildren);
                    }
                }
            });
        }
    }

    private void setQueryFormula(InvestorItem parent) {
        if (this.mmTalkQuery == null) {
            final MMTalkRequest request = new MMTalkRequest();
            request.setName("getFolderItems"); // $NON-NLS$
            request.setLanguage("");
            request.setIntraday(false);

            this.mmTalkQuery = new QueryMMTalk();
            request.setQuery(this.mmTalkQuery);

            final TableTreeTable rootnode = new TableTreeTable();
            rootnode.setFormula("FolderItems"); // $NON-NLS$

            MmTalkHelper.addColumnFormulas(rootnode,
                    "Id", // $NON-NLS$
                    "Typ", // $NON-NLS$
                    "Name", // $NON-NLS$
                    "Zone", // $NON-NLS$
                    "if(is[\"Depot\"];Depotnummer;Identifier)", // $NON-NLS$
                    "FolderItems.Length > 0" // $NON-NLS$
            );

            request.setRootnode(rootnode);

            this.block = this.context.addBlock("PM_MMTalk"); // $NON-NLS$
            this.block.setParameter(request);
        }

        this.block.setToBeRequested();
        this.mmTalkQuery.setMMTalkFormula(getFindFolderFormula(parent));
    }

    public static String getFindFolderFormula(InvestorItem item) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Findfolder(\""); // $NON-NLS$
        sb.append(item.getType().toString());
        sb.append(':');
        sb.append(item.getName());
        sb.append(':');
        sb.append(item.getZone());
        sb.append("\")");
        return sb.toString();
    }

    private List<InvestorItem> addHolders(InvestorItem parent, MMTable data) {
        final List<InvestorItem> listInvestorItems = MmTalkHelper.getInvestorItems(data);
        Collections.sort(listInvestorItems);
        for (InvestorItem item : listInvestorItems) {
            parent.add(item);
        }
        return listInvestorItems;
    }
}