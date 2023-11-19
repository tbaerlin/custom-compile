package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerUpDownData implements Serializable {
    static final long serialVersionUID = 1L;

    private final LocalDate referencedate;
    private final String region;
    private final List<Long> ups;
    private final List<Long> downs;
    private final List<Integer> upsStars;
    private final List<Integer> downsStars;

    @SuppressWarnings({"RedundantArrayCreation"})
    public ScreenerUpDownData(LocalDate referencedate, String region, List<Long> ups, List<Long> downs) {
        this.referencedate = referencedate;
        this.region = region;
        this.ups = ups;
        this.downs = downs;
        // create null-containing arrays as initial value
        this.upsStars = new ArrayList<>(Arrays.asList(new Integer[ups.size()]));
        this.downsStars= new ArrayList<>(Arrays.asList(new Integer[downs.size()]));
    }

    public LocalDate getReferencedate() {
        return referencedate;
    }

    public String getRegion() {
        return region;
    }

    public List<Long> getUps() {
        return ups;
    }

    public List<Long> getDowns() {
        return downs;
    }

    public void setUpStar(int index, Integer stars) {
        this.upsStars.set(index, stars);
    }

    public void setDownStar(int index, Integer stars) {
        this.downsStars.set(index, stars);
    }

    public List<Integer> getUpsStars() {
        return upsStars;
    }

    public List<Integer> getDownsStars() {
        return downsStars;
    }

    public String toString() {
        return "ScreenerUpDownData[referencedate=" + referencedate
                + ", region=" + region
                + ", ups=" + ups
                + ", upsStars=" + upsStars
                + ", downs=" + downs
                + ", downsStars=" + downsStars
                + "]";
    }
}
