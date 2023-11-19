/*
 * ListCommandWithOptionalPaging.java
 *
 * Created on 15.05.2007 08:39:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.Min;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.common.validator.ValidateUnless;

/**
 * A ListCommand for which paging can be disabled entirely so that the caller will always
 * get all available results. Not part of the normal ListCommand, as it only makes sense in some
 * cases (securities in a defined list) but not in others (news).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListCommandWithOptionalPaging extends ListCommand {
    private boolean disablePaging = false;

    /**
     * @return Disables or enables paging. Defaults to {@value false}.
     * @sample true
     */
    public boolean isDisablePaging() {
        return disablePaging;
    }

    /**
     * @param disablePaging Disables or enables paging.
     */
    public void setDisablePaging(boolean disablePaging) {
        this.disablePaging = disablePaging;
    }

    @Min(0)
    @ValidateUnless("disablePaging")
    public int getOffset() {
        return isDisablePaging() ? 0 : super.getOffset();
    }

    @Range(min = 0, max = 1000)
    @ValidateUnless("disablePaging")
    public int getCount() {
        return isDisablePaging() ? Integer.MAX_VALUE : super.getCount();
    }
}
