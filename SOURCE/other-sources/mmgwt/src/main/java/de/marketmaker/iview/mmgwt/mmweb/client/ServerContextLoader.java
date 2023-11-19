package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich Maurer
 *         Date: 11.03.13
 */
public class ServerContextLoader extends ResponseTypeCallback implements Command {
    private final DmxmlContext context = new DmxmlContext();
    private final DmxmlContext.Block<IMGResult> blockChartAnalysis;


    public ServerContextLoader() {
        this.context.setCancellable(false);
        this.blockChartAnalysis = this.context.addBlock("IMG_Chart_Analysis"); // $NON-NLS$
        this.blockChartAnalysis.setParameter("symbol", "106547.qid"); // $NON-NLS$
    }

    @Override
    public void execute() {
        AbstractMainController.INSTANCE.updateProgress(I18n.I.loadServerContext());
        this.context.issueRequest(this);
    }

    @Override
    protected void onResult() {
        if (this.blockChartAnalysis.isResponseOk()) {
            final String prefix = getPrefix(this.blockChartAnalysis.getResult().getRequest());
            Firebug.log("cupl: path=" + prefix);
            ChartUrlFactory.setChartUrlPrefix(prefix);
        }

        AbstractMainController.INSTANCE.runInitSequence();
    }

    private String getPrefix(String url) {
        int pos = url.indexOf('?');
        if (pos < 0) {
            pos = url.indexOf('#');
        }
        if (pos >= 0) {
            url = url.substring(0, pos);
        }
        pos = url.lastIndexOf('/');
        return url.substring(0, pos + 1);
    }
}
