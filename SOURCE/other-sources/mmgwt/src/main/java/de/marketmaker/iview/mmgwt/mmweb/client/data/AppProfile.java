/*
 * ProfileInfo.java
 *
 * Created on 29.04.2008 10:20:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AppProfile implements Serializable {
    protected static final long serialVersionUID = 1L;

    private HashSet<String> functions = new HashSet<String>();

    private HashSet<String> news = new HashSet<String>();

    private HashSet<String> pages = new HashSet<String>();

    private HashSet<String> products = new HashSet<String>();

    private String produktId;

    public String getProduktId() {
        return produktId;
    }

    public void setProduktId(String produktId) {
        this.produktId = produktId;
    }

    public boolean isFunctionAllowed(String s) {
        return this.functions.contains(s);
    }

    public boolean isNewsAllowed(String s) {
        return this.news.contains(s);
    }

    public boolean isAnyNewsAllowed() {
        return !this.news.isEmpty();
    }

    public boolean isPageAllowed(String s) {
        return this.pages.contains(s);
    }

    public boolean isProductAllowed(String s) {
        return this.products.contains(s);
    }

    public void setFunctions(List<String> allowedFunctions) {
        replace(this.functions, allowedFunctions);
    }

    public void setNews(List<String> allowedNews) {
        replace(this.news, allowedNews);
    }

    public void setPages(List<String> allowedPages) {
        replace(this.pages, allowedPages);
    }

    public void setProducts(List<String> allowedProducts) {
        replace(this.products, allowedProducts);
    }

    public HashSet<String> getFunctions() {
        return functions;
    }

    public HashSet<String> getNews() {
        return news;
    }

    public HashSet<String> getPages() {
        return pages;
    }

    public HashSet<String> getProducts() {
        return products;
    }

    public String toString() {
        return "AppProfile[f=" + this.functions // $NON-NLS-0$
                + ", p=" + this.products // $NON-NLS-0$
                + ", n=" + this.news // $NON-NLS-0$
                + "]"; // $NON-NLS-0$
    }

    private void replace(HashSet<String> target, List<String> values) {
        target.clear();
        target.addAll(values);
    }
}
