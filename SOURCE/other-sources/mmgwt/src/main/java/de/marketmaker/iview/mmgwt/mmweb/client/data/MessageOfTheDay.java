package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Ulrich Maurer
 *         Date: 14.05.12
 */
public class MessageOfTheDay implements Serializable {
    private Date firstDate;
    private Date lastDate;
    private String message;

    public Date getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(Date firstDate) {
        this.firstDate = firstDate;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
