package de.marketmaker.istar.merger.alert;

/**
 * constants according to the spec "WebService Schnittstellenbeschreibung"
 * see: http://cfl.market-maker.de/display/DAS/Entwicklung+AlertServer
 *
 * the int is used for requesting alerts with a certain state, this is not the same as
 * AlertStatus since we don't have RESTARTED or EXECUTED here
 */
public enum RetrieveAlertStatus {
    INVALID(-1),
    ANY(0),            // 0 = alle
    ACTIVE(1),         // 1 = aktive
    FIRED(2),          // 2 = executed
    EXPIRED(3),        // 3 = expired
    DELETED(4),        // 4 = deleted
    ANY_UNDELETED(5),  // 5 != deleted
    ;

    final int statusCode;

    RetrieveAlertStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
