package de.marketmaker.istar.merger.web.xmlmarket;

/**
 *
 */
public class UnknownSymbolDocument extends ErrorDocument {

    UnknownSymbolDocument(String symbol) {
        super("6001",
                "invalid parameter content",
                "Der eingegebene Parameterwert ist ungültig.",
                "Zu der eingegebenen WKN / ISIN (" + symbol + ") wurde kein Instrument gefunden",
                null);
    }
}
