package de.marketmaker.iview.mmgwt.mmweb.tools.apofundpricelists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.dmxml.ResponseType;

/**
 * @author umaurer
 */
public class ApoFundPricelists implements InitializingBean {
    private static final Log LOGGER = LogFactory.getLog(ApoFundPricelists.class);

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                LogFactory.getLog(ApoFundPricelists.class).info("load configuration from classpath");
                new ClassPathXmlApplicationContext("apo-fund-pricelists.xml", ApoFundPricelists.class);
            }
            else if (args[0].startsWith("file:")) {
                LogFactory.getLog(ApoFundPricelists.class).info("load configuration from " + args[0]);
                new FileSystemXmlApplicationContext(args[0]);
            }
            else if (new File(args[0]).isFile()) {
                LogFactory.getLog(ApoFundPricelists.class).info("load configuration from file: " + args[0]);
                new FileSystemXmlApplicationContext(args[0]);
            }
            else {
                LogFactory.getLog(ApoFundPricelists.class).info("load configuration from classpath: " + args[0]);
                new ClassPathXmlApplicationContext(args[0], ApoFundPricelists.class);
            }
        } catch (Throwable e) {
            LOGGER.error("cannot generate pricelists", e);
            System.exit(1);
        }
    }

    private final JaxbHandler handler = new JaxbHandler("de.marketmaker.iview.dmxml");

    final Map<String, List<String>> mapFundtype = new HashMap<String, List<String>>();

    private final Gson gson = new Gson();

    private boolean asObjects = true;

    private String listsUrl;

    private File fileJson;

    private RestTemplate restTemplate;

    private String uri;

    private String authentication = "dzbank-vrbp";

    private String authenticationType = "resource";

    @Required
    public void setListsUrl(String listsUrl) {
        this.listsUrl = listsUrl;
    }

    @Required
    public void setFileJson(String fileJson) {
        this.fileJson = new File(fileJson);
    }

    public void setAsObjects(boolean asObjects) {
        this.asObjects = asObjects;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(
                new AbstractHttpMessageConverter<ResponseType>(MediaType.parseMediaType("text/xml;charset=UTF-8")) {
            @Override
            protected boolean supports(Class<?> aClass) {
                return aClass == ResponseType.class;
            }

            @Override
            protected ResponseType readInternal(Class<? extends ResponseType> aClass,
                    HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
                return handler.unmarshal(new StreamSource(httpInputMessage.getBody()), ResponseType.class);
            }

            @Override
            protected void writeInternal(ResponseType responseType,
                    HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
                throw new UnsupportedOperationException();
            }
        });
    }

    public void afterPropertiesSet() throws Exception {
        final List<String> listVwdKeys = getVwdKeys();

        final Set<String> setAllQids = new HashSet<String>(listVwdKeys.size());

        requestFundtypes(listVwdKeys, setAllQids);

        final List<String> listListnames = new ArrayList<String>(mapFundtype.keySet());
        Collections.sort(listListnames);
        LOGGER.info("fundtypes found: " + listListnames);

        // retrieve top funds
        final Map<String, List<String>> mapTopFunds = requestTopFunds(setAllQids);

        LOGGER.info("write result file: " + this.fileJson.getAbsolutePath());
        final PrintStream out = new PrintStream(new FileOutputStream(this.fileJson));
        out.println("{");
        printLists(out, listListnames);
        for (int id = 0; id < listListnames.size(); id++) {
            final String listname = listListnames.get(id);
            printList(out, "list_apo_funds_" + id, listname, getFundsByType(listname));
        }
        for (Map.Entry<String, List<String>> entry : mapTopFunds.entrySet()) {
            final String listname = entry.getKey();
            printList(out, listname, listname, mapTopFunds.get(listname));
        }
        out.println();
        out.println("}");
        out.close();

        LOGGER.info("SUCCESS");
    }

    private void requestFundtypes(List<String> listVwdKeys,
            Set<String> setAllQids) throws Exception {
        final MoleculeRequest request = createRequest();
        final String queryVwdCodes = getQuery("vwdCode", listVwdKeys, false);
        addAtom(request, "all", String.valueOf(listVwdKeys.size()), queryVwdCodes, "name", "true");

        LOGGER.info("request finder result for " + listVwdKeys.size() + " funds");
        final ResponseType response = request(request);
        LOGGER.info("evaluate fundtypes");
        final List<BlockOrError> listBlockOrError = response.getData().getBlockOrError();
        for (BlockOrError boe : listBlockOrError) {
            if (boe instanceof ErrorType) {
                final ErrorType error = (ErrorType) boe;
                System.out.println(error.getDescription());
            }
            else if (boe instanceof FNDFinder) {
                final FNDFinder fndFinder = (FNDFinder) boe;
                for (FNDFinderElement e : fndFinder.getElement()) {
                    final String fundtype = e.getFundtype() == null ? "Sonstige Wertpapierfonds" : e.getFundtype();
                    List<String> listFundtype = getFundsByType(fundtype);
                    if (listFundtype == null || listFundtype.isEmpty()) {
                        listFundtype = new ArrayList<String>();
                        mapFundtype.put(fundtype, listFundtype);
                    }
                    final String qid = e.getQuotedata().getQid();
                    listFundtype.add(qid);
                    setAllQids.add(qid);
                }
            }
        }
    }

    private MoleculeRequest createRequest() {
        final MoleculeRequest request = new MoleculeRequest();
        request.setAuthentication(authentication);
        request.setAuthenticationType(authenticationType);
        return request;
    }

    private Map<String, List<String>> requestTopFunds(Set<String> setAllQids) throws Exception {
        final Set<String> setChartOthers = new HashSet<String>(setAllQids);
        final List<String> listQidEquity = getFundsByType("Aktienfonds");
        final List<String> listQidBond = getFundsByType("Rentenfonds");
        setChartOthers.removeAll(listQidEquity);
        setChartOthers.removeAll(listQidBond);
        final List<String> listQidOther = new ArrayList<String>(setChartOthers);

        final MoleculeRequest request = createRequest();
        addAtom(request, "list_apo_top_funds_equity", "4", getQuery("qid", listQidEquity, true), "bviperformance1y", "false");
        addAtom(request, "list_apo_top_funds_bond", "4", getQuery("qid", listQidBond, true), "bviperformance1y", "false");
        addAtom(request, "list_apo_top_funds_other", "4", getQuery("qid", listQidOther, true), "bviperformance1y", "false");

        final Map<String, List<String>> map = new HashMap<String, List<String>>();

        LOGGER.info("request top funds");
        final ResponseType response = request(request);
        LOGGER.info("evaluate top funds");
        final List<BlockOrError> listBlockOrError = response.getData().getBlockOrError();
        for (BlockOrError boe : listBlockOrError) {
            if (boe instanceof ErrorType) {
                final ErrorType error = (ErrorType) boe;
                System.out.println(error.getDescription());
            }
            else if (boe instanceof FNDFinder) {
                final FNDFinder fndFinder = (FNDFinder) boe;
                final List<String> list = new ArrayList<String>();
                for (FNDFinderElement e : fndFinder.getElement()) {
                    final String qid = e.getQuotedata().getQid();
                    list.add(qid);
                }
                map.put(fndFinder.getCorrelationId(), list);
            }
        }
        return map;
    }

    private List<String> getFundsByType(final String type) {
        List<String> result = mapFundtype.get(type);
        return (result != null) ? result : Collections.<String>emptyList();
    }

    private void addAtom(MoleculeRequest mr, String id, String count, String queryVwdCodes,
            final String sortBy, final String ascending) {
        Map<String, String[]> map = new HashMap<>();
        map.put("offset", new String[]{"0"});
        map.put("count", new String[]{count});
        map.put("sortBy", new String[]{sortBy});
        map.put("ascending", new String[]{ascending});
        map.put("query", new String[]{queryVwdCodes});
        mr.addAtom(id, "FND_Finder", map, null);
    }

    private String getQuery(final String parameter, List<String> listEntries,
            boolean cutQidSuffix) {
        final StringBuilder sb = new StringBuilder();
        sb.append(parameter).append("=='");
        String divider = "";
        for (String entry : listEntries) {
            if (cutQidSuffix) {
                entry = entry.replaceFirst("\\.qid", "");
            }
            sb.append(divider).append(entry);
            divider = "@";
        }
        sb.append("'");
        return sb.toString();
    }

    private void printLists(PrintStream out, List<String> list) {
        out.print("    \"apo-fund-pricelists\":[");
        String komma = "";
        for (int id = 0; id < list.size(); id++) {
            final String listname = list.get(id);
            out.println(komma);
            komma = ",";
            out.print("        {\"id\":\"" + id + "\", \"title\":\"" + listname + "\"}");
        }
        out.println();
        out.print("    ]");
    }

    private void printList(PrintStream out, String listKey, String listname, List<String> listQid) {
        out.println(",");
        out.println("    \"" + listKey + "\":{");
        out.println("        \"title\":\"" + listname + "\",");
        out.print("        \"elements\": [");
        String komma = "";
        for (String qid : listQid) {
            out.println(komma);
            out.print("            {\"id\":\"");
            out.print(qid);
            out.print("\"}");
            komma = ",";
        }
        out.println();
        out.println("        ]");
        out.print("    }");
    }


    public void evaluateLists() throws Exception {
        final URL url = new URL(this.listsUrl);
        final InputStream inputStream = url.openStream();

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Document doc = factory.newDocumentBuilder().parse(inputStream);
        inputStream.close();

        final PrintStream out = new PrintStream(new FileOutputStream(this.fileJson));
        out.println('{');
        out.print("    \"apo-fund-pricelists\":[");
        String komma = "";
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final NodeList nodeList = (NodeList) xpath.evaluate("/list/entries", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            final String id = (String) xpath.evaluate("list_id/text()", node, XPathConstants.STRING);
            final String name = (String) xpath.evaluate("list_name/text()", node, XPathConstants.STRING);

            out.println(komma);
            komma = ",";
            if (this.asObjects) {
                out.print("        { \"id\":\"" + id + "\", \"title\":\"" + name + "\" }");
            }
            else {
                out.println("        {");
                out.println("            \"id\":\"" + id + "\",");
                out.println("            \"title\":\"" + name + "\",");
                out.print("            \"elements\": [");
                evaluateList(out, factory, xpath, id);
                out.println("           ]");
                out.print("        }");
            }
        }
        out.println();
        out.print("    ]");

        if (this.asObjects) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                final String id = (String) xpath.evaluate("list_id/text()", node, XPathConstants.STRING);
                final String name = (String) xpath.evaluate("list_name/text()", node, XPathConstants.STRING);

                out.println(",");
                out.println("    \"list_apo_funds_" + id + "\":{");
                out.println("        \"title\":\"" + name + "\",");
                out.print("        \"elements\": [");
                evaluateList(out, factory, xpath, id);
                out.println("       ]");
                out.print("    }");
            }
        }


        out.println();
        out.println("}");
        out.close();
    }


    private void evaluateList(PrintStream out, DocumentBuilderFactory factory, XPath xpath,
            String id) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        final URL urlList = new URL(this.listsUrl + "&id=" + id);
        final InputStream inputStream = urlList.openStream();
        final Document docList = factory.newDocumentBuilder().parse(inputStream);
        inputStream.close();

        String komma = "";
        final NodeList nodeList = (NodeList) xpath.evaluate("/list/entries/entry", docList, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            final String vwdKey = (String) xpath.evaluate("instrumentkey/text()", node, XPathConstants.STRING);
//            final String isin = (String) xpath.evaluate("isin/text()", node, XPathConstants.STRING);
            out.println(komma);
            komma = ",";
            if (this.asObjects) {
                out.print("            {\"id\":");
            }
            else {
                out.print("                ");
            }
            out.print("\"" + vwdKey + "\"");
            if (this.asObjects) {
                out.print("}");
            }
        }
        out.println();
    }


    private List<String> getVwdKeys() throws Exception {
        LOGGER.info("request xml file: " + this.listsUrl);
        final URL urlList = new URL(this.listsUrl);
        final InputStream inputStream = urlList.openStream();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Document docList = factory.newDocumentBuilder().parse(inputStream);
        inputStream.close();
        final XPath xpath = XPathFactory.newInstance().newXPath();

        final List<String> list = new ArrayList<String>();
        final NodeList nodeList = (NodeList) xpath.evaluate("/list/entries/entry", docList, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            list.add((String) xpath.evaluate("instrumentkey/text()", node, XPathConstants.STRING));
        }
        LOGGER.info("nr of funds: " + list.size());
        return list;
    }

    private ResponseType request(final MoleculeRequest request) throws Exception {
        String json = gson.toJson(request);

        LOGGER.info("<request> " + json);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        return this.restTemplate.postForObject(uri, entity, ResponseType.class);
    }

}
