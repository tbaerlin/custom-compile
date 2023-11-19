package de.marketmaker.itools.gwtutil.client.util.date;

/**
 * @author umaurer
 */
public interface DateListener {
    /**
     * Indicates that the specified date was selected.
     * {@code date} can be {@code null}, which means "alltime" was chosen.
     *
     * @param date The selected date or {@code null}, if "alltime" was chosen.
     */
    public void setDate(MmJsDate date);
}
