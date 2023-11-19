import java.io.*;
import java.net.*;
import java.util.List;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

public class DmxmlRequestSample {
    public static void main(String[] args) throws Exception {
        // authentication handling
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("lbbw", "vwdtest".toCharArray());
            }
        });

        // create POST data
        final String request = URLEncoder.encode("request", "UTF-8") + "=" + URLEncoder.encode(getRequest(), "UTF-8");

        // send request
        final URL url = new URL("http://dm-test.vwd.com/dmxml-1/iview/retrieve.xml");
        final URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(request);
        wr.flush();

        // process result with JDOM
        final SAXBuilder builder = new SAXBuilder();
        final InputStream is = conn.getInputStream();



        final Document document = builder.build(is);
        final Element data = document.getRootElement().getChild("data");
        final Element block = data.getChild("block");

        final String symbol = block.getChild("quotedata").getChildTextTrim("vwdcode");
        final String lastPrice = block.getChild("pricedata").getChildTextTrim("price");
        System.out.println(symbol + ": " + lastPrice);

        is.close();
        wr.close();
    }

    private static String getRequest() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request>\n" +
                "  <authentication>mm-xml</authentication>\n" +
                "  <authenticationType>resource</authenticationType>\n" +
                "   <block key=\"MSC_PriceData\">\n" +
                "    <parameter key=\"symbol\" value=\"EUR.FXVWD\"/>\n" +
                "  </block>\n" +
                "</request>";
    }
}
