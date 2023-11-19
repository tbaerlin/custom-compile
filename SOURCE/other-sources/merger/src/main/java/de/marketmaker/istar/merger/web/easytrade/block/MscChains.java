package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.provider.chain.ChainData;
import de.marketmaker.istar.merger.provider.chain.ChainsProvider;
import de.marketmaker.istar.merger.provider.chain.ChainsRequest;
import de.marketmaker.istar.merger.provider.chain.ChainsResponse;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;


public class MscChains extends EasytradeCommandController {

    private ChainsProvider chainsProvider;

    public static class Command {

        private String query;

        private boolean elements = false;

        public int offset = 0;

        private int count = 10;

        private String[] searchfield = {"names"};

        private String searchstring;


        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public boolean getElements() {
            return elements;
        }

        public void setElements(boolean elements) {
            this.elements = elements;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        @MmInternal
        public String[] getSearchfield() {
            return searchfield;
        }

        public void setSearchfield(String[] searchfield) {
            this.searchfield = searchfield;
        }

        public String getSearchstring() {
            return searchstring;
        }

        public void setSearchstring(String searchstring) {
            this.searchstring = searchstring;
        }

    }

    public MscChains() {
        super(Command.class);
    }

    public void setChainsProvider(ChainsProvider chainsProvider) {
        this.chainsProvider = chainsProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final ChainsRequest chainsRequest = getChainsRequest(cmd);
        final ChainsResponse chainsResponse = chainsProvider.searchChainElements(chainsRequest);
        final List<ChainData> elements = chainsResponse.getElements();
        final Map<String, Object> model = new HashMap<>();
        model.put("query", chainsRequest.getQuery());
        model.put("offset", cmd.getOffset());
        model.put("count", elements == null ? 0 : elements.size());
        model.put("total", chainsResponse.getTotal());
        model.put("elements", cmd.getElements());
        model.put("chains", elements);
        return new ModelAndView("mscchains", model);
    }

    @NonNull
    private ChainsRequest getChainsRequest(Command cmd) {
        final ChainsRequest chainsRequest = new ChainsRequest();
        chainsRequest.setQuery(getQuery(cmd));
        chainsRequest.setOffset(cmd.getOffset());
        chainsRequest.setCount(cmd.getCount());
        return chainsRequest;
    }

    private String getQuery(Command cmd) {
        String query = cmd.getQuery();
        if (StringUtils.isEmpty(query)) {
            final StringBuilder stringBuilder = new StringBuilder();
            final String[] fields = cmd.getSearchfield();
            if (fields.length > 0) {
                append(stringBuilder, fields[0], cmd.getSearchstring());
            }
            for (int i = 1; i < fields.length; i++) {
                stringBuilder.append(" OR ");
                append(stringBuilder, fields[i], cmd.getSearchstring());
            }
            query = stringBuilder.toString();
        }
        return query;
    }

    // TODO: this method is a collection of ugly hacks to fix T-49953
    // TODO: lucene special chars are: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
    // see: https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
    private void append(StringBuilder stringBuilder, String fieldName, String value) {
        switch (fieldName) {
            case "vwdcode":
                stringBuilder.append("vwdCode");
                break;
            case "vwdsymbol":
                stringBuilder.append("vwdSymbol");
                break;
            case "chaininstrument":
                stringBuilder.append("chainInstrument");
                break;
            default:
                stringBuilder.append(fieldName);
        }
        stringBuilder.append(" == ");
        if (value.startsWith("'")) {
            value = value.substring(1);
        }
        if (value.endsWith("'")) {
            value = value.substring(0, value.length()-1);
        }

        stringBuilder.append("'");
        stringBuilder.append(StringEscapeUtils.ESCAPE_XML.translate(value));
        stringBuilder.append("'");
    }
}