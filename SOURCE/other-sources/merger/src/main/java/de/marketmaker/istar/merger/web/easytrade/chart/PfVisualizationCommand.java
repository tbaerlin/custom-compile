package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.merger.web.easytrade.block.UserCommand;

/**
 * PfVisualizationCommand.java
 * Created on Sep 21, 2009 1:41:35 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PfVisualizationCommand extends BaseImgCommand implements UserCommand {

    private String userid;

    private Long companyid;

    private String portfolioid;

    private String type;

    @Override
    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        sb.append("&type=").append(this.type)
                .append("&portfolioid=").append(this.portfolioid);
        if (this.userid != null) {
            sb.append("&userid=").append(this.userid);
        }
        return sb;
    }

    /**
     * @return the height of the chart in pixel
     */
    @Range(min = 10, max = 1000)
    public int getHeight() {
        return super.getHeight();
    }

    /**
     * @return the with of the chart in pixel
     * @sample 600
     */
    @Range(min = 10, max = 1000)
    public int getWidth() {
        return super.getWidth();
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return this.userid;
    }

    public Long getCompanyid() {
        return this.companyid;
    }

    public void setCompanyid(Long companyid) {
        this.companyid = companyid;
    }

    /**
     * @return defines the attribute that pie chart will show
     */
    @NotNull
    @RestrictedSet("TYP,LAND,WAEHRUNG,ASSET")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NotNull
    public String getPortfolioid() {
        return portfolioid;
    }

    public void setPortfolioid(String portfolioid) {
        this.portfolioid = portfolioid;
    }
}