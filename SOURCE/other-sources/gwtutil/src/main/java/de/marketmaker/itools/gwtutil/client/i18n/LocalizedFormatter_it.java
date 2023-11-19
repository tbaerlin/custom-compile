package de.marketmaker.itools.gwtutil.client.i18n;

/**
 * @author Ulrich Maurer
 *         Date: 28.02.12
 */
public class LocalizedFormatter_it extends LocalizedFormatter_de {
    @Override
    public String getPlaceholderIsoDay() {
        return "aaaa-mm-gg";
    }

    @Override
    public String getPlaceholderDmy() {
        return "gg.mm.aaaa";
    }

    public String getPlaceholderHm() {
        return "oo:mm";
    }

    public String getPlaceholderHms() {
        return "oo:mm:ss";
    }

}
