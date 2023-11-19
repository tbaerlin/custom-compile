/*
 * PageAggregation.java
 *
 * Created on 11.01.2010 17:00:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

/**
 * Stores how many times a page has been accessed by users of a particular client with a unique
 * combination of selector values.
 * @author oflege
 */
public class PageAggregation {
    private final int clientId;

    private final int pageDefId;

    private int num;

    private String selector1;

    private String selector2;

    private String selector3;

    private String selector4;

    public PageAggregation(Visit visit, int pageDefId) {
        this.clientId = visit.getClient();
        this.pageDefId = pageDefId;
        this.selector1 = visit.getSelector1();
        this.selector2 = visit.getSelector2();
        this.selector3 = visit.getSelector3();
        this.selector4 = visit.getSelector4();
    }

    public int getClientId() {
        return clientId;
    }

    public int getPageDefId() {
        return pageDefId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(clientId)
                .append(", ").append(pageDefId)
                .append(", #").append(this.num)
                .append(": [");
        if (selector1 != null) {
            sb.append(selector1);
            if (selector2 != null) {
                sb.append(", ").append(selector2);
                if (selector3 != null) {
                    sb.append(", ").append(selector3);
                    if (selector4 != null) {
                        sb.append(", ").append(selector4);
                    }
                }
            }
        }
        return sb.append("]").toString();
    }

    public void incNum() {
        this.num++;
    }

    public int getNum() {
        return num;
    }

    public String getSelector1() {
        return selector1;
    }

    public String getSelector2() {
        return selector2;
    }

    public String getSelector3() {
        return selector3;
    }

    public String getSelector4() {
        return selector4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageAggregation that = (PageAggregation) o;

        if (clientId != that.clientId) return false;
        if (pageDefId != that.pageDefId) return false;
        if (pageDefId == 0) {
            return true;
        }
        if (selector1 != null ? !selector1.equals(that.selector1) : that.selector1 != null)
            return false;
        if (selector2 != null ? !selector2.equals(that.selector2) : that.selector2 != null)
            return false;
        if (selector3 != null ? !selector3.equals(that.selector3) : that.selector3 != null)
            return false;
        if (selector4 != null ? !selector4.equals(that.selector4) : that.selector4 != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientId;
        result = 31 * result + pageDefId;
        if (this.pageDefId == 0) {
            return result;
        }
        result = 31 * result + (selector1 != null ? selector1.hashCode() : 0);
        result = 31 * result + (selector2 != null ? selector2.hashCode() : 0);
        result = 31 * result + (selector3 != null ? selector3.hashCode() : 0);
        result = 31 * result + (selector4 != null ? selector4.hashCode() : 0);
        return result;
    }

    public void setSelectors(Visit visit) {
        this.selector1 = visit.getSelector1();
        this.selector2 = visit.getSelector2();
        this.selector3 = visit.getSelector3();
        this.selector4 = visit.getSelector4();
    }
}
