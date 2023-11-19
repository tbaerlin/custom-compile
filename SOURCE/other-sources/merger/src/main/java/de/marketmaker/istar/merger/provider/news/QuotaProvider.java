package de.marketmaker.istar.merger.provider.news;


public interface QuotaProvider {

    QuotaProvider UNLIMITED = (vwdId, amount) -> true;

    QuotaProvider NO_PERMISSION = (vwdId, amount) -> false;


    boolean acquire(String vwdId, int amount);

}
