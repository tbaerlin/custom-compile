package de.marketmaker.iview.mmgwt.mmweb.client.util;


public class WarrantTypeRenderer implements Renderer<String> {

    @Override
    public String render(String value) {
        return render(value, "n/a"); // $NON-NLS$
    }

    public String render(String value, String fallback) {
        switch (value) {
            case "P": // $NON-NLS$
                return "Put"; // $NON-NLS$
            case "C": // $NON-NLS$
                return "Call"; // $NON-NLS$
            default:
                return fallback;
        }
    }

}
