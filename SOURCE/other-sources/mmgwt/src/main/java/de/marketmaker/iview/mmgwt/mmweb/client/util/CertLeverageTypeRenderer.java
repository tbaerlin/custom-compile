package de.marketmaker.iview.mmgwt.mmweb.client.util;

public class CertLeverageTypeRenderer implements Renderer<String> {

    @Override
    public String render(String value) {
        switch (value) {
            case "PUT":   // $NON-NLS$
                return "Put"; // $NON-NLS$
            case "CALL":  // $NON-NLS$
                return "Call"; // $NON-NLS$
            default:
                return value;
        }
    }

}
